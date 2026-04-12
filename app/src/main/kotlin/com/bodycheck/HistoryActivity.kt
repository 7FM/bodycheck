package com.bodycheck

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.bodycheck.data.ScanData
import com.bodycheck.data.ScanDatabase
import com.bodycheck.view.HistoryChartView

class HistoryActivity : AppCompatActivity() {

    private lateinit var db: ScanDatabase

    data class MetricDef(val label: String, val unit: String, val color: Int, val extractor: (ScanData) -> Float)

    private val metrics = listOf(
        MetricDef("Weight", "kg", Color.parseColor("#1976D2")) { it.weight },
        MetricDef("Skeletal Muscle", "kg", Color.parseColor("#388E3C")) { it.muscleMass },
        MetricDef("Body Fat", "%", Color.parseColor("#D32F2F")) { it.percentBodyFat },
        MetricDef("BMI", "", Color.parseColor("#F57C00")) { it.bmi },
        MetricDef("Body Water", "L", Color.parseColor("#00BCD4")) { it.totalBodyWater },
        MetricDef("Muscle Protein", "kg", Color.parseColor("#9C27B0")) { it.proteinMass },
        MetricDef("BMR", "kcal", Color.parseColor("#F44336")) { it.bmr },
        MetricDef("Health Score", "pts", Color.parseColor("#FFC107")) { it.healthScore },
        MetricDef("Visceral Fat", "level", Color.parseColor("#00897B")) { it.visceralFatLevel.toFloat() },
        MetricDef("Bio Age", "years", Color.parseColor("#009688")) { it.biologicalAge },
        MetricDef("Fat-Free Mass", "kg", Color.parseColor("#7B1FA2")) { it.freeFatMass }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyThemeMode(this)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_history)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.trends)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ThemeHelper.applyGenderTheme(this)

        db = ScanDatabase(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        val scans = db.getAllScans().sortedBy { it.scanDate }
        val container = findViewById<LinearLayout>(R.id.chartsContainer)
        container.removeAllViews()

        if (scans.size < 2) {
            val tv = TextView(this).apply {
                text = "Need at least 2 scans for trend charts"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@HistoryActivity, R.color.text_secondary))
                setPadding(0, (32 * resources.displayMetrics.density).toInt(), 0, 0)
                gravity = android.view.Gravity.CENTER
            }
            container.addView(tv)
            return
        }

        val dp = resources.displayMetrics.density

        for (metric in metrics) {
            val points = scans.map { HistoryChartView.DataPoint(it.scanDate, metric.extractor(it)) }

            // Title
            val title = TextView(this).apply {
                text = if (metric.unit.isNotEmpty()) "${metric.label} (${metric.unit})" else metric.label
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@HistoryActivity, R.color.text_primary))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(0, (12 * dp).toInt(), 0, (4 * dp).toInt())
            }
            container.addView(title)

            // Chart
            val chart = HistoryChartView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (160 * dp).toInt()
                ).apply { bottomMargin = (4 * dp).toInt() }
                setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
                setBackgroundColor(ContextCompat.getColor(this@HistoryActivity, R.color.surface))
            }
            val series = HistoryChartView.Series(metric.label, metric.color, points)
            chart.setData(listOf(series), setOf(metric.label))
            container.addView(chart)
        }
    }
}
