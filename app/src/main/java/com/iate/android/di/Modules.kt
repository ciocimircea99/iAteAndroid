package com.iate.android.di

import androidx.room.Room
import com.iate.android.BuildConfig
import com.iate.android.data.database.AppDatabase
import com.iate.android.data.openai.OpenAIApi
import com.iate.android.ui.viewmodel.HistoryViewModel
import com.iate.android.ui.viewmodel.MainViewModel
import com.iate.android.ui.viewmodel.OnboardingViewModel
import com.iate.android.ui.viewmodel.SettingsViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val viewModelsModule = module {
    viewModel { MainViewModel(get(),get(),get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { HistoryViewModel(get(),get()) }
    viewModel { OnboardingViewModel() }
}

val databaseModule = module {
    // Provide the database instance
    single {
        Room.databaseBuilder(
            get(), // Get the context from Koin
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration().build()
    }

    // Provide FoodDao
    single {
        get<AppDatabase>().foodDao()
    }

    // Provide CalorieGoalsDao
    single {
        get<AppDatabase>().userSettingsDao()
    }
}


val networkModule = module {

    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    // Provide Retrofit instance
    single {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(get()) // Use OkHttpClient provided by Koin
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Provide OpenAIApi interface
    single {
        get<Retrofit>().create(OpenAIApi::class.java)
    }
}