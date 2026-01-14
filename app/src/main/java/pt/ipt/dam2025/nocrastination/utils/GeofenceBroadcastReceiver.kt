package pt.ipt.dam2025.nocrastination.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

// Receptor de broadcast para processar eventos de geofence
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    // Função chamado quando um broadcast é recebido
    override fun onReceive(context: Context, intent: Intent) {
        // Extrair evento de geofencing do intent
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // Verificar se o evento é nulo
        if (geofencingEvent == null) {
            Log.e("GeofenceReceiver", "GeofencingEvent é nulo")
            return
        }

        // Verificar se houve erro no evento
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

        // Obter tipo de transição
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Obter lista de geofences que foram ativados
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        // Processar cada geofence ativado
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
                }

                Geofence.GEOFENCE_TRANSITION_DWELL -> {
                    Log.d("GeofenceReceiver", "PERMANECEU na zona: $requestId")
                }
            }
        }
    }

    // Mostrar notificação de entrada numa zona
    private fun showEnterNotification(context: Context, requestId: String) {
        // Extrair ID da localização do requestId (formato: "focus_location_ID")
        val locationId = requestId.removePrefix("focus_location_").toIntOrNull() ?: return

        // Mensagem genérica para a notificação
        val title = "Zona de Produtividade"
        val message = "Vamos pôr as mãos ao trabalho! Esta é a área ideal para se focar."

        showNotification(context, title, message)
    }

    // Metodo auxiliar para mostrar notificação
    private fun showNotification(context: Context, title: String, message: String) {
        NotificationHelper(context).showProductivityNotification(title, message)
    }
}

// Objeto com códigos de status para geofence
object GeofenceStatusCodes {
    const val GEOFENCE_NOT_AVAILABLE = 1000
    const val GEOFENCE_TOO_MANY_GEOFENCES = 1001
    const val GEOFENCE_TOO_MANY_PENDING_INTENTS = 1002
}