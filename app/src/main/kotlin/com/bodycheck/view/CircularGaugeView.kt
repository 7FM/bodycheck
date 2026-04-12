package com.bodycheck.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.bodycheck.R
import kotlin.math.roundToInt

class CircularGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density
    private val scaledDensity = resources.displayMetrics.scaledDensity

    private val arcStrokeWidth = 12f * density

    private val backgroundArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = arcStrokeWidth
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#E0E0E0")
    }

    private val foregroundArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = arcStrokeWidth
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#1976D2")
    }

    private val valueTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 16f * scaledDensity
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.text_primary)
        isFakeBoldText = true
    }

    private val unitTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 10f * scaledDensity
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.text_secondary)
    }

    private val arcRect = RectF()

    private var value: Float = 0f
    private var maxValue: Float = 100f
    private var label: String = ""
    private var unit: String = ""
    private var displayText: String? = null

    companion object {
        private const val START_ANGLE = 135f
        private const val SWEEP_ANGLE = 270f
    }

    fun setData(value: Float, maxValue: Float, label: String, unit: String, color: Int) {
        this.value = value
        this.maxValue = if (maxValue > 0f) maxValue else 1f
        this.label = label
        this.unit = unit
        this.displayText = null
        foregroundArcPaint.color = color
        invalidate()
    }

    fun setTextData(displayText: String, value: Float, maxValue: Float, label: String, color: Int) {
        this.value = value
        this.maxValue = if (maxValue > 0f) maxValue else 1f
        this.label = label
        this.unit = ""
        this.displayText = displayText
        foregroundArcPaint.color = color
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = (120 * density).toInt()
        val width = resolveSize(desiredSize, widthMeasureSpec)
        val height = resolveSize(desiredSize, heightMeasureSpec)
        val size = minOf(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = arcStrokeWidth / 2f + 4f * density
        arcRect.set(
            paddingLeft + padding,
            paddingTop + padding,
            width - paddingRight - padding,
            height - paddingBottom - padding
        )

        // Background arc
        canvas.drawArc(arcRect, START_ANGLE, SWEEP_ANGLE, false, backgroundArcPaint)

        // Foreground arc
        val fraction = (value / maxValue).coerceIn(0f, 1f)
        val foregroundSweep = SWEEP_ANGLE * fraction
        canvas.drawArc(arcRect, START_ANGLE, foregroundSweep, false, foregroundArcPaint)

        // Center value text
        val centerX = width / 2f
        val centerY = height / 2f

        val text = displayText
        if (text != null) {
            val innerRadius = arcRect.width() / 2f - arcStrokeWidth / 2f
            val maxTextWidth = innerRadius * 1.7f
            val savedSize = valueTextPaint.textSize
            var textSize = savedSize
            valueTextPaint.textSize = textSize
            while (valueTextPaint.measureText(text) > maxTextWidth && textSize > 8f * scaledDensity) {
                textSize -= 1f * scaledDensity
                valueTextPaint.textSize = textSize
            }
            val textY = centerY + textSize / 3f
            canvas.drawText(text, centerX, textY, valueTextPaint)
            valueTextPaint.textSize = savedSize
        } else {
            val displayValue = if (value == value.roundToInt().toFloat()) {
                value.roundToInt().toString()
            } else {
                String.format("%.1f", value)
            }

            val valueY = centerY + valueTextPaint.textSize / 3f
            canvas.drawText(displayValue, centerX, valueY, valueTextPaint)

            // Unit text below center
            val unitY = valueY + unitTextPaint.textSize + 4f * density
            canvas.drawText(unit, centerX, unitY, unitTextPaint)
        }
    }
}
