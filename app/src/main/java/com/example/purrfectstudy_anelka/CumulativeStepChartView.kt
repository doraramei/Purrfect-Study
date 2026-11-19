package com.example.purrfectstudy_anelka

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class CumulativeStepChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var cumulativeSeconds = IntArray(24)
    private val density = resources.displayMetrics.density

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EFEFEF")
        strokeWidth = 1f * density
        style = Paint.Style.STROKE
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#22111111")
        style = Paint.Style.FILL
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#111111")
        strokeWidth = 2.2f * density
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#111111")
        style = Paint.Style.FILL
    }

    private val axisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#757575")
        textSize = 10f * density
    }

    private val strokePath = Path()
    private val fillPath = Path()

    fun setData(hourlyCumulative: IntArray) {
        cumulativeSeconds = if (hourlyCumulative.size == 24) {
            hourlyCumulative.copyOf()
        } else {
            IntArray(24)
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        val padLeft = 8f * density
        val padRight = 8f * density
        val padTop = 10f * density
        val padBottom = 20f * density

        val chartW = (width - padLeft - padRight).coerceAtLeast(1f)
        val chartH = (height - padTop - padBottom).coerceAtLeast(1f)
        val bottomY = padTop + chartH

        // Grid horizontal tipis (top, middle, baseline)
        canvas.drawLine(padLeft, padTop, padLeft + chartW, padTop, gridPaint)
        canvas.drawLine(padLeft, padTop + chartH / 2f, padLeft + chartW, padTop + chartH / 2f, gridPaint)
        canvas.drawLine(padLeft, bottomY, padLeft + chartW, bottomY, gridPaint)

        // Label sumbu X: 5:00 - 17:00 - 5:00
        val labelY = height - 4f * density
        axisTextPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("5:00", padLeft, labelY, axisTextPaint)
        axisTextPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("17:00", padLeft + chartW / 2f, labelY, axisTextPaint)
        axisTextPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("5:00", padLeft + chartW, labelY, axisTextPaint)

        val maxSecs = max(cumulativeSeconds.maxOrNull() ?: 0, 3600)
        val stepX = chartW / 24f

        strokePath.reset()
        fillPath.reset()

        strokePath.moveTo(padLeft, bottomY)
        fillPath.moveTo(padLeft, bottomY)

        var prevY = bottomY
        var lastActiveX = padLeft
        var lastActiveY = bottomY

        for (i in 0 until 24) {
            val secs = cumulativeSeconds[i].coerceAtLeast(0)
            val ratio = (secs.toFloat() / maxSecs.toFloat()).coerceIn(0f, 1f)
            val xStart = padLeft + i * stepX
            val xEnd = padLeft + (i + 1) * stepX
            val y = bottomY - ratio * chartH

            // Step mendatar ke titik xStart pada level lama, lalu naik ke level baru
            strokePath.lineTo(xStart, prevY)
            strokePath.lineTo(xStart, y)
            strokePath.lineTo(xEnd, y)

            fillPath.lineTo(xStart, prevY)
            fillPath.lineTo(xStart, y)
            fillPath.lineTo(xEnd, y)

            if (secs > 0) {
                lastActiveX = xEnd
                lastActiveY = y
            }
            prevY = y
        }

        fillPath.lineTo(padLeft + chartW, bottomY)
        fillPath.close()

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(strokePath, linePaint)

        if ((cumulativeSeconds.maxOrNull() ?: 0) > 0) {
            canvas.drawCircle(lastActiveX, lastActiveY, 3.5f * density, dotPaint)
        }
    }
}
