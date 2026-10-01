package com.mision.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/** Notification channels created once, at app start. */
object NotificationChannels {

    const val REMINDERS = "mision_recordatorios"

    /**
     * Channel of the former in-app celebration notifications. They always
     * duplicated the celebration dialog on screen, so the channel is removed
     * from existing installs.
     */
    private const val LEGACY_CELEBRATIONS = "mision_celebraciones"

    fun ensure(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                REMINDERS,
                "Recordatorio diario",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Un aviso al día para que no pierdas tu racha"
            },
        )
        manager.deleteNotificationChannel(LEGACY_CELEBRATIONS)
    }
}
