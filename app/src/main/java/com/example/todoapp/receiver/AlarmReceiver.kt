package com.example.todoapp.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.todoapp.MainActivity
import com.example.todoapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val REMINDER_ID = "reminder_id"
        const val REMINDER_TITLE = "reminder_title"
        const val REMINDER_TIME = "reminder_time"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra(REMINDER_ID, 0)
        val title = intent.getStringExtra(REMINDER_TITLE) ?: "Recordatorio"
        val reminderTimeMillis = intent.getLongExtra(REMINDER_TIME, 0L)
        val noteId = intent.getIntExtra("NOTE_ID", -1)

        val timeString = if (reminderTimeMillis > 0L) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(reminderTimeMillis))
        } else ""

        showNotification(context, title, "Hora de tu tarea ($timeString)", reminderId, noteId)
    }

    private fun showNotification(context: Context, title: String, message: String, notificationId: Int, noteId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "todo_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Notas",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Configurar el Intent para abir la nota que mando la notificacion
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Pasar el Id del MainActivity
            putExtra("NOTE_ID", noteId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId, // Usar ID único para que no se sobrescriban los intents
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}