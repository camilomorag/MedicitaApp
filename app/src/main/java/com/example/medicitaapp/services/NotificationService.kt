package com.example.medicitaapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.medicitaapp.MainActivity

class NotificationService(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "medicita_notifications"
        const val CHANNEL_NAME = "Medicita Notificaciones"

        var unreadCount = 0
            private set

        fun incrementUnreadCount() {
            unreadCount++
        }

        fun resetUnreadCount() {
            unreadCount = 0
        }
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de fórmulas médicas y estados"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Verificar si tenemos permiso para mostrar notificaciones
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // En versiones anteriores a Android 13 no se necesita permiso
        }
    }

    fun showFormulaSubmittedNotification(medicamento: String) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "notifications")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("📄 Fórmula enviada")
            .setContentText("Tu fórmula para $medicamento ha sido enviada al farmaceuta")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(1001, notification)
        incrementUnreadCount()
    }

    fun showFormulaStatusNotification(
        estado: String,
        medicamento: String,
        turno: String = "",
        ubicacion: String = ""
    ) {
        if (!hasNotificationPermission()) return

        val (title, message) = when (estado) {
            "aceptada" -> Pair(
                "✅ Fórmula aceptada",
                "Tu fórmula para $medicamento fue aceptada${if (turno.isNotBlank()) ". Turno: $turno" else ""}"
            )
            "rechazada" -> Pair(
                "❌ Fórmula rechazada",
                "Tu fórmula para $medicamento fue rechazada. Comunícate con el farmaceuta."
            )
            "aplazada" -> Pair(
                "⏰ Fórmula aplazada",
                "Tu fórmula para $medicamento fue aplazada. Espera actualizaciones."
            )
            "lista" -> Pair(
                "🎉 Medicamento listo",
                "Tu medicamento $medicamento está listo para recoger. Ubicación: $ubicacion"
            )
            else -> Pair(
                "📋 Actualización",
                "Tu fórmula para $medicamento ha sido actualizada a: $estado"
            )
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "notifications")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(1002, notification)
        incrementUnreadCount()
    }

    fun showNewFormulaForPharmacistNotification(patientName: String) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "pharmacist_requests")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("👨‍⚕️ Nueva fórmula pendiente")
            .setContentText("$patientName ha enviado una nueva fórmula médica")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(1003, notification)
        incrementUnreadCount()
    }

    fun clearNotifications() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()
        resetUnreadCount()
    }
}