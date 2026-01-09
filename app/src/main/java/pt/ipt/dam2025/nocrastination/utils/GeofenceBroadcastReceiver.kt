package pt.ipt.dam2025.nocrastination.utils.geofencing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import pt.ipt.dam2025.nocrastination.utils.NotificationHelper

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Log.e("GeofenceReceiver", "GeofencingEvent é nulo")
            return
        }

        if (geofencingEvent.hasError()) {
            val errorMessage = when (geofencingEvent.errorCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence não disponível"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Demasiados geofences"
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Demasiados pending intents"
                else -> "Erro desconhecido: ${geofencingEvent.errorCode}"
            }
            Log.e("GeofenceReceiver", "Erro no Geofencing: $errorMessage")
            return
        }

        // Obter transição
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Obter geofences que ativaram
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        triggeringGeofences?.forEach { geofence ->
            val requestId = geofence.requestId

            when (geofenceTransition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    Log.d("GeofenceReceiver", "ENTROU na zona: $requestId")
                    // Mostrar notificação motivacional
                    showEnterNotification(context, requestId)
                }

                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    Log.d("GeofenceReceiver", "SAIU da zona: $requestId")
                    // Podes adicionar uma notificação de saída se quiseres
                    // showExitNotification(context, requestId)
                }

                Geofence.GEOFENCE_TRANSITION_DWELL -> {
                    Log.d("GeofenceReceiver", "PERMANECEU na zona: $requestId")
                }
            }
        }
    }

    private fun showEnterNotification(context: Context, requestId: String) {
        // Extrair ID da localização do requestId (formato: "focus_location_ID")
        val locationId = requestId.removePrefix("focus_location_").toIntOrNull() ?: return

        // Por enquanto, usamos uma mensagem genérica
        // Em produção, poderias buscar os dados da localização de uma base de dados local
        val title = "Zona de Produtividade 🎯"
        val message = "Vamos pôr as mãos ao trabalho! Esta é a área ideal para focar."

        showNotification(context, title, message)
    }

    private fun showNotification(context: Context, title: String, message: String) {
        NotificationHelper(context).showProductivityNotification(title, message)
    }
}

// Classe para códigos de status do Geofence (não disponível no SDK principal)
object GeofenceStatusCodes {
    const val GEOFENCE_NOT_AVAILABLE = 1000
    const val GEOFENCE_TOO_MANY_GEOFENCES = 1001
    const val GEOFENCE_TOO_MANY_PENDING_INTENTS = 1002
}