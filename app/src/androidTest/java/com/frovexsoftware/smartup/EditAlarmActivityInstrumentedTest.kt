package com.frovexsoftware.smartup

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.assertion.ViewAssertions.matches
import com.frovexsoftware.smartup.challenge.ChallengeType
import com.frovexsoftware.smartup.ui.EditAlarmActivity
import org.junit.Test
import org.junit.runner.RunWith
import java.util.ArrayList

@RunWith(AndroidJUnit4::class)
class EditAlarmActivityInstrumentedTest {

    @Test
    fun openEditAlarmAndToggleChallenges() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName("com.frovexsoftware.smartup", "com.frovexsoftware.smartup.ui.EditAlarmActivity")
            putExtra("alarm_id", -1)
            putExtra("time", System.currentTimeMillis())
            putExtra("date", -1L)
            putExtra("enabled", true)
            putExtra("description", "")
            putExtra("is24h", true)
            putIntegerArrayListExtra("weekdays", ArrayList())
            putExtra("challenge_type", ChallengeType.NONE.name)
        }

        ActivityScenario.launch<EditAlarmActivity>(intent)

        onView(withId(R.id.tvTimeBig)).check(matches(isDisplayed()))
        onView(withId(R.id.btnCreate)).check(matches(isDisplayed()))

        onView(withId(R.id.chipSnake)).perform(click())
        onView(withId(R.id.chipDots)).perform(click())
        onView(withId(R.id.chipMath)).perform(click())
        onView(withId(R.id.chipColor)).perform(click())

        onView(withId(R.id.extraChallengesToggle)).perform(click())
        onView(withId(R.id.extraChallengesContainer)).check(matches(isDisplayed()))

        onView(withId(R.id.chipShake)).perform(click())
        onView(withId(R.id.chipHold)).perform(click())
        onView(withId(R.id.chipRandom)).perform(click())
    }
}
