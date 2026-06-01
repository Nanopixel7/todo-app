package com.study.pomodoro.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CircularTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var progress: Float = 0f
        set(value) { field = value.coerceIn(0f, 1f); invalidate() }

    var isPlaying: Boolean = false
        set(value) { field = value; invalidate() }

    var showIcon: Boolean = true
        set(value) { field = value; invalidate() }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(10f)
        strokeCap = Paint.Cap.ROUND
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(2f)
        strokeCap = Paint.Cap.ROUND
    }

    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x44FFFFFF
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }

    private val arcRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) * 0.72f
        val ringStroke = dpToPx(10f)

        // Tick marks
        val tickCount = 24
        val outerR = radius + dpToPx(18f)
        val innerR = radius + dpToPx(10f)
        for (i in 0 until tickCount) {
            val angle = Math.toRadians((i * 360.0 / tickCount) - 90.0)
            val sx = cx + outerR * cos(angle).toFloat()
            val sy = cy + outerR * sin(angle).toFloat()
            val ex = cx + innerR * cos(angle).toFloat()
            val ey = cy + innerR * sin(angle).toFloat()
            val isMain = i % 6 == 0
            tickPaint.strokeWidth = if (isMain) dpToPx(3f) else dpToPx(1.5f)
            val startR = if (isMain) outerR else outerR - dpToPx(3f)
            canvas.drawLine(
                cx + startR * cos(angle).toFloat(),
                cy + startR * sin(angle).toFloat(),
                ex, ey, tickPaint
            )
        }

        // Ring
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius)
        canvas.drawCircle(cx, cy, radius, ringPaint)

        // Sweep arc (elapsed portion)
        if (progress > 0f) {
            sweepPaint.strokeWidth = ringStroke
            canvas.drawArc(arcRect, -90f, progress * 360f, false, sweepPaint)
        }

        // Play or Pause icon (hidden when time text is shown)
        if (showIcon) {
            if (isPlaying) {
                drawPauseIcon(canvas, cx, cy, radius * 0.28f)
            } else {
                drawPlayIcon(canvas, cx, cy, radius * 0.30f)
            }
        }
    }

    private fun drawPlayIcon(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val path = Path()
        path.moveTo(cx - size * 0.5f, cy - size)
        path.lineTo(cx - size * 0.5f, cy + size)
        path.lineTo(cx + size, cy)
        path.close()
        canvas.drawPath(path, iconPaint)
    }

    private fun drawPauseIcon(canvas: Canvas, cx: Float, cy: Float, size: Float) {
        val barW = size * 0.55f
        val barH = size * 1.8f
        val gap = size * 0.45f
        canvas.drawRoundRect(
            cx - gap - barW, cy - barH,
            cx - gap, cy + barH,
            dpToPx(3f), dpToPx(3f), iconPaint
        )
        canvas.drawRoundRect(
            cx + gap, cy - barH,
            cx + gap + barW, cy + barH,
            dpToPx(3f), dpToPx(3f), iconPaint
        )
    }

    private fun dpToPx(dp: Float): Float =
        dp * context.resources.displayMetrics.density
}
