// NoCrastinationApplication.kt
package pt.ipt.dam2025.nocrastination

import android.app.Application
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.AuthApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.TaskApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.interceptor.AuthInterceptor
import pt.ipt.dam2025.nocrastination.data.datasource.remote.interceptor.ConnectivityInterceptor
import pt.ipt.dam2025.nocrastination.data.mapper.TaskMapper
import pt.ipt.dam2025.nocrastination.data.repositories.AuthRepositoryImpl
import pt.ipt.dam2025.nocrastination.data.repositories.TaskRepositoryImpl
import pt.ipt.dam2025.nocrastination.domain.repository.AuthRepository
import pt.ipt.dam2025.nocrastination.domain.repository.TaskRepository
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.AuthViewModel
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.TasksViewModel
import pt.ipt.dam2025.nocrastination.utils.PreferenceManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NoCrastinationApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@NoCrastinationApplication)
            modules(
                listOf(
                    appModule,
                    apiModule,
                    repositoryModule,
                    viewModelModule
                )
            )
        }
    }

    // Módulo de aplicação (preferences, interceptors)
    private val appModule = module {
        single { PreferenceManager(get()) }
        single { AuthInterceptor(get()) }
        single { ConnectivityInterceptor(get()) }
        single { TaskMapper() }
    }

    // Módulo de API (Retrofit, APIs)
    private val apiModule = module {
        single {
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .addInterceptor(get<ConnectivityInterceptor>())
                .addInterceptor(get<AuthInterceptor>())
                .build()
        }

        single {
            Retrofit.Builder()
                .baseUrl("http://10.0.2.2:1337/")
                .client(get())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        single { get<Retrofit>().create(AuthApi::class.java) }
        single { get<Retrofit>().create(TaskApi::class.java) }
    }

    // Módulo de repositórios
    private val repositoryModule = module {
        single<AuthRepository> {
            AuthRepositoryImpl(
                authApi = get(),
                preferenceManager = get()
            )
        }
        single<TaskRepository> {
            TaskRepositoryImpl(
                taskApi = get(),
                taskMapper = get()
            )
        }
    }

    // Módulo de ViewModels
    private val viewModelModule = module {
        viewModel {
            AuthViewModel(
                authRepository = get()
            )
        }
        viewModel {
            TasksViewModel(
                tasksRepository = get()
            )
        }
    }
}