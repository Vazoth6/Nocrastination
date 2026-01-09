package pt.ipt.dam2025.nocrastination.data.datasource.remote

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.auth0.android.jwt.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.AuthApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.api.TaskApi
import pt.ipt.dam2025.nocrastination.data.datasource.remote.interceptor.AuthInterceptor
import pt.ipt.dam2025.nocrastination.data.datasource.remote.interceptor.ConnectivityInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // CORREÇÃO: Use HTTP (não HTTPS) para desenvolvimento local
    private const val BASE_URL = "http://10.0.2.2:1337/"
    private var retrofit: Retrofit? = null

    // API Service instances
    private var authApi: AuthApi? = null
    private var taskApi: TaskApi? = null

    fun initialize(context: Context) {
        if (retrofit == null) {
            retrofit = buildRetrofit(context)
            Log.d("ApiClient", "✅ Retrofit inicializado com URL: $BASE_URL")
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        Log.d("ApiClient", "🔄 Construindo Retrofit...")

        // Gson configuration
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .serializeNulls()
            .create()

        // HTTP logging (only in debug mode)
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("API", message)
        }.apply {
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
            // Adicione isso para ignorar problemas de SSL em desenvolvimento
            .hostnameVerifier { _, _ -> true } // Apenas para desenvolvimento!
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
            Log.d("ApiClient", "✅ AuthApi criada")
        }
        return authApi!!
    }

    fun getTaskApi(context: Context): TaskApi {
        if (taskApi == null) {
            taskApi = getRetrofit(context).create(TaskApi::class.java)
            Log.d("ApiClient", "✅ TaskApi criada")
        }
        return taskApi!!
    }

    private fun getRetrofit(context: Context): Retrofit {
        if (retrofit == null) {
            retrofit = buildRetrofit(context)
        }
        return retrofit!!
    }

    // Metodo para testar a conexão
    fun testConnection() {
        Log.d("ApiClient", "🔗 Testando conexão com: $BASE_URL")
        Log.d("ApiClient", "📡 Endpoints disponíveis:")
        Log.d("ApiClient", "  - POST ${BASE_URL}api/auth/local")
        Log.d("ApiClient", "  - POST ${BASE_URL}api/auth/local/register")
        Log.d("ApiClient", "  - GET ${BASE_URL}api/users/me")
    }

    fun testTaskConnection(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val taskApi = getTaskApi(context)
                val response = taskApi.getTasks()

                Log.d("ApiClient", "🔍 Teste de tasks - Código: ${response.code()}")
                Log.d("ApiClient", "🔍 Teste de tasks - Sucesso: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d("ApiClient", "✅ ${it.data.size} tarefas encontradas")
                        it.data.forEach { taskData ->
                            Log.d("ApiClient", "   - ${taskData.id}: ${taskData.attributes.title}")
                        }
                    }
                } else {
                    Log.e("ApiClient", "❌ Erro: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ApiClient", "❌ Exceção: ${e.message}", e)
            }
        }
    }
}