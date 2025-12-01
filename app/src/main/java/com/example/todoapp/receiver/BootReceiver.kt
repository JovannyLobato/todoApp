package com.example.todoapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.todoapp.TodoApplication
import com.example.todoapp.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            // Usamos goAsync porque vamos a acceder a la base de datos (operación lenta)
            val pendingResult = goAsync()

            // Accedemos a la aplicación para obtener la base de datos
            val app = context.applicationContext as TodoApplication
            val noteDao = app.database.noteDao() // Acceso directo al DAO

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Obtenemos TODOS los recordatorios
                    val reminders = noteDao.getAllReminders()
                    val now = System.currentTimeMillis()

                    // 2. Recorremos y reprogramamos SOLO los que son a futuro
                    reminders.forEach { reminder ->
                        if (reminder.reminderTime > now) {
                            // Necesitamos el título de la nota para la notificación.
                            // Hacemos una consulta rápida para obtener la nota de este recordatorio
                            val note = noteDao.getNoteWithDetails(reminder.noteId)?.note
                            val title = note?.title?.ifEmpty { "Tarea pendiente" } ?: "Recordatorio"

                            // Reprogramamos usando tu AlarmScheduler existente
                            AlarmScheduler.scheduleReminder(context, reminder, title)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    // Importante: Avisamos al sistema que terminamos
                    pendingResult.finish()
                }
            }
        }
    }
}