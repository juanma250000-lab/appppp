package com.mision.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/** Notification channels created once, at app start. */
object NotificationChannels {

    const val REMINDERS = "mision_recordatorios"
    const val CELEBRATIONS = "mision_celebraciones"

    fun ensure(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannel(
            NotificationChannel(
                REMINDERS,
                "Recordatorios",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Aviso diario y recordatorios de tus misiones"
                enableVibration(true)
            },
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CELEBRATIONS,
                "Logros y rachas",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Hitos, niveles y recompensas desbloqueadas"
                enableVibration(true)
            },
        )
    }
}
