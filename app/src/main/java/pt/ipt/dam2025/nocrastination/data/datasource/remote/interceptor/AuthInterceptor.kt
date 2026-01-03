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
        Log.d("AuthInterceptor", "🔄 Interceptando requisição para: $url")

        // Skip adding auth header for authentication endpoints
        if (url.contains("/api/auth/")) {
            Log.d("AuthInterceptor", "✅ Endpoint de auth, pulando token")
            return chain.proceed(originalRequest)
        }

        val token = preferenceManager.getAuthToken()

        Log.d("AuthInterceptor", "🔍 Token disponível: ${!token.isNullOrEmpty()}")

        return if (!token.isNullOrEmpty()) {
            val requestWithAuth = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()

            Log.d("AuthInterceptor", "✅ Adicionando token JWT à requisição")
            Log.d("AuthInterceptor", "🔐 Token (primeiros 20 chars): ${token.take(20)}...")

            chain.proceed(requestWithAuth)
        } else {
            Log.w("AuthInterceptor", "⚠️ Sem token JWT, enviando requisição sem autenticação")
            chain.proceed(originalRequest)
        }
    }
}