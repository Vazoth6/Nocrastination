// NoCrastinationApplication.kt
package pt.ipt.dam2025.nocrastination

import android.app.Application
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.AuthApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.interceptor.AuthInterceptor
import pt.ipt.dam2025.nocrastination.data.datasource.remote.interceptor.ConnectivityInterceptor
import pt.ipt.dam2025.nocrastination.data.repositories.AuthRepositoryImpl
import pt.ipt.dam2025.nocrastination.domain.repository.AuthRepository
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.AuthViewModel
import pt.ipt.dam2025.nocrastination.utils.PreferenceManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NoCrastinationApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@NoCrastinationApplication)
            modules(appModule)
        }
    }

    private val appModule = module {

        // Preference Manager
        single { PreferenceManager(get()) }

        // Interceptors
        single { AuthInterceptor(get()) }
        single { ConnectivityInterceptor(get()) }

        // OkHttpClient
        single {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(get<ConnectivityInterceptor>())
                .addInterceptor(get<AuthInterceptor>())
                .build()
        }

        // Retrofit
        single {
            Retrofit.Builder()
                .baseUrl("http://10.0.2.2:1337/")
                .client(get())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        // API Services
        single { get<Retrofit>().create(AuthApi::class.java) }

        // Repositories
        single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

        // ViewModels
        viewModel { AuthViewModel(get()) }
    }
}