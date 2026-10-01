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
                "Recordatorios diarios",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Avisos para que no pierdas tu racha"
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
