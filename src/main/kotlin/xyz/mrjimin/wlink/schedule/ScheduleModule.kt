package xyz.mrjimin.wlink.schedule

import org.koin.dsl.module

val scheduleModule = module {
    single<ScheduleRepository> {
        ScheduleRepositoryImpl()
    }

    single {
        ScheduleService(get(),)
    }
}