package pt.ipt.dam2025.nocrastination.utils

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import pt.ipt.dam2025.nocrastination.domain.models.FocusLocation

// Gestor para operações de geofencing
class GeofencingManager(private val context: Context) {

    // Cliente para geofencing do Google Play Services
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    companion object {
        private const val TAG = "GeofencingManager"
        private const val GEOFENCE_RADIUS_DEFAULT = 100f // metros
        private const val GEOFENCE_EXPIRATION = Geofence.NEVER_EXPIRE
        private const val GEOFENCE_LOITERING_DELAY = 5000L // 5 segundos
        private const val PENDING_INTENT_REQUEST_CODE = 100
    }

    // PendingIntent lazy para o BroadcastReceiver
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            PENDING_INTENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Adicionar geofences para todas as localizações de foco
    fun addGeofencesForFocusLocations(locations: List<FocusLocation>) {
        if (!hasLocationPermissions()) {
            Log.w(TAG, "Sem permissões de localização")
            return
        }

        // Filtrar apenas localizações ativas
        val enabledLocations = locations.filter { it.enabled }
        if (enabledLocations.isEmpty()) {
            Log.d(TAG, "Nenhuma localização de foco ativa")
            return
        }

        // Remover todos os geofences antigos primeiro
        removeAllGeofences()

        // Criar lista de geofences
        val geofences = enabledLocations.map { focusLocation ->
            Geofence.Builder()
                .setRequestId("focus_location_${focusLocation.id}")
                .setCircularRegion(
                    focusLocation.latitude,
                    focusLocation.longitude,
                    focusLocation.radius
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .setLoiteringDelay(GEOFENCE_LOITERING_DELAY.toInt())
                .build()
        }

        // Construir pedido de geofencing
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        // Verificar permissões antes de adicionar
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Permissão de localização não concedida")
            return
        }

        // Adicionar geofences ao cliente
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d(TAG, "Geofences adicionados com sucesso para ${enabledLocations.size} localizações")
            }
            addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao adicionar geofences", exception)
            }
        }
    }

    // Remover todos os geofences
    fun removeAllGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d(TAG, "Todos os geofences removidos")
            }
            addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao remover geofences", exception)
            }
        }
    }

    // Remover um geofence específico
    fun removeGeofence(locationId: Int) {
        val geofenceRequestId = "focus_location_$locationId"
        geofencingClient.removeGeofences(listOf(geofenceRequestId)).run {
            addOnSuccessListener {
                Log.d(TAG, "Geofence removido: $locationId")
            }
            addOnFailureListener { exception ->
                Log.e(TAG, "Erro ao remover geofence: $locationId", exception)
            }
        }
    }

    // Verificar se tem permissões de localização
    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}