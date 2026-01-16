package com.frovexsoftware.smartup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Простая заглушка-поле: только рисует сетку и плейсхолдер без игровой логики.
 */
class SnakeBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val gridSize = 8
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#303030")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E88E5")
        style = Paint.Style.FILL
        alpha = 60
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cellSize = (minOf(width, height).toFloat() / gridSize)
        val offsetX = (width - cellSize * gridSize) / 2f
        val offsetY = (height - cellSize * gridSize) / 2f

        // lightly fill area
        canvas.drawRect(offsetX, offsetY, offsetX + cellSize * gridSize, offsetY + cellSize * gridSize, fillPaint)

        // grid
        for (i in 0..gridSize) {
            val pos = offsetX + i * cellSize
            canvas.drawLine(pos, offsetY, pos, offsetY + cellSize * gridSize, gridPaint)
            canvas.drawLine(offsetX, offsetY + i * cellSize, offsetX + cellSize * gridSize, offsetY + i * cellSize, gridPaint)
        }
    }
}
