package com.frovexsoftware.smartup

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import java.util.Calendar

object AlarmScheduler {

    fun schedule(context: Context, alarm: AlarmData, promptExactPermission: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, alarm)

        val needsExactPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()

        if (needsExactPermission && promptExactPermission && context is Activity) {
            requestExactAlarmPermission(context)
        }

        val triggerMillis = alarm.timeInMillis
        try {
            val clockInfo = AlarmManager.AlarmClockInfo(triggerMillis, pendingIntent)
            alarmManager.setAlarmClock(clockInfo, pendingIntent)
        } catch (e: SecurityException) {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        }
    }

    fun cancel(context: Context, alarm: AlarmData) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val existing = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        existing?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    fun rescheduleAll(context: Context, alarms: List<AlarmData>) {
        alarms.filter { it.enabled }.forEach { alarm ->
            // Ensure stored time aligns to next trigger considering date/weekdays if present.
            val recomputed = recomputeNextTrigger(alarm)
            alarm.timeInMillis = recomputed.timeInMillis
            schedule(context, alarm, promptExactPermission = false)
        }
    }

    private fun recomputeNextTrigger(alarm: AlarmData): AlarmData {
        val cal = Calendar.getInstance().apply { timeInMillis = alarm.timeInMillis }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val next = TimeLogic.calculateNextTrigger(hour, minute, alarm.weekdays, alarm.dateMillis)
        alarm.timeInMillis = next.timeInMillis
        return alarm
    }

    private fun buildPendingIntent(context: Context, alarm: AlarmData): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("time", alarm.timeInMillis)
            putExtra("challenge_type", alarm.challengeType)
        }
        return PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun requestExactAlarmPermission(activity: Activity) {
        val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = android.net.Uri.parse("package:${activity.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { activity.startActivity(intent) }
            .onFailure {
                val fallback = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:${activity.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                runCatching { activity.startActivity(fallback) }
            }
    }
}
