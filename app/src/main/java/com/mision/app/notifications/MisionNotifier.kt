package com.mision.app.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mision.app.R
import com.mision.app.presentation.MainActivity

/**
 * Posts the daily reminder.
 *
 * It degrades gracefully: if permission is missing or notifications are
 * disabled nothing is posted and no exception escapes.
 */
class MisionNotifier(private val context: Context) {

    /**
     * Morning / afternoon / evening reminder with copy chosen by time of day.
     * [pendingMissions] is null when the count could not be read.
     */
    fun showDailyReminder(pendingMissions: Int?, hour: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val (title, body) = reminderCopy(pendingMissions, hour)
        val openApp = PendingIntent.getActivity(
            context,
            REMINDER_ID,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.brand_violet))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        // Covers the race where the permission is revoked between check and post.
        runCatching { manager.notify(REMINDER_ID, notification) }
    }

    companion object {
        const val REMINDER_ID = 2000

        fun reminderCopy(pendingMissions: Int?, hour: Int): Pair<String, String> = when {
            hour < 12 || pendingMissions == null ->
                "Tus misiones te esperan" to "Tu mascota te está esperando. Revisa tus misiones del día."
            pendingMissions <= 0 -> "¡Día completado!" to "Has cerrado todas tus misiones de hoy. ¡Buen trabajo!"
            hour <= 18 -> "Todavía tienes misiones pendientes" to pendingLabel(pendingMissions)
            else -> "Completa tus misiones para mantener tu racha" to pendingLabel(pendingMissions)
        }

        private fun pendingLabel(pending: Int): String =
            if (pending == 1) "Te queda 1 misión para cerrar el día."
            else "Te quedan $pending misiones para cerrar el día."
    }
}
