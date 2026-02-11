package com.frovexsoftware.smartup

import com.frovexsoftware.smartup.challenge.DateChallengeFragment
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class DateChallengeFragmentUnitTest {

    @Test
    fun testCorrectDate() {
        val fragment = DateChallengeFragment()
        val testDate = Date(1678886400000) // March 15, 2023
        fragment.dateProvider = { testDate }

        assertTrue(fragment.isCorrect("15.03.2023"))
    }

    @Test
    fun testIncorrectDate() {
        val fragment = DateChallengeFragment()
        val testDate = Date(1678886400000) // March 15, 2023
        fragment.dateProvider = { testDate }

        assertFalse(fragment.isCorrect("14.03.2023"))
    }
}
