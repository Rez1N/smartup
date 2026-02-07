package com.frovexsoftware.smartup

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @Test
    fun openSettingsAndAiAgent() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.btnSettings)).perform(click())
        onView(withId(R.id.settingsDrawer)).check(matches(isDisplayed()))
        scenario.onActivity { activity ->
            activity.findViewById<DrawerLayout>(R.id.drawerLayout).closeDrawer(GravityCompat.END)
        }

        onView(withId(R.id.btnAiAgent)).perform(click())
        onView(withId(R.id.btnAiAgentBack)).check(matches(isDisplayed()))
    }

    @Test
    fun openCreateAlarmScreen() {
        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.btnAddAlarm)).perform(click())
        onView(withId(R.id.tvTimeBig)).check(matches(isDisplayed()))
        onView(withId(R.id.btnCreate)).check(matches(isDisplayed()))
    }
}
