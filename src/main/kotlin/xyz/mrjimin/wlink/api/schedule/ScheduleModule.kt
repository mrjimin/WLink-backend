package xyz.mrjimin.wlink.api.schedule

import org.koin.dsl.module

val scheduleModule = module {
    single<ScheduleRepository> {
        ScheduleRepositoryImpl()
    }

    single {
        ScheduleService(get(),)
    }
}
