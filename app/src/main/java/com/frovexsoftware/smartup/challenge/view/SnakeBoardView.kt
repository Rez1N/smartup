package com.frovexsoftware.smartup.challenge.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.frovexsoftware.smartup.R
import kotlin.math.min
import java.util.LinkedList
import java.util.Random

class SnakeBoardView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    internal val snake = LinkedList<Pair<Int, Int>>()
    private var food = Pair(-1, -1)
    private var direction = Direction.RIGHT
    private var score = 0
    private val random = Random()

    internal val boardSize = 12
    private var cellSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f
    private var segmentRadius = 0f

    private val boardColor = ContextCompat.getColor(context, R.color.snake_board_bg)
    private val gridColor = ContextCompat.getColor(context, R.color.snake_grid_line)
    private val snakeBodyColor = ContextCompat.getColor(context, R.color.snake_body)
    private val snakeHeadColor = ContextCompat.getColor(context, R.color.snake_head)
    private val foodColor = ContextCompat.getColor(context, R.color.snake_food)

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
        segmentRadius = cellSize * 0.35f
    }

    private val rect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Board background
        paint.color = boardColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(
            offsetX, offsetY,
            offsetX + cellSize * boardSize,
            offsetY + cellSize * boardSize,
            cellSize, cellSize, paint
        )

        // Subtle grid
        paint.color = gridColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        for (i in 1 until boardSize) {
            val x = offsetX + i * cellSize
            val y = offsetY + i * cellSize
            canvas.drawLine(x, offsetY, x, offsetY + cellSize * boardSize, paint)
            canvas.drawLine(offsetX, y, offsetX + cellSize * boardSize, y, paint)
        }
        paint.style = Paint.Style.FILL

        // Draw food (rounded)
        paint.color = foodColor
        val fx = offsetX + food.first * cellSize + cellSize / 2f
        val fy = offsetY + food.second * cellSize + cellSize / 2f
        canvas.drawCircle(fx, fy, segmentRadius, paint)

        // Draw snake body
        for ((i, segment) in snake.withIndex()) {
            paint.color = if (i == 0) snakeHeadColor else snakeBodyColor
            val left = offsetX + segment.first * cellSize + (cellSize - segmentRadius * 2) / 2f
            val top = offsetY + segment.second * cellSize + (cellSize - segmentRadius * 2) / 2f
            rect.set(left, top, left + segmentRadius * 2, top + segmentRadius * 2)
            canvas.drawRoundRect(rect, segmentRadius * 0.5f, segmentRadius * 0.5f, paint)
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
