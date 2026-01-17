package com.frovexsoftware.smartup

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class StopAlarmActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_alarm)

        val challengeType = ChallengeType.from(intent.getStringExtra("challenge_type"))

        val fragment = when (challengeType) {
            ChallengeType.MATH -> MathChallengeFragment()
            ChallengeType.DATE -> DateChallengeFragment()
            ChallengeType.COLOR -> ColorChallengeFragment()
            ChallengeType.DOTS -> DotsChallengeFragment()
            ChallengeType.SNAKE -> SnakeChallengeFragment()
            else -> null
        }

        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } else {
            onChallengeCompleted()
        }
    }

    open fun onChallengeCompleted() {
        AlarmPlayer.stop()
        AlarmNotifier.stop(this)
        finish()
    }
}
