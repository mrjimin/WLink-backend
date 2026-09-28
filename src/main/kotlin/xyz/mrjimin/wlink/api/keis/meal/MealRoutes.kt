package xyz.mrjimin.wlink.api.keis.meal

import com.github.mrjimin.keis.core.domain.enums.MealType
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import xyz.mrjimin.wlink.util.NOW
import java.time.LocalDate

fun Route.mealRoutes() {
    val mealsService by inject<MealService>()

    route("/api/meals") {
        get {
            val date = call.queryParameters["date"]
                ?.let(LocalDate::parse)
                ?: NOW

            val type = call.queryParameters["type"]
                ?.let(MealType::from)
                ?: MealType.ALL

            call.respond(mealsService.getMeals(date, type))
        }
    }
}

