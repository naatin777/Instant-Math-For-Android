package com.naatin777.instantmath.di

import androidx.room.Room
import com.naatin777.instantmath.data.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "instant_math_db"
        ).build()
    }

    single { get<AppDatabase>().exampleDao() }
}
