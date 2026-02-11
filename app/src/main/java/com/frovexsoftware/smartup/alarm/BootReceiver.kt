package com.frovexsoftware.smartup.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val alarms = AlarmStorage.load(context)
            AlarmScheduler.rescheduleAll(context, alarms)
            AlarmStorage.save(context, alarms)
        }
    }
}
