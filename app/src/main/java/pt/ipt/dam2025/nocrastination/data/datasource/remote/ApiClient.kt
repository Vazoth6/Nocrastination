package pt.ipt.dam2025.nocrastination.data.datasource.remote

import android.content.Context
import com.auth0.android.jwt.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.AuthApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.TaskApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.interceptor.AuthInterceptor
import pt.ipt.dam2025.nocrastination.data.datasource.remote.interceptor.ConnectivityInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://10.0.0.2:1337/api/" // Alterar isto em produção
    private var retrofit: Retrofit? = null

    // API Service instances
    private var authApi: AuthApi? = null
    private var taskApi: TaskApi? = null

    fun initialize(context: Context) {
        if (retrofit == null) {
            retrofit = buildRetrofit(context)
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        // Gson configuration
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .serializeNulls()
            .create()

        // HTTP logging (only in debug mode)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // Create OkHttpClient with interceptors
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(ConnectivityInterceptor(context))
            .addInterceptor(AuthInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Lazy initialization of API services
    fun getAuthApi(context: Context): AuthApi {
        if (authApi == null) {
            authApi = getRetrofit(context).create(AuthApi::class.java)
        }
        return authApi!!
    }

    fun getTaskApi(context: Context): TaskApi {
        if (taskApi == null) {
            taskApi = getRetrofit(context).create(TaskApi::class.java)
        }
        return taskApi!!
    }

    private fun getRetrofit(context: Context): Retrofit {
        if (retrofit == null) {
            retrofit = buildRetrofit(context)
        }
        return retrofit!!
    }

    // Helper method to update base URL (for testing/debugging)
    fun updateBaseUrl(newBaseUrl: String, context: Context) {
        retrofit = null
        authApi = null
        taskApi = null

        // Reinitialize with new URL
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(ConnectivityInterceptor(context))
            .addInterceptor(AuthInterceptor(context))
            .build()

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .serializeNulls()
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl(newBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}