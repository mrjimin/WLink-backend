package xyz.mrjimin.wlink.api.keis.meal

import com.github.mrjimin.keis.core.adapter.dto.MealResponseDto
import com.github.mrjimin.keis.core.adapter.mapper.toDto
import com.github.mrjimin.keis.core.domain.enums.MealType
import xyz.mrjimin.wlink.api.keis.testKeisContext
import java.time.LocalDate

class MealService {

    fun getMeals(
        date: LocalDate,
        type: MealType
    ): List<MealResponseDto> {
        return testKeisContext.meals {
            if (type != MealType.ALL) type(type)
            date(date)
        }.map { it.toDto() }
    }

}
