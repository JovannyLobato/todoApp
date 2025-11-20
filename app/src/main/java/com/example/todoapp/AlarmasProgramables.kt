package com.example.todoapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.example.todoapp.receivers.AlarmasReceiver
import org.threeten.bp.LocalDateTime

data class AlarmItem(
    var message: String = "",
    var alarmTime: LocalDateTime = LocalDateTime.now()
)
interface AlarmScheduler{
    fun schedule(alarmItem: AlarmItem)
    fun cancel(alarmItem: AlarmItem)
}

/*
private var alarmMgr: AlarmManager? = null
private lateinit var alarmIntent: PendingIntent

class CalendarizarAlarmas(val ctx: Context) : AlarmScheduler{
    override fun schedule(alarmItem: AlarmItem) {
        val alarmMgr = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(ctx, AlarmasReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(ctx, 1001, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        alarmMgr?.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 60 * 1000,
            alarmIntent
        )
    }

    override fun cancel(alarmItem: AlarmItem) {
        TODO("Not yet implemented")
    }

}


fun AlarmasScreen(alarmScheduler: AlarmScheduler){
    val recordAudioPermissionState = rememberMultiplePremissionsStete(
        permissions = listOf(Manifest.permission.POST_NOTIFICATION)
        Manifest.permission.SCHEDULE_EXACT_ALARM
    )
    var AlarItem

    Button(OnClick = {
        alarmItem = if (BUILD.VERSION.SDK_INT >= Build.VERSION_CODES.o)
    })
}
*/