package com.frovexsoftware.smartup

import com.frovexsoftware.smartup.alarm.TimeLogic
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class TimeLogicTest {

    @Test
    fun testCalculateNextTrigger_singleWeekday() {
        val now = Calendar.getInstance()
        val today = now.get(Calendar.DAY_OF_WEEK)
        val nextDay = (today % 7) + 1

        val trigger = TimeLogic.calculateNextTrigger(10, 0, setOf(nextDay), null)

        val expected = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertEquals(expected.get(Calendar.DAY_OF_YEAR), trigger.get(Calendar.DAY_OF_YEAR))
    }

    @Test
    fun testCalculateNextTrigger_pastDate() {
        val now = Calendar.getInstance()
        val pastDate = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        val trigger = TimeLogic.calculateNextTrigger(10, 0, emptySet(), pastDate.timeInMillis)

        val expected = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertEquals(expected.timeInMillis, trigger.timeInMillis)
    }
}
