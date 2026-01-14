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

// Classe Singleton que centraliza toda a configuração da API
object ApiClient {

    /*
    * URL base para desenvolvimento local
    * Usa HTTP (não HTTPS) para desenvolvimento local
    * 10.0.2.2 é o IP especial do Android para localhost do emulador
    * Porta 1337 é a porta padrão do Strapi
    * */
    private const val BASE_URL = "http://10.0.2.2:1337/"

    // Instância singleton do Retrofit
    private var retrofit: Retrofit? = null

    // Instâncias lazy das APIs (padrão Singleton)
    private var authApi: AuthApi? = null
    private var taskApi: TaskApi? = null

    // Metodo de inicialização explícito que deve ser chamado no Application ou Activity
    fun initialize(context: Context) {
        if (retrofit == null) {
            retrofit = buildRetrofit(context)
            Log.d("ApiClient", " Retrofit a inicializar com URL: $BASE_URL")
        }
    }

    // Constrói a configuração completa do Retrofit
    private fun buildRetrofit(context: Context): Retrofit {
        Log.d("ApiClient", "🔄 A construir Retrofit...")

        /*
        * Configuração do Gson para serialização/desserialização JSON
        * Formato de data compatível com Strapi/ISO 8601
        * */
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .serializeNulls()
            .create()

        // Interceptor de logging - apenas em modo DEBUG
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("API", message) // Log detalhado de todas as requisições/respostas
        }.apply {
            // Nível BODY mostra tudo (headers, body), apenas para desenvolvimento
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE // Desligado em produção
            }
        }

        // Construção do cliente HTTP com todas as configurações
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Timeout de conexão
            .readTimeout(30, TimeUnit.SECONDS) // Timeout de leitura
            .writeTimeout(30, TimeUnit.SECONDS) // Timeout de escrita

            // Adição de interceptors por ordem de importância:
            .addInterceptor(loggingInterceptor)
            .addInterceptor(ConnectivityInterceptor(context))
            .addInterceptor(AuthInterceptor(context))
            // Configuração apenas para desenvolvimento, não aplicar em produção
            .hostnameVerifier { _, _ -> true } // Ignora verificação SSL
            .build()

        // Construção final do Retrofit
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Inicialização lazy (sob demanda) da AuthApi
    fun getAuthApi(context: Context): AuthApi {
        if (authApi == null) {
            authApi = getRetrofit(context).create(AuthApi::class.java)
            Log.d("ApiClient", " AuthApi criada")
        }
        return authApi!!
    }

    // Inicialização lazy (sob demanda) da TaskApi
    fun getTaskApi(context: Context): TaskApi {
        if (taskApi == null) {
            taskApi = getRetrofit(context).create(TaskApi::class.java)
            Log.d("ApiClient", " TaskApi criada")
        }
        return taskApi!!
    }

    // Obtém ou constrói a instância do Retrofit
    private fun getRetrofit(context: Context): Retrofit {
        if (retrofit == null) {
            retrofit = buildRetrofit(context)
        }
        return retrofit!!
    }

    // Metodo de debug para testar a configuração da conexão
    fun testConnection() {
        Log.d("ApiClient", " A testar conexão com: $BASE_URL")
        Log.d("ApiClient", " Endpoints disponíveis:")
        Log.d("ApiClient", "  - POST ${BASE_URL}api/auth/local")
        Log.d("ApiClient", "  - POST ${BASE_URL}api/auth/local/register")
        Log.d("ApiClient", "  - GET ${BASE_URL}api/users/me")
    }

    // Teste real da conexão a realizar uma requisição real
    fun testTaskConnection(context: Context) {

        // Usa CoroutineScope próprio para não bloquear UI thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val taskApi = getTaskApi(context)
                val response = taskApi.getTasks()

                Log.d("ApiClient", " Teste de tasks - Código: ${response.code()}")
                Log.d("ApiClient", " Teste de tasks - Sucesso: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    response.body()?.let {
                        Log.d("ApiClient", " ${it.data.size} tarefas encontradas")
                        it.data.forEach { taskData ->
                            Log.d("ApiClient", "   - ${taskData.id}: ${taskData.attributes.title}")
                        }
                    }
                } else {
                    // Log do erro completo
                    Log.e("ApiClient", " Erro: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ApiClient", " Exceção: ${e.message}", e)
            }
        }
    }
}