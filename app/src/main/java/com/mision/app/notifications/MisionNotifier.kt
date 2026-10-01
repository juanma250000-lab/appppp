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
import com.mision.app.core.time.SpanishLocale
import com.mision.app.presentation.MainActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Small wrapper around the platform notification APIs.
 *
 * Every call degrades gracefully: if permission is missing or notifications are
 * disabled nothing is posted and no exception escapes.
 */
class MisionNotifier(private val context: Context) {

    private val manager get() = NotificationManagerCompat.from(context)

    private fun baseBuilder(channelId: String, id: Int): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    /** Morning / afternoon / evening reminder with copy chosen by time of day. */
    fun showDailyReminder(pendingMissions: Int, hour: Int) {
        val (title, body) = reminderCopy(pendingMissions, hour)
        notify(
            builder = baseBuilder(NotificationChannels.REMINDERS, REMINDER_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body)),
            id = REMINDER_ID,
        )
    }

    fun showStreakMilestone(days: Int) {
        val id = 3000 + days
        val body = "Has desbloqueado una nueva recompensa por mantener tu racha."
        notify(
            builder = baseBuilder(NotificationChannels.CELEBRATIONS, id)
                .setContentTitle("¡Racha de $days días!")
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body)),
            id = id,
        )
    }

    fun showAchievement(name: String) {
        val id = 4000 + (name.hashCode() and 0x3FF)
        notify(
            builder = baseBuilder(NotificationChannels.CELEBRATIONS, id)
                .setContentTitle("¡Logro desbloqueado!")
                .setContentText(name)
                .setStyle(NotificationCompat.BigTextStyle().bigText(name)),
            id = id,
        )
    }

    fun showLevelUp(level: Int) {
        val id = 5000 + level
        notify(
            builder = baseBuilder(NotificationChannels.CELEBRATIONS, id)
                .setContentTitle("¡Nuevo nivel!")
                .setContentText("Has alcanzado el nivel $level. Tu mascota está orgullosa de ti."),
            id = id,
        )
    }

    private fun notify(builder: NotificationCompat.Builder, id: Int) {
        // Single gate for every post. The permission check sits inline so lint
        // can prove it is performed: below Android 13 POST_NOTIFICATIONS does
        // not exist, so only the user setting is honoured there. runCatching
        // covers the remaining race where permission is revoked mid-post.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (!manager.areNotificationsEnabled()) return
        runCatching { manager.notify(id, builder.build()) }
    }

    companion object {
        const val REMINDER_ID = 2000

        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", SpanishLocale)

        /** Visible clock label, mostly useful for tests and debugging. */
        fun formatTime(dateTime: LocalDateTime): String = timeFormatter.format(dateTime)

        fun reminderCopy(pendingMissions: Int, hour: Int): Pair<String, String> = when {
            hour < 12 -> "Buenos días" to "Tu mascota te está esperando. Revisa tus misiones del día."
            hour in 12..18 && pendingMissions > 0 ->
                "Todavía tienes misiones pendientes" to
                    "Te quedan $pendingMissions misiones para cerrar el día."
            hour in 12..18 -> "¡Buen progreso!" to "Ya tienes todo al día. Descansa un poco."
            else -> "Completa tus misiones para mantener tu racha" to
                "Te queda poco para cerrar el día. ¡Tú puedes!"
        }
    }
}
