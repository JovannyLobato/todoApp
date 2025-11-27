// com/example/todoapp/receiver/ReminderBroadcastReceiver.kt

package com.example.todoapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todoapp.NotificationHelper
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
        android.util.Log.d("PRUEBA_ALARMA", "¡El Receiver se activó! Recibiendo alarma...")
        val reminderId = intent.getIntExtra(REMINDER_ID, 0)
        val title = intent.getStringExtra(REMINDER_TITLE) ?: "Recordatorio"
        val reminderTimeMillis = intent.getLongExtra(REMINDER_TIME, 0L)

        val timeString = if (reminderTimeMillis > 0) {
            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(reminderTimeMillis))
        } else {
            "Hora desconocida"
        }

        val message = "¡Es hora de: $title!"

        NotificationHelper.showNotification(
            context,
            reminderId,
            "Recordatorio de ${timeString}",
            message
        )
        android.util.Log.d("PRUEBA_ALARMA", "Intentando mostrar notificación")

    }
}