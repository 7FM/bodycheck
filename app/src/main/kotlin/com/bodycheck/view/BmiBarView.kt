package com.bodycheck.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.bodycheck.R

class BmiBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private val scaledDensity = resources.displayMetrics.scaledDensity

    private val barHeight = 24f * density
    private val indicatorSize = 8f * density
    private val labelTextSize = 9f * scaledDensity
    private val rangeTextSize = 8f * scaledDensity
    private val indicatorGap = 4f * density
    private val labelGap = 6f * density
    private val barCornerRadius = 4f * density

    private var bmi: Float = 0f

    private data class BmiCategory(
        val name: String,
        val rangeLabel: String,
        val rangeMin: Float,
        val rangeMax: Float,
        val color: Int
    )

    private val categories = listOf(
        BmiCategory("Underweight", "10-18.4", 10f, 18.4f, Color.parseColor("#0c639a")),
        BmiCategory("Normal", "18.5-24.9", 18.5f, 24.9f, Color.parseColor("#4fbf09")),
        BmiCategory("Overweight", "25-29.9", 25f, 29.9f, Color.parseColor("#f0e233")),
        BmiCategory("Obese I", "30-34.9", 30f, 34.9f, Color.parseColor("#d08713")),
        BmiCategory("Obese II", "35-39.9", 35f, 39.9f, Color.parseColor("#bc0d0d")),
        BmiCategory("Obese III", "40+", 40f, 50f, Color.parseColor("#870909"))
    )

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.getColor(R.color.text_primary)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = labelTextSize
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.text_primary)
    }

    private val rangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = rangeTextSize
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.text_secondary)
    }

    private val indicatorPath = Path()

    companion object {
        private const val SCALE_MIN = 10f
        private const val SCALE_MAX = 50f
    }

    fun setBmi(bmi: Float) {
        this.bmi = bmi.coerceIn(SCALE_MIN, SCALE_MAX)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (300 * density).toInt()
        val desiredHeight = (indicatorSize + indicatorGap + barHeight + labelGap +
                labelTextSize + 4f * density + rangeTextSize + 8f * density).toInt()

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val leftPad = paddingLeft.toFloat()
        val rightPad = paddingRight.toFloat()
        val topPad = paddingTop.toFloat()
        val availableWidth = width - leftPad - rightPad
        val segmentWidth = availableWidth / categories.size

        val barTop = topPad + indicatorSize + indicatorGap
        val barBottom = barTop + barHeight

        // Draw colored bar segments
        for (i in categories.indices) {
            barPaint.color = categories[i].color
            val segLeft = leftPad + i * segmentWidth
            val segRight = segLeft + segmentWidth

            canvas.drawRoundRect(
                segLeft, barTop, segRight, barBottom,
                if (i == 0) barCornerRadius else 0f,
                if (i == 0) barCornerRadius else 0f,
                barPaint
            )
            // Overdraw with plain rect to cover inner corners, then redraw rounded ends
        }

        // Simpler approach: draw all segments as plain rects, then round the first and last
        // Re-draw with a clip or just accept slight visual simplification
        // For correctness, draw left-rounded first, right-rounded last, plain middle
        for (i in categories.indices) {
            barPaint.color = categories[i].color
            val segLeft = leftPad + i * segmentWidth
            val segRight = segLeft + segmentWidth
            canvas.drawRect(segLeft, barTop, segRight, barBottom, barPaint)
        }

        // Draw triangle indicator above the bar
        if (bmi > 0f) {
            var indicatorX = leftPad
            for (i in categories.indices) {
                val cat = categories[i]
                val segLeft = leftPad + i * segmentWidth
                if (bmi <= cat.rangeMax || i == categories.lastIndex) {
                    val fractionInSeg = ((bmi - cat.rangeMin) / (cat.rangeMax - cat.rangeMin)).coerceIn(0f, 1f)
                    indicatorX = segLeft + fractionInSeg * segmentWidth
                    break
                }
            }

            indicatorPath.reset()
            indicatorPath.moveTo(indicatorX, barTop - indicatorGap)
            indicatorPath.lineTo(indicatorX - indicatorSize, barTop - indicatorGap - indicatorSize)
            indicatorPath.lineTo(indicatorX + indicatorSize, barTop - indicatorGap - indicatorSize)
            indicatorPath.close()

            canvas.drawPath(indicatorPath, indicatorPaint)
        }

        // Draw labels below the bar
        val labelY = barBottom + labelGap + labelTextSize
        val rangeY = labelY + 4f * density + rangeTextSize

        for (i in categories.indices) {
            val centerX = leftPad + i * segmentWidth + segmentWidth / 2f
            canvas.drawText(categories[i].name, centerX, labelY, labelPaint)
            canvas.drawText(categories[i].rangeLabel, centerX, rangeY, rangePaint)
        }
    }
}
