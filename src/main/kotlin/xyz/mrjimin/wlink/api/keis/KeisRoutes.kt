package xyz.mrjimin.wlink.api.keis

import io.ktor.server.routing.*
import xyz.mrjimin.wlink.api.keis.meal.mealRoutes

fun Route.keisRoutes() {
    mealRoutes()
}
