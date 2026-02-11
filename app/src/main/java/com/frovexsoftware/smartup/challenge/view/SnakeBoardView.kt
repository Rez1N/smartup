package com.frovexsoftware.smartup.challenge.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min
import java.util.LinkedList
import java.util.Random

class SnakeBoardView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint()
    internal val snake = LinkedList<Pair<Int, Int>>()
    private var food = Pair(-1, -1)
    private var direction = Direction.RIGHT
    private var score = 0
    private val random = Random()

    internal val boardSize = 12
    private var cellSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f
    private var inset = 0f

    var onGameWon: (() -> Unit)? = null

    init {
        reset()
    }

    val currentScore: Int
        get() = score

    fun reset() {
        snake.clear()
        snake.add(Pair(boardSize / 2, boardSize / 2))
        ensureFood()
        direction = Direction.RIGHT
        score = 0
        invalidate()
    }

    fun setDirection(newDirection: Direction) {
        if (direction.isOpposite(newDirection)) return
        direction = newDirection
    }

    fun update() {
        ensureFood()
        val head = snake.first()
        val newHeadRaw = when (direction) {
            Direction.UP -> Pair(head.first, head.second - 1)
            Direction.DOWN -> Pair(head.first, head.second + 1)
            Direction.LEFT -> Pair(head.first - 1, head.second)
            Direction.RIGHT -> Pair(head.first + 1, head.second)
        }

        val newHead = Pair(
            ((newHeadRaw.first % boardSize) + boardSize) % boardSize,
            ((newHeadRaw.second % boardSize) + boardSize) % boardSize
        )

        if (snake.contains(newHead)) {
            reset()
            return
        }

        snake.addFirst(newHead)

        if (newHead == food) {
            score++
            if (score >= 5) {
                onGameWon?.invoke()
            } else {
                generateFood()
            }
        } else {
            snake.removeLast()
        }
        invalidate()
    }

    private fun ensureFood() {
        if (food.first !in 0 until boardSize || food.second !in 0 until boardSize) {
            generateFood()
            return
        }
        if (snake.contains(food)) {
            generateFood()
        }
    }

    private fun generateFood() {
        do {
            food = Pair(random.nextInt(boardSize), random.nextInt(boardSize))
        } while (snake.contains(food))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val usable = min(w, h).toFloat()
        cellSize = usable / boardSize
        offsetX = (w - usable) / 2f
        offsetY = (h - usable) / 2f
        inset = cellSize * 0.1f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Board background
        paint.color = Color.BLACK
        canvas.drawRect(offsetX, offsetY, offsetX + cellSize * boardSize, offsetY + cellSize * boardSize, paint)

        // Draw food
        paint.color = Color.YELLOW
        canvas.drawRect(
            offsetX + food.first * cellSize + inset,
            offsetY + food.second * cellSize + inset,
            offsetX + (food.first + 1) * cellSize - inset,
            offsetY + (food.second + 1) * cellSize - inset,
            paint
        )

        // Draw snake
        paint.color = Color.GREEN
        for (segment in snake) {
            canvas.drawRect(
                offsetX + segment.first * cellSize + inset,
                offsetY + segment.second * cellSize + inset,
                offsetX + (segment.first + 1) * cellSize - inset,
                offsetY + (segment.second + 1) * cellSize - inset,
                paint
            )
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
