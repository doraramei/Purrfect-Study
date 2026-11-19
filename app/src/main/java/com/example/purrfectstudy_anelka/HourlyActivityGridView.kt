package com.example.purrfectstudy_anelka

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class HourlyActivityGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class SessionBlock(
        val startMinuteFrom5Am: Int,
        val endMinuteFrom5Am: Int,
        val color: Int
    )

    private var blocks: List<SessionBlock> = emptyList()
    private val density = resources.displayMetrics.density

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#757575")
        textSize = 8.5f * density
        textAlign = Paint.Align.RIGHT
    }

    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val emptyCellColor = Color.parseColor("#F5F5F5")
    private val rectF = RectF()

    fun setSessions(sessionBlocks: List<SessionBlock>) {
        blocks = sessionBlocks
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return

        val labelWidth = 20f * density
        val gridLeft = labelWidth + 4f * density
        val gridWidth = (width - gridLeft - 2f * density).coerceAtLeast(1f)
        val totalRows = 24
        val totalCols = 6
        val gap = 1.5f * density

        val rowHeight = height.toFloat() / totalRows.toFloat()
        val colWidth = gridWidth / totalCols.toFloat()
        val corner = 2f * density

        for (row in 0 until totalRows) {
            val hour24 = (5 + row) % 24
            val rowTop = row * rowHeight
            val rowBottom = rowTop + rowHeight - gap

            // Gambar label jam di kiri (hanya tiap baris agar mudah dibaca)
            val centerY = rowTop + (rowHeight - gap) / 2f + (labelPaint.textSize * 0.35f)
            canvas.drawText(hour24.toString(), labelWidth, centerY, labelPaint)

            for (col in 0 until totalCols) {
                val cellStartMin = row * 60 + col * 10
                val cellEndMin = cellStartMin + 10

                val matchingBlock = blocks.firstOrNull { block ->
                    block.endMinuteFrom5Am > cellStartMin && block.startMinuteFrom5Am < cellEndMin
                }

                cellPaint.color = matchingBlock?.color ?: emptyCellColor

                val cellLeft = gridLeft + col * colWidth
                val cellRight = cellLeft + colWidth - gap
                rectF.set(cellLeft, rowTop, cellRight, rowBottom)
                canvas.drawRoundRect(rectF, corner, corner, cellPaint)
            }
        }
    }
}
