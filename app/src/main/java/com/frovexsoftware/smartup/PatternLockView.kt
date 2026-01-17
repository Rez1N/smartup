package com.frovexsoftware.smartup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.min

class PatternLockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnPatternCompleteListener {
        fun onPatternCompleted(pattern: List<Int>)
    }

    private val gridSize = 3
    private val nodes = ArrayList<PointF>(gridSize * gridSize)
    private val targetPattern = mutableListOf<Int>()
    private val userPattern = mutableListOf<Int>()

    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#221E88E5")
    }
    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#F2F8FF")
    }
    private val nodeActivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#CDE2FF")
    }
    private val targetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#A6C8FF")
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(18f, 14f), 0f)
    }
    private val userPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.parseColor("#B2D7FF")
    }
    private val nodeRadiusPx = 5f // ~10px diameter (dp)
    private val haloRadiusPx = 30f // soft shadow/halo around dots

    private var nodeRadius: Float = 0f
    private var haloRadius: Float = 0f
    private var fingerX: Float? = null
    private var fingerY: Float? = null

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    var previewEnabled: Boolean = true
    var listener: OnPatternCompleteListener? = null
    var progressListener: ((Int) -> Unit)? = null

    fun setTargetPattern(pattern: List<Int>) {
        targetPattern.clear()
        targetPattern.addAll(pattern)
        resetUserPattern()
        invalidate()
    }

    fun resetUserPattern() {
        userPattern.clear()
        fingerX = null
        fingerY = null
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        nodes.clear()
        val size = min(w, h).toFloat()
        val cell = size / gridSize
        val density = resources.displayMetrics.density
        nodeRadius = nodeRadiusPx * density
        haloRadius = haloRadiusPx * density
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val cx = col * cell + cell / 2f
                val cy = row * cell + cell / 2f
                nodes.add(PointF(cx, cy))
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw inactive nodes with halo
        nodes.forEach { point ->
            canvas.drawCircle(point.x, point.y, haloRadius, haloPaint)
            canvas.drawCircle(point.x, point.y, nodeRadius, nodePaint)
        }

        if (previewEnabled && targetPattern.size >= 2) {
            drawPattern(canvas, targetPattern, targetPaint)
            // Draw target nodes emphasized
            targetPattern.forEach { index ->
                val point = nodes.getOrNull(index) ?: return@forEach
                canvas.drawCircle(point.x, point.y, nodeRadius, targetPaint)
            }
        }

        if (userPattern.isNotEmpty()) {
            drawPattern(canvas, userPattern, userPaint)
            userPattern.forEach { index ->
                val point = nodes.getOrNull(index) ?: return@forEach
                canvas.drawCircle(point.x, point.y, nodeRadius, nodeActivePaint)
            }
        }

        // Draw line to current finger position
        val lastIndex = userPattern.lastOrNull()
        if (lastIndex != null && fingerX != null && fingerY != null) {
            val lastPoint = nodes[lastIndex]
            canvas.drawLine(lastPoint.x, lastPoint.y, fingerX!!, fingerY!!, userPaint)
        }
    }

    private fun drawPattern(canvas: Canvas, pattern: List<Int>, paint: Paint) {
        if (pattern.size < 2) return
        val path = Path()
        val first = nodes.getOrNull(pattern.first()) ?: return
        path.moveTo(first.x, first.y)
        for (i in 1 until pattern.size) {
            val point = nodes.getOrNull(pattern[i]) ?: continue
            path.lineTo(point.x, point.y)
        }
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                resetUserPattern()
                handleTouch(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                handleTouch(event.x, event.y)
                fingerX = event.x
                fingerY = event.y
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                fingerX = null
                fingerY = null
                listener?.onPatternCompleted(userPattern.toList())
                invalidate()
            }
        }
        return true
    }

    private fun handleTouch(x: Float, y: Float) {
        val index = findNode(x, y) ?: return
        if (!userPattern.contains(index)) {
            userPattern.add(index)
            progressListener?.invoke(userPattern.size)
            vibrateTick()
        }
    }

    private fun findNode(x: Float, y: Float): Int? {
        nodes.forEachIndexed { index, point ->
            if (hypot((x - point.x).toDouble(), (y - point.y).toDouble()) <= nodeRadius * 1.6f) {
                return index
            }
        }
        return null
    }

    private fun vibrateTick() {
        val vib = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(15L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(15L)
        }
    }
}
