package com.frovexsoftware.smartup

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.assertion.ViewAssertions.matches
import com.frovexsoftware.smartup.challenge.ChallengeType
import com.frovexsoftware.smartup.ui.StopAlarmActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StopAlarmActivityInstrumentedTest {

    @Test
    fun openNoChallengeScreen() {
        launchWithChallenge(ChallengeType.NONE)
        onView(withId(R.id.no_challenge_container)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_stop_alarm)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_snooze_alarm)).check(matches(isDisplayed()))
    }

    @Test
    fun openSnakeChallenge() {
        launchWithChallenge(ChallengeType.SNAKE)
        onView(withId(R.id.snakeBoard)).check(matches(isDisplayed()))
    }

    @Test
    fun openDotsChallenge() {
        launchWithChallenge(ChallengeType.DOTS)
        onView(withId(R.id.patternView)).check(matches(isDisplayed()))
    }

    @Test
    fun openMathChallenge() {
        launchWithChallenge(ChallengeType.MATH)
        onView(withId(R.id.answerEditText)).check(matches(isDisplayed()))
    }

    @Test
    fun openColorChallenge() {
        launchWithChallenge(ChallengeType.COLOR)
        onView(withId(R.id.colorView)).check(matches(isDisplayed()))
    }

    @Test
    fun openDateChallenge() {
        launchWithChallenge(ChallengeType.DATE)
        onView(withId(R.id.tvDateFormat)).check(matches(isDisplayed()))
    }

    @Test
    fun openShakeChallenge() {
        launchWithChallenge(ChallengeType.SHAKE)
        onView(withId(R.id.tvShakeCount)).check(matches(isDisplayed()))
    }

    @Test
    fun openHoldChallenge() {
        launchWithChallenge(ChallengeType.HOLD)
        onView(withId(R.id.btnHold)).check(matches(isDisplayed()))
    }

    private fun launchWithChallenge(type: ChallengeType) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName("com.frovexsoftware.smartup", "com.frovexsoftware.smartup.ui.StopAlarmActivity")
            putExtra("alarm_id", 1)
            putExtra("challenge_type", type.name)
        }
        ActivityScenario.launch<StopAlarmActivity>(intent)
    }
}
