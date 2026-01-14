package pt.ipt.dam2025.nocrastination.data.datasource.remote.interceptor

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import okhttp3.Interceptor
import okhttp3.Response
import pt.ipt.dam2025.nocrastination.data.exceptions.NoInternetException
import java.io.IOException

class ConnectivityInterceptor(
    private val context: Context
) : Interceptor {

    // Anotação importante: requer permissão explícita no AndroidManifest.xml
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        // Verifica conectividade antes de qualquer requisição
        if (!isNetworkAvailable()) {
            // Lança exceção personalizada com mensagem amigável ao utilizador
            throw NoInternetException("Sem ligação à internet. Por favor verifique as suas definições de rede.")
        }
        return chain.proceed(chain.request())
    }

    // Metodo que verifica todos os tipos de conectividade disponíveis
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        // Verifica se há rede ativa
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        // Verifica múltiplos tipos de transporte
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
            else -> false
        }
    }
}