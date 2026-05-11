package com.naatin777.instantmath

import android.app.Application
import com.naatin777.instantmath.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class InstantMathApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@InstantMathApplication)
            modules(appModule)
        }
    }
}
