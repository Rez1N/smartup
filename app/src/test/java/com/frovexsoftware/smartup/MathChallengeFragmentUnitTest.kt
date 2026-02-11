package com.frovexsoftware.smartup

import com.frovexsoftware.smartup.challenge.MathChallengeFragment
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Random

class MathChallengeFragmentUnitTest {

    @Test
    fun testGenerateQuestion() {
        val fragment = MathChallengeFragment()
        fragment.random = Random(0) // Seeded random for predictability

        fragment.generateQuestion()

        assertEquals(1, fragment.num1)
        assertEquals(9, fragment.num2)
        assertEquals(10, fragment.correctAnswer)
    }
}
