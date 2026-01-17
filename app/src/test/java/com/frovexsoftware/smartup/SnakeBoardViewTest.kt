package com.frovexsoftware.smartup

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(AndroidJUnit4::class)
class SnakeBoardViewTest {

    private lateinit var snakeBoardView: SnakeBoardView

    @Before
    fun setup() {
        snakeBoardView = SnakeBoardView(RuntimeEnvironment.getApplication(), null)
    }

    @Test
    fun testReset() {
        snakeBoardView.setDirection(SnakeBoardView.Direction.UP)
        snakeBoardView.update()
        snakeBoardView.reset()
        assertEquals(Pair(10, 10), snakeBoardView.snake.first)
        assertEquals(1, snakeBoardView.snake.size)
    }

    @Test
    fun testUpdate() {
        val initialHead = snakeBoardView.snake.first
        snakeBoardView.update()
        val newHead = snakeBoardView.snake.first
        assertNotEquals(initialHead, newHead)
    }

    @Test
    fun testSetDirection() {
        snakeBoardView.setDirection(SnakeBoardView.Direction.UP)
        snakeBoardView.update()
        assertEquals(Pair(10, 9), snakeBoardView.snake.first)
    }
}
