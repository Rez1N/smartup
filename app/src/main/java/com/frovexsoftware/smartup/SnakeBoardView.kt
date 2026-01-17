package com.frovexsoftware.smartup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.LinkedList
import java.util.Random

class SnakeBoardView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    internal val snake = LinkedList<Pair<Int, Int>>()
    private var food = Pair(0, 0)
    private var direction = Direction.RIGHT
    private var score = 0

    private val boardSize = 20
    private var cellSize = 0f

    var onGameWon: (() -> Unit)? = null

    init {
        reset()
    }

    fun reset() {
        snake.clear()
        snake.add(Pair(boardSize / 2, boardSize / 2))
        generateFood()
        direction = Direction.RIGHT
        score = 0
        invalidate()
    }

    fun setDirection(newDirection: Direction) {
        if (direction.isOpposite(newDirection)) return
        direction = newDirection
    }

    fun update() {
        val head = snake.first
        val newHead = when (direction) {
            Direction.UP -> Pair(head.first, head.second - 1)
            Direction.DOWN -> Pair(head.first, head.second + 1)
            Direction.LEFT -> Pair(head.first - 1, head.second)
            Direction.RIGHT -> Pair(head.first + 1, head.second)
        }

        if (newHead.first < 0 || newHead.first >= boardSize || newHead.second < 0 || newHead.second >= boardSize || snake.contains(newHead)) {
            reset()
            return
        }

        snake.addFirst(newHead)

        if (newHead == food) {
            score++
            if (score >= 3) {
                onGameWon?.invoke()
            } else {
                generateFood()
            }
        } else {
            snake.removeLast()
        }
        invalidate()
    }

    private fun generateFood() {
        val random = Random()
        do {
            food = Pair(random.nextInt(boardSize), random.nextInt(boardSize))
        } while (snake.contains(food))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = w.toFloat() / boardSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw food
        paint.color = Color.YELLOW
        canvas.drawRect(food.first * cellSize, food.second * cellSize, (food.first + 1) * cellSize, (food.second + 1) * cellSize, paint)

        // Draw snake
        paint.color = Color.GREEN
        for (segment in snake) {
            canvas.drawRect(segment.first * cellSize, segment.second * cellSize, (segment.first + 1) * cellSize, (segment.second + 1) * cellSize, paint)
        }
    }

    enum class Direction {
        UP, DOWN, LEFT, RIGHT;

        fun isOpposite(other: Direction): Boolean {
            return (this == UP && other == DOWN) || (this == DOWN && other == UP) ||
                   (this == LEFT && other == RIGHT) || (this == RIGHT && other == LEFT)
        }
    }
}
