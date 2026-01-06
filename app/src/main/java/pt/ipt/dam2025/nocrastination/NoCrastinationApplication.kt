// NoCrastinationApplication.kt
package pt.ipt.dam2025.nocrastination

import android.app.Application
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.AuthApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.PomodoroApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.TaskApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.UserProfileApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.interceptor.AuthInterceptor
import pt.ipt.dam2025.nocrastination.data.datasource.remote.interceptor.ConnectivityInterceptor
import pt.ipt.dam2025.nocrastination.data.mapper.PomodoroMapper
import pt.ipt.dam2025.nocrastination.data.mapper.TaskMapper
import pt.ipt.dam2025.nocrastination.data.mapper.UserProfileMapper
import pt.ipt.dam2025.nocrastination.data.repositories.AuthRepositoryImpl
import pt.ipt.dam2025.nocrastination.data.repositories.PomodoroRepositoryImpl
import pt.ipt.dam2025.nocrastination.data.repositories.TaskRepositoryImpl
import pt.ipt.dam2025.nocrastination.data.repositories.UserProfileRepositoryImpl
import pt.ipt.dam2025.nocrastination.domain.repository.AuthRepository
import pt.ipt.dam2025.nocrastination.domain.repository.PomodoroRepository
import pt.ipt.dam2025.nocrastination.domain.repository.TaskRepository
import pt.ipt.dam2025.nocrastination.domain.repository.UserProfileRepository
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.AuthViewModel
import pt.ipt.dam2025.nocrastination.presentation.viewmodel.PomodoroViewModel
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.TasksViewModel
import pt.ipt.dam2025.nocrastination.presentations.viewmodel.UserProfileViewModel
import pt.ipt.dam2025.nocrastination.utils.PreferenceManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NoCrastinationApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("NoCrastinationApp", "🚀 Aplicação iniciando...")

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

        Log.d("NoCrastinationApp", "✅ Koin inicializado")
    }

    // Módulo de aplicação (preferences, interceptors)
    private val appModule = module {
        single { PreferenceManager(get()) }
        single { AuthInterceptor(get()) }
        single { ConnectivityInterceptor(get()) }
        single { TaskMapper() }
        single  { PomodoroMapper() }
        single { UserProfileMapper }
    }

    // Módulo de API (Retrofit, APIs)
    private val apiModule = module {
        single {
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                })
                .addInterceptor(get<ConnectivityInterceptor>())
                .addInterceptor(get<AuthInterceptor>())
                .hostnameVerifier { _, _ -> true } // Para desenvolvimento
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
        single { get<Retrofit>().create(PomodoroApi::class.java) }
        single { get<Retrofit>().create(UserProfileApi::class.java) }
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
        single<PomodoroRepository> {  // ADICIONAR
            PomodoroRepositoryImpl(
                pomodoroApi = get(),
                pomodoroMapper = get()
            )
        }
        single<UserProfileRepository> {
            UserProfileRepositoryImpl(
                userProfileApi = get(),
                userProfileMapper = get()
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
                taskRepository = get()
            )
        }
        viewModel {
            PomodoroViewModel(
                pomodoroRepository = get()
            )
        }
        viewModel {
            UserProfileViewModel(
                userProfileRepository = get(),
                authRepository = get()
            )
        }
    }
}