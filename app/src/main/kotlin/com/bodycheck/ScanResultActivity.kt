package com.bodycheck

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import android.content.Intent
import android.content.SharedPreferences
import com.bodycheck.data.FieldMapping
import com.bodycheck.data.ScanData
import com.bodycheck.data.ScanDatabase
import com.bodycheck.data.SegmentalRating
import com.bodycheck.view.BmiBarView
import com.bodycheck.view.BodyHumanView
import com.bodycheck.view.CircularGaugeView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanResultActivity : AppCompatActivity() {

    private lateinit var db: ScanDatabase
    private var useRadarMode = true
    private var currentScan: ScanData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyThemeMode(this)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_scan_result)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.scan_result)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ThemeHelper.applyGenderTheme(this)

        db = ScanDatabase(this)

        val scanId = intent.getLongExtra("scan_id", -1)
        if (scanId == -1L) {
            Toast.makeText(this, "Invalid scan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val scan = db.getScanById(scanId)
        if (scan == null) {
            Toast.makeText(this, "Scan not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        populateViews(scan)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.scan_result_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_trends -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            R.id.action_gender -> {
                val prefs = getSharedPreferences("bodycheck_prefs", MODE_PRIVATE)
                val current = prefs.getString("gender", "male")
                val options = arrayOf("Male", "Female")
                val selected = if (current == "female") 1 else 0
                AlertDialog.Builder(this)
                    .setTitle("Gender")
                    .setSingleChoiceItems(options, selected) { dialog, which ->
                        prefs.edit().putString("gender", if (which == 1) "female" else "male").apply()
                        ThemeHelper.applyGenderTheme(this)
                        // Rebuild fat mass rows too since thresholds could differ
                        findViewById<android.widget.LinearLayout>(R.id.fatRadarContainer).removeAllViews()
                        currentScan?.let { rebuildSegmentalRatings(it) }
                        dialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun rebuildSegmentalRatings(scan: ScanData) {
        val prefs = getSharedPreferences("bodycheck_prefs", MODE_PRIVATE)
        val isMale = prefs.getString("gender", "male") == "male"
        val segLabels = arrayOf("Left Arm", "Right Arm", "Trunk", "Left Leg", "Right Leg")
        val segTypes = arrayOf(
            SegmentalRating.Segment.ARM, SegmentalRating.Segment.ARM,
            SegmentalRating.Segment.TORSO,
            SegmentalRating.Segment.LEG, SegmentalRating.Segment.LEG
        )
        val leanRadarIndices = intArrayOf(
            FieldMapping.LEFT_ARM_LEAN_MASS_RADAR, FieldMapping.RIGHT_ARM_LEAN_MASS_RADAR,
            FieldMapping.TRUNK_LEAN_MASS_RADAR,
            FieldMapping.LEFT_LEG_LEAN_MASS_RADAR, FieldMapping.RIGHT_LEG_LEAN_MASS_RADAR
        )
        val leanAbsIndices = intArrayOf(
            FieldMapping.LEFT_ARM_LEAN_MASS, FieldMapping.RIGHT_ARM_LEAN_MASS,
            FieldMapping.TRUNK_LEAN_MASS,
            FieldMapping.LEFT_LEG_LEAN_MASS, FieldMapping.RIGHT_LEG_LEAN_MASS
        )
        val fatRadarIndices = intArrayOf(
            FieldMapping.LEFT_ARM_FAT_MASS_RADAR, FieldMapping.RIGHT_ARM_FAT_MASS_RADAR,
            FieldMapping.TRUNK_FAT_MASS_RADAR,
            FieldMapping.LEFT_LEG_FAT_MASS_RADAR, FieldMapping.RIGHT_LEG_FAT_MASS_RADAR
        )
        val fatAbsIndices = intArrayOf(
            FieldMapping.LEFT_ARM_FAT_MASS, FieldMapping.RIGHT_ARM_FAT_MASS,
            FieldMapping.TRUNK_FAT_MASS,
            FieldMapping.LEFT_LEG_FAT_MASS, FieldMapping.RIGHT_LEG_FAT_MASS
        )

        val leanContainer = findViewById<android.widget.LinearLayout>(R.id.leanRadarContainer)
        leanContainer.removeAllViews()

        for (i in segLabels.indices) {
            val radar = scan.rawValues[leanRadarIndices[i]]
            val value = scan.rawValues[leanAbsIndices[i]]
            if (useRadarMode) {
                leanContainer.addView(createRatingRow(segLabels[i], value, "kg", radar, -1, null))
            } else {
                // Use radar-derived block index instead of buggy threshold comparison
                // (lean mass values compared against muscle-only thresholds produces wrong results)
                val block = SegmentalRating.radarToBlockIndex(radar)
                leanContainer.addView(createRatingRow(segLabels[i], value, "kg", radar, block, null))
            }
        }

        // Fat mass: only build once (radar-only, doesn't change with toggle)
        val fatContainer = findViewById<android.widget.LinearLayout>(R.id.fatRadarContainer)
        if (fatContainer.childCount == 0) {
            for (i in segLabels.indices) {
                val radar = scan.rawValues[fatRadarIndices[i]]
                val value = scan.rawValues[fatAbsIndices[i]]
                fatContainer.addView(createRatingRow(segLabels[i], value, "kg", radar, -1, null))
            }
        }

        // Update toggle button text
        findViewById<TextView>(R.id.btnToggleRatingMode).text = if (useRadarMode) "Mode: Radar" else "Mode: Threshold"
    }

    private fun populateViews(scan: ScanData) {
        currentScan = scan
        val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy  HH:mm", Locale.getDefault())

        // Date
        findViewById<TextView>(R.id.tvDate).text = dateFormat.format(Date(scan.scanDate))

        // Row 1 gauges
        findViewById<CircularGaugeView>(R.id.gaugeWeight).apply {
            setData(scan.weight, 200f, "Weight", "kg", Color.parseColor("#1976D2"))
            setOnClickListener { showInfo("Weight", getString(R.string.weight_desc)) }
        }
        findViewById<CircularGaugeView>(R.id.gaugeWater).apply {
            setData(scan.totalBodyWater, 80f, "Body Water", "L", Color.parseColor("#00BCD4"))
            setOnClickListener { showInfo("Body Water", getString(R.string.body_water_desc)) }
        }
        findViewById<CircularGaugeView>(R.id.gaugeFat).apply {
            setData(scan.percentBodyFat, 50f, "Body Fat", "%", Color.parseColor("#FF9800"))
            setOnClickListener { showInfo("Body Fat", getString(R.string.body_fat_desc)) }
        }
        findViewById<CircularGaugeView>(R.id.gaugeMuscle).apply {
            setData(scan.muscleMass, 60f, "Skeletal Muscle", "kg", Color.parseColor("#4CAF50"))
            setOnClickListener { showInfo("Skeletal Muscle", getString(R.string.skeletal_muscle_desc)) }
        }

        // Row 2 gauges
        findViewById<CircularGaugeView>(R.id.gaugeProtein).apply {
            setData(scan.proteinMass, 30f, "Muscle Protein", "kg", Color.parseColor("#9C27B0"))
            setOnClickListener { showInfo("Muscle Protein", getString(R.string.muscle_protein_desc)) }
        }

        val vfLevel = scan.visceralFatLevel
        val vfText = visceralFatText(vfLevel)
        val vfColor = visceralFatColor(vfLevel)
        findViewById<CircularGaugeView>(R.id.gaugeVisceralFat).apply {
            setTextData(vfText, vfLevel.toFloat(), 5f, "Visceral Fat", vfColor)
            setOnClickListener { showInfo("Visceral Fat", getString(R.string.visceral_fat_desc)) }
        }

        findViewById<CircularGaugeView>(R.id.gaugeBioAge).apply {
            setData(scan.biologicalAge, 80f, "Bio Age", "years", Color.parseColor("#009688"))
            setOnClickListener { showInfo("Bio Age", getString(R.string.bioage_desc)) }
        }
        findViewById<CircularGaugeView>(R.id.gaugeMetabolic).apply {
            setData(scan.bmr, 3000f, "Metabolic", "kcal", Color.parseColor("#F44336"))
            setOnClickListener { showInfo("Metabolic", getString(R.string.metabolic_desc)) }
        }

        // BMI bar
        findViewById<TextView>(R.id.tvBmiLabel).text = String.format("BMI: %.1f", scan.bmi)
        findViewById<BmiBarView>(R.id.bmiBar).setBmi(scan.bmi)

        // Health Score
        val healthScore = scan.healthScore.toInt()
        val healthBar = findViewById<android.widget.ProgressBar>(R.id.healthScoreProgress)
        healthBar.progress = healthScore
        // Color the progress bar based on score
        val hsColor = when {
            healthScore < 30 -> Color.parseColor("#DE0000")
            healthScore < 70 -> Color.parseColor("#FFCC00")
            else -> Color.parseColor("#67BA2F")
        }
        val hsDrawable = healthBar.progressDrawable as? android.graphics.drawable.LayerDrawable
        hsDrawable?.findDrawableByLayerId(android.R.id.progress)?.setTint(hsColor)

        // Position score text inside the bar, near the end of the fill
        val tvScoreValue = findViewById<TextView>(R.id.tvHealthScoreValue)
        tvScoreValue.text = healthScore.toString()
        healthBar.post {
            val barWidth = healthBar.width
            val fillEnd = barWidth * healthScore / 100f
            val textWidth = tvScoreValue.paint.measureText(healthScore.toString())
            val padding = 8 * resources.displayMetrics.density
            val x = (fillEnd - textWidth - padding).coerceAtLeast(padding)
            tvScoreValue.translationX = x
        }

        findViewById<TextView>(R.id.tvHealthScoreTitle).setOnClickListener {
            showInfo("Health Score", getString(R.string.health_score_desc))
        }

        // Body Type Matrix
        val axisX = scan.rawValues[FieldMapping.BODY_TYPE_ANALYSIS_AXIS_X]
        val axisY = scan.rawValues[FieldMapping.BODY_TYPE_ANALYSIS_AXIS_Y]
        val col = axisX.toInt().coerceIn(0, 2)
        val row = (3f - axisY).toInt().coerceIn(0, 2)
        val matrixIds = arrayOf(
            intArrayOf(R.id.matrixA1, R.id.matrixA2, R.id.matrixA3),
            intArrayOf(R.id.matrixB1, R.id.matrixB2, R.id.matrixB3),
            intArrayOf(R.id.matrixC1, R.id.matrixC2, R.id.matrixC3)
        )
        for (r in 0..2) {
            for (c in 0..2) {
                val tv = findViewById<TextView>(matrixIds[r][c])
                if (r == row && c == col) {
                    tv.setBackgroundColor(Color.parseColor("#1976D2"))
                    tv.setTextColor(Color.WHITE)
                    tv.setTypeface(null, android.graphics.Typeface.BOLD)
                } else {
                    tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                }
            }
        }

        // Lean mass body view
        findViewById<BodyHumanView>(R.id.bodyLeanMass).setSegmentData(
            leftArm = scan.leftArmLeanMass,
            rightArm = scan.rightArmLeanMass,
            trunk = scan.trunkLeanMass,
            leftLeg = scan.leftLegLeanMass,
            rightLeg = scan.rightLegLeanMass,
            unit = "kg"
        )

        // Fat mass body view
        findViewById<BodyHumanView>(R.id.bodyFatMass).setSegmentData(
            leftArm = scan.leftArmFatMass,
            rightArm = scan.rightArmFatMass,
            trunk = scan.trunkFatMass,
            leftLeg = scan.leftLegFatMass,
            rightLeg = scan.rightLegFatMass,
            unit = "kg"
        )

        // Segmental ratings with toggle
        findViewById<TextView>(R.id.btnToggleRatingMode).setOnClickListener {
            useRadarMode = !useRadarMode
            currentScan?.let { rebuildSegmentalRatings(it) }
        }
        if (!scan.hasExtendedData) {
            findViewById<TextView>(R.id.btnToggleRatingMode).visibility = View.GONE
            useRadarMode = true
        }
        rebuildSegmentalRatings(scan)

        // Raw data expandable section
        val tvRawDataToggle = findViewById<TextView>(R.id.tvRawDataToggle)
        val tvRawData = findViewById<TextView>(R.id.tvRawData)

        val rawDataText = buildRawDataText(scan)
        tvRawData.text = rawDataText

        tvRawDataToggle.setOnClickListener {
            if (tvRawData.visibility == View.GONE) {
                tvRawData.visibility = View.VISIBLE
                tvRawDataToggle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, AppCompatResources.getDrawable(this, R.drawable.ic_arrow_up), null
                )
            } else {
                tvRawData.visibility = View.GONE
                tvRawDataToggle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, AppCompatResources.getDrawable(this, R.drawable.ic_arrow_down), null
                )
            }
        }
    }

    private fun showInfo(title: String, description: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(description)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun visceralFatText(level: Int): String = when (level) {
        1 -> "Very good"
        2 -> "Good"
        3 -> "Over standard"
        4 -> "High"
        5 -> "Very high"
        else -> "Unknown"
    }

    private fun visceralFatColor(level: Int): Int = when (level) {
        1, 2 -> Color.parseColor("#4CAF50")
        3 -> Color.parseColor("#FFC107")
        else -> Color.parseColor("#F44336")
    }

    private fun createRatingRow(
        label: String, value: Float, unit: String, radar: Float,
        blockIndex: Int, thresholds: DoubleArray?
    ): View {
        val outer = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(0, (4 * resources.displayMetrics.density).toInt(), 0, (4 * resources.displayMetrics.density).toInt())
        }
        val dp = resources.displayMetrics.density

        // Top row: label + value + radar bar + radar rating
        val row = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        row.addView(TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(ContextCompat.getColor(this@ScanResultActivity, R.color.text_primary))
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
        })

        row.addView(TextView(this).apply {
            text = String.format("%.1f %s", value, unit)
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@ScanResultActivity, R.color.text_secondary))
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f)
            gravity = android.view.Gravity.CENTER
        })

        // Bar with normal range zone + indicator
        val barContainer = android.widget.FrameLayout(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(0, (18 * dp).toInt(), 2f)
        }

        // Background
        barContainer.addView(View(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@ScanResultActivity, R.color.divider))
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT, (8 * dp).toInt(),
                android.view.Gravity.CENTER_VERTICAL
            )
        })

        // Normal range zone (green band) - thresholds[1]..thresholds[4] = standard zone
        if (thresholds != null) {
            val scaleMin = (thresholds[0] * 0.85).toFloat()
            val scaleMax = (thresholds[5] * 1.15).toFloat()
            val scaleRange = scaleMax - scaleMin

            // Green normal zone
            barContainer.post {
                val w = barContainer.width
                val normStart = ((thresholds[1] - scaleMin) / scaleRange).coerceIn(0.0, 1.0)
                val normEnd = ((thresholds[4] - scaleMin) / scaleRange).coerceIn(0.0, 1.0)
                val zoneView = View(this).apply {
                    setBackgroundColor(Color.parseColor("#C8E6C9"))
                }
                val lp = android.widget.FrameLayout.LayoutParams(
                    ((normEnd - normStart) * w).toInt(), (8 * dp).toInt(),
                    android.view.Gravity.CENTER_VERTICAL
                )
                lp.marginStart = (normStart * w).toInt()
                zoneView.layoutParams = lp
                barContainer.addView(zoneView, 1)

                // Value indicator
                val valueFrac = ((value - scaleMin) / scaleRange).coerceIn(0f, 1f)
                val ind = View(this).apply { setBackgroundColor(Color.parseColor("#1976D2")) }
                val indLp = android.widget.FrameLayout.LayoutParams(
                    (6 * dp).toInt(), (16 * dp).toInt(), android.view.Gravity.CENTER_VERTICAL
                )
                ind.layoutParams = indLp
                barContainer.addView(ind)
                ind.translationX = valueFrac * (w - 6 * dp)
            }
        } else {
            // Radar-only bar (for fat mass where we don't have lean thresholds)
            val fraction = ((radar + 3f) / 6f).coerceIn(0f, 1f)
            val indicatorColor = when {
                radar < -1f -> Color.parseColor("#F44336")
                radar < 0f -> Color.parseColor("#FFC107")
                radar < 1f -> Color.parseColor("#4CAF50")
                else -> Color.parseColor("#1976D2")
            }
            val indicator = View(this).apply { setBackgroundColor(indicatorColor) }
            indicator.layoutParams = android.widget.FrameLayout.LayoutParams(
                (6 * dp).toInt(), (16 * dp).toInt(), android.view.Gravity.CENTER_VERTICAL
            )
            barContainer.addView(indicator)
            barContainer.post {
                indicator.translationX = fraction * (barContainer.width - 6 * dp)
            }
            // Center line
            barContainer.addView(View(this).apply {
                setBackgroundColor(Color.parseColor("#9E9E9E"))
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    (1 * dp).toInt(), (14 * dp).toInt(), android.view.Gravity.CENTER
                )
            })
        }

        row.addView(barContainer)

        // Rating label: show active mode only
        val radarText = when {
            radar < -1f -> "Low"
            radar < 0f -> "Below"
            radar < 1f -> "Normal"
            radar < 2f -> "Above"
            else -> "High"
        }
        val radarColor = when {
            radar < -1f -> Color.parseColor("#F44336")
            radar < 0f -> Color.parseColor("#FFC107")
            radar < 1f -> Color.parseColor("#4CAF50")
            else -> Color.parseColor("#1976D2")
        }

        val ratingLabel: String
        val ratingColor: Int
        if (blockIndex >= 0) {
            ratingLabel = SegmentalRating.blockLabel(blockIndex)
            ratingColor = when (blockIndex) {
                0, 1 -> Color.parseColor("#F44336")
                2 -> Color.parseColor("#FFC107")
                3 -> Color.parseColor("#4CAF50")
                else -> Color.parseColor("#1976D2")
            }
        } else {
            ratingLabel = radarText
            ratingColor = radarColor
        }

        row.addView(TextView(this).apply {
            text = ratingLabel
            textSize = 11f
            setTextColor(ratingColor)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            gravity = android.view.Gravity.END
        })

        outer.addView(row)
        return outer
    }

    private fun buildRawDataText(scan: ScanData): String {
        val sb = StringBuilder()
        for (i in scan.rawValues.indices) {
            val name = if (i < FieldMapping.FIELD_NAMES.size) {
                FieldMapping.FIELD_NAMES[i]
            } else {
                "Field $i"
            }
            val value = scan.rawValues[i]
            val displayValue = if (value == value.toInt().toFloat()) {
                value.toInt().toString()
            } else {
                String.format("%.2f", value)
            }
            sb.append(String.format("%2d. %-35s %s", i, name, displayValue))
            if (i < scan.rawValues.size - 1) sb.append("\n")
        }
        return sb.toString()
    }
}
