package com.example.todoapp.util

import android.R.attr.data
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.todoapp.model.Reminder
import com.example.todoapp.receiver.AlarmReceiver

object AlarmScheduler {

    fun scheduleReminder(context: Context, reminder: Reminder, noteTitle: String) {
        val requestCode = reminder.id
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.REMINDER_ID, reminder.id)
            putExtra(AlarmReceiver.REMINDER_TITLE, noteTitle)
            putExtra(AlarmReceiver.REMINDER_TIME, reminder.reminderTime)
            putExtra("NOTE_ID", reminder.noteId)
            data = android.net.Uri.parse("content://receiver/${reminder.id}")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = reminder.reminderTime

        if (triggerTime > System.currentTimeMillis()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // Para Android 5.0 y 5.1 (API 21 y 22)
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun cancelReminder(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            data = android.net.Uri.parse("content://receiver/$reminderId")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}