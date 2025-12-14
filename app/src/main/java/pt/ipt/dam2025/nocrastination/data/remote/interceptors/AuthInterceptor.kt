package pt.ipt.dam2025.nocrastination.data.remote.interceptors

import android.content.Context
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

        // Skip adding auth header for login/register endpoints
        if (originalRequest.url.encodedPath.contains("/auth/")) {
            return chain.proceed(originalRequest)
        }

        val token = preferenceManager.getAuthToken()

        return if (token != null) {
            val requestWithAuth = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(requestWithAuth)
        } else {
            chain.proceed(originalRequest)
        }
    }
}