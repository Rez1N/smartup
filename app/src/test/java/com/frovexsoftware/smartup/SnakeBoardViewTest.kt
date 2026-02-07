package com.frovexsoftware.smartup

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
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
        val center = snakeBoardView.boardSize / 2
        assertEquals(Pair(center, center), snakeBoardView.snake.first())
        assertEquals(1, snakeBoardView.snake.size)
    }

    @Test
    fun testUpdate() {
        val initialHead = snakeBoardView.snake.first()
        snakeBoardView.update()
        val newHead = snakeBoardView.snake.first()
        assertNotEquals(initialHead, newHead)
    }

    @Test
    fun testSetDirection() {
        val start = snakeBoardView.snake.first()
        snakeBoardView.setDirection(SnakeBoardView.Direction.UP)
        snakeBoardView.update()
        val expected = Pair(start.first, (start.second - 1 + snakeBoardView.boardSize) % snakeBoardView.boardSize)
        assertEquals(expected, snakeBoardView.snake.first())
    }
}
