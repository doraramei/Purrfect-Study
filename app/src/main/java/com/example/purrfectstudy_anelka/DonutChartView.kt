package com.example.purrfectstudy_anelka

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class Slice(
        val label: String,
        val value: Float,
        val color: Int
    )

    private var slices: List<Slice> = emptyList()
    private var centerTitle: String = "0:00:00"
    private var centerSubtitle: String = ""

    private val density = resources.displayMetrics.density

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val sliceLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = 10f * density
    }

    private val centerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1F1F1F")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textSize = 13f * density
    }

    private val centerSubtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#757575")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textSize = 10.5f * density
    }

    private val emptyColor = Color.parseColor("#E5E5EA")
    private val rectF = RectF()

    fun setData(newSlices: List<Slice>, title: String, subtitle: String = "") {
        slices = newSlices.filter { it.value > 0f }
        centerTitle = title
        centerSubtitle = subtitle
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        if (size <= 0f) return

        val pad = 4f * density
        val radius = (size - pad * 2f) / 2f
        val cx = width / 2f
        val cy = height / 2f
        val innerRadius = radius * 0.52f
        val midRadius = (radius + innerRadius) / 2f

        rectF.set(cx - radius, cy - radius, cx + radius, cy + radius)

        val total = slices.sumOf { it.value.toDouble() }.toFloat()
        if (total <= 0f || slices.isEmpty()) {
            arcPaint.color = emptyColor
            canvas.drawCircle(cx, cy, radius, arcPaint)
            canvas.drawCircle(cx, cy, innerRadius, holePaint)
            drawCenterText(canvas, cx, cy)
            return
        }

        if (slices.size == 1) {
            val single = slices[0]
            arcPaint.color = single.color
            canvas.drawCircle(cx, cy, radius, arcPaint)
            canvas.drawCircle(cx, cy, innerRadius, holePaint)

            sliceLabelPaint.color = if (isLightColor(single.color)) {
                Color.parseColor("#2D2A26")
            } else {
                Color.WHITE
            }
            val labelY = cy - midRadius + (sliceLabelPaint.textSize * 0.35f)
            canvas.drawText("100%", cx, labelY, sliceLabelPaint)

            drawCenterText(canvas, cx, cy)
            return
        }

        var startAngle = -90f
        for (slice in slices) {
            val sweepAngle = (slice.value / total) * 360f
            arcPaint.color = slice.color
            canvas.drawArc(rectF, startAngle, sweepAngle, true, arcPaint)

            val percent = ((slice.value / total) * 100f).toInt()
            if (percent >= 8) {
                val midAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                val tx = cx + (midRadius * cos(midAngleRad)).toFloat()
                val ty = cy + (midRadius * sin(midAngleRad)).toFloat() + (sliceLabelPaint.textSize * 0.35f)
                sliceLabelPaint.color = if (isLightColor(slice.color)) {
                    Color.parseColor("#2D2A26")
                } else {
                    Color.WHITE
                }
                canvas.drawText("$percent%", tx, ty, sliceLabelPaint)
            }
            startAngle += sweepAngle
        }

        canvas.drawCircle(cx, cy, innerRadius, holePaint)
        drawCenterText(canvas, cx, cy)
    }

    private fun drawCenterText(canvas: Canvas, cx: Float, cy: Float) {
        if (centerSubtitle.isNotEmpty()) {
            canvas.drawText(centerTitle, cx, cy - 2f * density, centerTitlePaint)
            canvas.drawText(centerSubtitle, cx, cy + 12f * density, centerSubtitlePaint)
        } else {
            val baselineOffset = (centerTitlePaint.descent() + centerTitlePaint.ascent()) / 2f
            canvas.drawText(centerTitle, cx, cy - baselineOffset, centerTitlePaint)
        }
    }

    private fun isLightColor(color: Int): Boolean {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val luminance = (0.299 * r + 0.587 * g + 0.114 * b)
        return luminance > 180.0
    }
}
