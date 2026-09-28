package xyz.mrjimin.wlink.api.keis.meal

import org.junit.Test
import xyz.mrjimin.wlink.api.keis.testKeisContext
import java.time.LocalDate

class MealServiceTest {

    @Test
    fun `full meal list`() {
        val meals = testKeisContext.meals {
            thisMonth()
        }
        println(meals)
    }

    @Test
    fun `meals list by date`() {
        val date = "20260928"

        val meals = testKeisContext.meals {
            date(date)
        }

        println(meals)
    }

    @Test
    fun `java localTime`() {
        val nowYear = LocalDate.now().year
        val nowMonth = LocalDate.now().monthValue
        val nowDay = LocalDate.now().dayOfMonth

        println(nowYear)
        println(nowMonth)
        println(nowDay)
    }
}
