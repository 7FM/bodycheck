package com.bodycheck.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.bodycheck.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class DataPoint(val timestamp: Long, val value: Float)
    data class Series(val label: String, val color: Int, val points: List<DataPoint>)

    private val density = resources.displayMetrics.density
    private val scaledDensity = resources.displayMetrics.scaledDensity

    private var allSeries: List<Series> = emptyList()
    private val _visibleLabels = mutableSetOf<String>()

    var visibleSeries: Set<String>
        get() = _visibleLabels
        set(value) {
            _visibleLabels.clear()
            _visibleLabels.addAll(value)
            invalidate()
        }

    fun toggleSeries(label: String) {
        if (_visibleLabels.contains(label)) _visibleLabels.remove(label)
        else _visibleLabels.add(label)
        invalidate()
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        color = context.getColor(R.color.divider)
    }

    private val axisLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 10f * scaledDensity
        color = context.getColor(R.color.text_secondary)
    }

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 13f * scaledDensity
        textAlign = Paint.Align.CENTER
        color = context.getColor(R.color.text_secondary)
    }

    private val legendPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 11f * scaledDensity
        color = context.getColor(R.color.text_primary)
    }

    private val linePath = Path()
    private val fillPath = Path()

    private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

    fun setData(series: List<Series>, defaultVisible: Set<String>? = null) {
        allSeries = series
        _visibleLabels.clear()
        if (defaultVisible != null) {
            _visibleLabels.addAll(defaultVisible)
        } else if (series.isNotEmpty()) {
            _visibleLabels.add(series.first().label)
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val leftMargin = 48f * density
        val rightMargin = 16f * density
        val topMargin = 8f * density
        val bottomMargin = 24f * density

        val chartLeft = paddingLeft + leftMargin
        val chartRight = width - paddingRight - rightMargin
        val chartTop = paddingTop + topMargin
        val chartBottom = height - paddingBottom - bottomMargin
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        // Collect visible series
        val visible = allSeries.filter { it.label in _visibleLabels }

        // Check if we have enough data
        val allPoints = visible.flatMap { it.points }
        if (allPoints.size < 2 || visible.all { it.points.size < 2 }) {
            canvas.drawText(
                if (allSeries.flatMap { it.points }.isEmpty()) "No data"
                else "Need at least 2 scans for chart",
                width / 2f, height / 2f, emptyPaint
            )
            return
        }

        // Compute Y range from all visible series
        val minVal = allPoints.minOf { it.value }
        val maxVal = allPoints.maxOf { it.value }
        val valueRange = if (maxVal - minVal < 1f) 2f else (maxVal - minVal) * 1.2f
        val valueMid = (maxVal + minVal) / 2f
        val valueBottom = valueMid - valueRange / 2f
        val valueTop = valueMid + valueRange / 2f

        // Compute X range from all visible series
        val minTime = allPoints.minOf { it.timestamp }
        val maxTime = allPoints.maxOf { it.timestamp }
        val timeRange = if (maxTime == minTime) 1L else maxTime - minTime

        // Horizontal grid lines
        val gridLineCount = 4
        axisLabelPaint.textAlign = Paint.Align.RIGHT
        for (i in 0..gridLineCount) {
            val fraction = i.toFloat() / gridLineCount
            val y = chartBottom - fraction * chartHeight
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint)
            val value = valueBottom + fraction * (valueTop - valueBottom)
            canvas.drawText(
                String.format("%.1f", value),
                chartLeft - 4f * density, y + axisLabelPaint.textSize / 3f,
                axisLabelPaint
            )
        }

        // Draw each visible series
        val seriesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * density
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
        val seriesFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val seriesDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        for (series in visible) {
            val sorted = series.points.sortedBy { it.timestamp }
            if (sorted.size < 2) continue

            seriesPaint.color = series.color
            seriesDotPaint.color = series.color
            seriesFillPaint.color = Color.argb(30, Color.red(series.color), Color.green(series.color), Color.blue(series.color))

            linePath.reset()
            fillPath.reset()

            for (i in sorted.indices) {
                val point = sorted[i]
                val x = chartLeft + ((point.timestamp - minTime).toFloat() / timeRange) * chartWidth
                val y = chartBottom - ((point.value - valueBottom) / (valueTop - valueBottom)) * chartHeight

                if (i == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, chartBottom)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            val lastX = chartLeft + ((sorted.last().timestamp - minTime).toFloat() / timeRange) * chartWidth
            fillPath.lineTo(lastX, chartBottom)
            fillPath.close()

            canvas.drawPath(fillPath, seriesFillPaint)
            canvas.drawPath(linePath, seriesPaint)

            for (point in sorted) {
                val x = chartLeft + ((point.timestamp - minTime).toFloat() / timeRange) * chartWidth
                val y = chartBottom - ((point.value - valueBottom) / (valueTop - valueBottom)) * chartHeight
                canvas.drawCircle(x, y, 3f * density, seriesDotPaint)
            }
        }

        // Date labels along the X axis (use first visible series' timestamps, or all)
        axisLabelPaint.textAlign = Paint.Align.CENTER
        val refPoints = (visible.firstOrNull()?.points ?: emptyList()).sortedBy { it.timestamp }
        val maxDateLabels = 6
        val labelStep = if (refPoints.size <= maxDateLabels) 1 else refPoints.size / maxDateLabels
        for (i in refPoints.indices) {
            if (i % labelStep == 0 || i == refPoints.size - 1) {
                val point = refPoints[i]
                val x = chartLeft + ((point.timestamp - minTime).toFloat() / timeRange) * chartWidth
                canvas.drawText(
                    dateFormat.format(Date(point.timestamp)),
                    x, chartBottom + 14f * density, axisLabelPaint
                )
            }
        }
    }
}
