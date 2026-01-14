package pt.ipt.dam2025.nocrastination.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import pt.ipt.dam2025.nocrastination.R

// Classe auxiliar para gerir notificações
class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "productivity_channel"
        private const val CHANNEL_NAME = "Notificações de Produtividade"
        private const val NOTIFICATION_ID = 1001
    }

    // Inicializador para criar o canal de notificação
    init {
        createNotificationChannel()
    }

    // Criar canal de notificação (necessário para Android 8.0+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações quando entrares em zonas de foco"
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Mostrar notificação de produtividade
    fun showProductivityNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Cria este ícone em res/drawable/
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}