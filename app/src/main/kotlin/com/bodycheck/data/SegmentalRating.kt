package com.bodycheck.data

import kotlin.math.roundToInt

object SegmentalRating {

    enum class Segment { ARM, LEG, TORSO }

    // Threshold tables: [segment][massRange][gender] → 6 base coefficients
    // massRange: 0 = normalizedMass < 25, 1 = 25-29, 2 = >= 30
    // gender: 0 = male, 1 = female
    // Thresholds define 6 boundaries between block indices 0-5

    private val ARM = arrayOf(
        // < 25
        arrayOf(
            doubleArrayOf(0.488, 0.519, 0.549, 0.580, 0.611, 0.641), // male
            doubleArrayOf(0.340, 0.372, 0.398, 0.423, 0.448, 0.474)  // female
        ),
        // 25-29
        arrayOf(
            doubleArrayOf(0.529, 0.562, 0.595, 0.628, 0.660, 0.693),
            doubleArrayOf(0.359, 0.389, 0.418, 0.448, 0.477, 0.507)
        ),
        // >= 30
        arrayOf(
            doubleArrayOf(0.564, 0.604, 0.643, 0.683, 0.722, 0.762),
            doubleArrayOf(0.373, 0.407, 0.441, 0.476, 0.510, 0.544)
        )
    )

    private val LEG = arrayOf(
        arrayOf(
            doubleArrayOf(1.699, 1.774, 1.848, 1.923, 1.997, 2.072),
            doubleArrayOf(1.413, 1.491, 1.568, 1.646, 1.723, 1.801)
        ),
        arrayOf(
            doubleArrayOf(1.831, 1.916, 2.002, 2.087, 2.172, 2.258),
            doubleArrayOf(1.571, 1.654, 1.737, 1.820, 1.903, 1.986)
        ),
        arrayOf(
            doubleArrayOf(2.042, 2.150, 2.257, 2.365, 2.472, 2.580),
            doubleArrayOf(1.730, 1.838, 1.946, 2.055, 2.163, 2.271)
        )
    )

    private val TORSO = arrayOf(
        arrayOf(
            doubleArrayOf(3.82, 3.98, 4.13, 4.29, 4.45, 4.60),
            doubleArrayOf(2.60, 2.76, 2.91, 3.07, 3.23, 3.38)
        ),
        arrayOf(
            doubleArrayOf(4.19, 4.34, 4.49, 4.64, 4.79, 4.94),
            doubleArrayOf(3.41, 3.49, 3.58, 3.66, 3.74, 3.83)
        ),
        arrayOf(
            doubleArrayOf(4.55, 4.73, 4.91, 5.10, 5.28, 5.46),
            doubleArrayOf(3.83, 3.94, 4.05, 4.17, 4.28, 4.39)
        )
    )

    private fun getTable(segment: Segment) = when (segment) {
        Segment.ARM -> ARM
        Segment.LEG -> LEG
        Segment.TORSO -> TORSO
    }

    /**
     * Get the 6 threshold values in kg for a given segment.
     * @param segment ARM, LEG, or TORSO
     * @param skeletalMuscleMass total skeletal muscle mass in kg
     * @param heightCm user height in cm
     * @param isMale true for male
     * @return 6 thresholds in kg: [0-1]=below, [2-3]=normal, [4-5]=above
     */
    fun getThresholdsKg(
        segment: Segment,
        skeletalMuscleMass: Float,
        heightCm: Float,
        isMale: Boolean
    ): DoubleArray {
        val h100sq = (heightCm / 100.0) * (heightCm / 100.0)
        val normalizedMass = (skeletalMuscleMass / h100sq).roundToInt()
        val rangeIndex = when {
            normalizedMass < 25 -> 0
            normalizedMass < 30 -> 1
            else -> 2
        }
        val genderIndex = if (isMale) 0 else 1
        val base = getTable(segment)[rangeIndex][genderIndex]
        return DoubleArray(6) { base[it] * h100sq }
    }

    /**
     * Get block index 0-5 for a segmental value against thresholds.
     * 0 = way below standard, 5 = way above standard.
     */
    fun getBlockIndex(value: Float, thresholds: DoubleArray): Int {
        for (i in 5 downTo 0) {
            when {
                i == 5 && value > thresholds[5] -> return 5
                i == 0 && value < thresholds[0] -> return 0
                i in 1..4 && value >= thresholds[i] -> return i
            }
        }
        return 0
    }

    /**
     * Derive a block index (0-5) from a radar value.
     * Useful for 64-element QR codes that lack explicit assessment fields.
     */
    fun radarToBlockIndex(radar: Float): Int = when {
        radar < -1.5f -> 0  // Very Low
        radar < -0.5f -> 1  // Low
        radar < 0.5f  -> 2  // Below Avg
        radar < 1.0f  -> 3  // Normal
        radar < 2.0f  -> 4  // Above Avg
        else          -> 5  // High
    }

    fun blockLabel(index: Int): String = when (index) {
        0 -> "Very Low"
        1 -> "Low"
        2 -> "Below Avg"
        3 -> "Normal"
        4 -> "Above Avg"
        5 -> "High"
        else -> "?"
    }
}
