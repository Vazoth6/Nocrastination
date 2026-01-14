package pt.ipt.dam2025.nocrastination.data.datasource.remote.interceptor

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import pt.ipt.dam2025.nocrastination.utils.PreferenceManager
import java.io.IOException

class AuthInterceptor(
    private val context: Context
) : Interceptor {

    private val preferenceManager = PreferenceManager(context)

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val url = originalRequest.url.toString()

        // Log para debugging - importante em desenvolvimento mas pode ser removido em produção
        Log.d("AuthInterceptor", " A interceptar requisição para: $url")

        /*
        * Não adiciona token a endpoints de autenticação
        * Caso contrário, poderia causar loop infinito ou problemas de autenticação
        * */
        if (url.contains("/api/auth/")) {
            Log.d("AuthInterceptor", " Endpoint de auth, a puxar token")
            return chain.proceed(originalRequest)
        }

        // Obtém o token JWT armazenado localmente
        val token = preferenceManager.getAuthToken()

        Log.d("AuthInterceptor", " Token disponível: ${!token.isNullOrEmpty()}")

        return if (!token.isNullOrEmpty()) {
            // Adiciona o token JWT ao header Authorization seguindo o padrão Bearer
            val requestWithAuth = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()

            Log.d("AuthInterceptor", " A adicionar token JWT à requisição")

            // Log mostra apenas parte do token por segurança
            Log.d("AuthInterceptor", " Token (primeiros 20 chars): ${token.take(20)}...")

            chain.proceed(requestWithAuth)

        } else {
            /*
            * Caso não haja token, a requisição prossegue sem autenticação
            * O servidor responderá com 401 Unauthorized se o endpoint for protegido
            * */
            Log.w("AuthInterceptor", " Sem token JWT, a enviar requisição sem autenticação")
            chain.proceed(originalRequest)
        }
    }
}