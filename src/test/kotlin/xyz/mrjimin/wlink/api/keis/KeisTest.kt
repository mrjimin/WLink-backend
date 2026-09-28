package xyz.mrjimin.wlink.api.keis

import com.github.mrjimin.keis.core.api.school.schoolContext
import com.github.mrjimin.keis.ktor.keisKtor
import io.kotest.matchers.shouldBe
import org.junit.Test
import xyz.mrjimin.wlink.config.applicationHttpClient
import xyz.mrjimin.wlink.util.requiredEnv

class KeisTest {
    val testKeisContext = keisKtor(requiredEnv("KEIS_API"), applicationHttpClient)
        .schoolContext("우석고등학교") ?: throw error("그냥 어이 없는 오류")

    @Test
    fun `keis test`() {
        val schoolName = testKeisContext.school.name
        schoolName shouldBe "우석고등학교"
    }
}
