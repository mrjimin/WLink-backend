package xyz.mrjimin.wlink.api.keis

import org.koin.dsl.module
import xyz.mrjimin.wlink.api.keis.meal.MealService

val keisModule = module {
    single { MealService() }
}
