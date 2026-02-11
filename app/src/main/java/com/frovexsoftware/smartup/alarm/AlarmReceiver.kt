package com.frovexsoftware.smartup.alarm

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import com.frovexsoftware.smartup.R
import com.frovexsoftware.smartup.ui.StopAlarmActivity

class AlarmReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        val ctx = context ?: return
        val alarmId = intent?.getIntExtra("alarm_id", -1) ?: -1
        val timeMillis = intent?.getLongExtra("time", -1L) ?: -1L
        val challengeType = intent?.getStringExtra("challenge_type")

        Toast.makeText(ctx, ctx.getString(R.string.alarm_ringing_toast), Toast.LENGTH_LONG).show()

        wakeScreen(ctx)
        startVibration(ctx)
        AlarmPlayer.start(ctx)
        AlarmNotifier.start(ctx, alarmId, timeMillis, challengeType)

        val fullScreenIntent = Intent(ctx, StopAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("alarm_id", alarmId)
            putExtra("time", timeMillis)
            putExtra("challenge_type", challengeType)
        }
        runCatching { ctx.startActivity(fullScreenIntent) }
    }

    private fun startVibration(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(800)
        }
    }

    private fun wakeScreen(context: Context) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        val wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "smartup:alarm_wake"
        )
        wakeLock.acquire(10_000L)
    }
}
