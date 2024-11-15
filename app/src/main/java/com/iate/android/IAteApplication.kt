package com.iate.android

import android.app.Application
import com.iate.android.di.databaseModule
import com.iate.android.di.networkModule
import com.iate.android.di.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class IAteApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Start Koin
        startKoin {
            androidContext(this@IAteApplication)
            modules(
                listOf(networkModule, databaseModule, viewModelsModule)
            )
        }
    }
}