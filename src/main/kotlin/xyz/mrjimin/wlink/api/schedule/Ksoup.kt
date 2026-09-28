//package xyz.mrjimin.wlink.schedule
//
//import com.github.mrjimin.ksoup.ksoup
//import io.ktor.client.*
//import io.ktor.client.call.*
//import io.ktor.client.engine.cio.*
//import io.ktor.client.request.*
//import io.ktor.http.*
//import io.ktor.server.response.*
//import io.ktor.server.routing.*
//import kotlinx.datetime.LocalDate
//import kotlinx.datetime.LocalDateTime
//import org.jsoup.nodes.Document
//import org.jsoup.nodes.Element
//import org.jsoup.nodes.Node
//import org.jsoup.nodes.TextNode
//import org.jsoup.select.NodeVisitor
//import kotlin.uuid.ExperimentalUuidApi
//import kotlin.uuid.Uuid
//
//private val BASE_URL = "https://school.jbedu.kr/woosuk/M010501/list.do"
//private const val CRAWL_MONTHS = 3
//
//private val EXCLUDED_KEYWORDS = setOf(
//    "학사일정", "교육활동", "우석고등학교", "메인메뉴", "본문내용", "퀵메뉴"
//)
//
//private val DATE_PATTERN = Regex(
//    """(\d{4}\.\d{2}\.\d{2}(?:\s*~\s*\d{4}\.\d{2}\.\d{2})?)\s*\n?-?\s*([^\n]+)"""
//)
//
//private fun getHtmlTextWithNewlines(doc: Document): String {
//    val sb = StringBuilder()
//    doc.traverse(object : NodeVisitor {
//        override fun head(node: Node, depth: Int) {
//            if (node is TextNode) {
//                sb.append(node.text())
//            } else if (node is Element && node.isBlock) {
//                sb.append("\n")
//            }
//        }
//        override fun tail(node: Node, depth: Int) {}
//    })
//    return sb.toString()
//}
//
//private fun parseDateRange(dateText: String): Pair<LocalDate, LocalDate>? {
//    val cleanText = dateText.replace(".", "-")
//    val parts = cleanText.split("~").map { it.trim() }
//
//    val startText = parts.getOrNull(0) ?: return null
//    val endText = parts.getOrNull(1) ?: startText
//
//    return try {
//        val startDate = LocalDate.parse(startText)
//        val endDate = LocalDate.parse(endText.ifEmpty { startText })
//        if (endDate < startDate) null else Pair(startDate, endDate)
//    } catch (e: Exception) {
//        null
//    }
//}
//
//private fun isValidTitle(title: String): Boolean {
//    if (title.isBlank()) return false
//    return EXCLUDED_KEYWORDS.none { it in title }
//}
//
//suspend fun crawlMonth(client: HttpClient, year: Int, month: Int): List<Schedule> {
//    val doc: Document = client.get(BASE_URL) {
//        parameter("y", year)
//        parameter("m", month)
//    }.body()
//
//    val text = getHtmlTextWithNewlines(doc)
//    val schedulesByKey = mutableMapOf<Triple<LocalDate, LocalDate, String>, Schedule>()
//    val nowSeoul = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Seoul"))
//        .let { LocalDateTime(it.year, it.monthValue, it.dayOfMonth, it.hour, it.minute, it.second, it.nano) }
//
//    @OptIn(ExperimentalUuidApi::class)
//    for (matchResult in DATE_PATTERN.findAll(text)) {
//        val (dateText, rawTitle) = matchResult.destructured
//        val title = rawTitle.trim()
//
//        if (!isValidTitle(title)) continue
//
//        val dateRange = parseDateRange(dateText) ?: continue
//        val (startDate, endDate) = dateRange
//
//        val key = Triple(startDate, endDate, title)
//        schedulesByKey[key] = Schedule(
//            id = Uuid.random(),
//            title = title,
//            startDate = startDate,
//            endDate = endDate,
//            isPeriod = startDate != endDate,
//            createdAt = nowSeoul
//        )
//    }
//
//    return schedulesByKey.values.toList()
//}
//
//private fun addMonths(year: Int, month: Int, offset: Int): Pair<Int, Int> {
//    val monthIndex = year * 12 + month - 1 + offset
//    return Pair(monthIndex / 12, monthIndex % 12 + 1)
//}
//
//suspend fun collectSchedules(client: HttpClient): List<Schedule> {
//    val today = java.time.LocalDate.now()
//    val schedulesByKey = mutableMapOf<Triple<LocalDate, LocalDate, String>, Schedule>()
//
//    for (offset in 0 until CRAWL_MONTHS) {
//        val (year, month) = addMonths(today.year, today.monthValue, offset)
//        val monthSchedules = crawlMonth(client, year, month)
//        for (schedule in monthSchedules) {
//            schedulesByKey[Triple(schedule.startDate, schedule.endDate, schedule.title)] = schedule
//        }
//    }
//
//    return schedulesByKey.values.sortedWith(compareBy({ it.startDate }, { it.endDate }, { it.title }))
//}
//
//fun Route.schedule() {
//    get("/schedule") {
//        val client = HttpClient(CIO) {
//            ksoup {
//                parseAsHtml(ContentType.Text.Html)
//            }
//        }
//
//        client.use { httpClient ->
//            val schedules = collectSchedules(httpClient)
//            call.respond(schedules)
//        }
//    }
//}