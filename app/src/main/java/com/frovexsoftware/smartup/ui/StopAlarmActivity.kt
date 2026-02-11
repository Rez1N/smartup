package com.frovexsoftware.smartup.ui

import android.os.Bundle
import android.os.Build
import android.view.WindowManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.frovexsoftware.smartup.R
import com.frovexsoftware.smartup.alarm.AlarmData
import com.frovexsoftware.smartup.alarm.AlarmNotifier
import com.frovexsoftware.smartup.alarm.AlarmPlayer
import com.frovexsoftware.smartup.alarm.AlarmScheduler
import com.frovexsoftware.smartup.challenge.ChallengeCallback
import com.frovexsoftware.smartup.challenge.ChallengeType
import com.frovexsoftware.smartup.challenge.ColorChallengeFragment
import com.frovexsoftware.smartup.challenge.DateChallengeFragment
import com.frovexsoftware.smartup.challenge.DotsChallengeFragment
import com.frovexsoftware.smartup.challenge.HoldChallengeFragment
import com.frovexsoftware.smartup.challenge.MathChallengeFragment
import com.frovexsoftware.smartup.challenge.ShakeChallengeFragment
import com.frovexsoftware.smartup.challenge.SnakeChallengeFragment

open class StopAlarmActivity : AppCompatActivity(), ChallengeCallback {

    private lateinit var noChallengeContainer: View
    private lateinit var fragmentContainer: View
    private lateinit var btnStop: MaterialButton
    private lateinit var btnSnooze: MaterialButton

    private var alarmId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_alarm)

        alarmId = intent.getIntExtra("alarm_id", -1)
        noChallengeContainer = findViewById(R.id.no_challenge_container)
        fragmentContainer = findViewById(R.id.fragment_container)
        btnStop = findViewById(R.id.btn_stop_alarm)
        btnSnooze = findViewById(R.id.btn_snooze_alarm)

        val challengeType = ChallengeType.from(intent.getStringExtra("challenge_type"))

        val fragment = when (challengeType) {
            ChallengeType.MATH -> MathChallengeFragment()
            ChallengeType.DATE -> DateChallengeFragment()
            ChallengeType.COLOR -> ColorChallengeFragment()
            ChallengeType.DOTS -> DotsChallengeFragment()
            ChallengeType.SNAKE -> SnakeChallengeFragment()
            ChallengeType.SHAKE -> ShakeChallengeFragment()
            ChallengeType.HOLD -> HoldChallengeFragment()
            else -> null
        }

        if (fragment != null) {
            noChallengeContainer.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } else {
            fragmentContainer.visibility = View.GONE
            noChallengeContainer.visibility = View.VISIBLE

            btnStop.setOnClickListener { onChallengeCompleted() }
            btnSnooze.setOnClickListener { onSnoozeRequested() }
        }
    }

    override fun onChallengeCompleted() {
        AlarmPlayer.stop()
        AlarmNotifier.stop(this)
        finish()
    }

    private fun onSnoozeRequested() {
        scheduleSnooze()
        AlarmPlayer.stop()
        AlarmNotifier.stop(this)
        finish()
    }

    private fun scheduleSnooze() {
        val now = System.currentTimeMillis()
        val snoozeMillis = now + SNOOZE_MINUTES * 60_000L
        val id = if (alarmId != -1) alarmId else ((now % Int.MAX_VALUE).toInt())
        val alarm = AlarmData(
            id = id,
            timeInMillis = snoozeMillis,
            weekdays = emptySet(),
            dateMillis = null,
            enabled = true,
            description = "",
            challengeType = ChallengeType.NONE.name
        )
        AlarmScheduler.schedule(this, alarm, promptExactPermission = false)
    }

    private companion object {
        const val SNOOZE_MINUTES = 10
    }
}
