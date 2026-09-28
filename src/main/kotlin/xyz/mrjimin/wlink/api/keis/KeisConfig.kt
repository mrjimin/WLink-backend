package xyz.mrjimin.wlink.api.keis

import com.github.mrjimin.keis.core.api.school.schoolContext
import com.github.mrjimin.keis.ktor.keisKtor
import xyz.mrjimin.wlink.config.applicationHttpClient
import xyz.mrjimin.wlink.util.requiredEnv

val testKeisContext = keisKtor(requiredEnv("KEIS_API"), applicationHttpClient)
    .schoolContext("우석고등학교") ?: throw error("그냥 어이 없는 오류")
