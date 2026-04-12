package com.bodycheck.data

data class ScanData(
    val rawValues: FloatArray,
    val scanDate: Long,
    val id: Long = -1
) {
    private fun safeGet(index: Int): Float = if (index < rawValues.size) rawValues[index] else 0f

    val weight: Float get() = rawValues[FieldMapping.WEIGHT]
    val freeFatMass: Float get() = rawValues[FieldMapping.FREE_FAT_MASS]
    val fatMass: Float get() = rawValues[FieldMapping.FM_FAT_MASS]
    val percentBodyFat: Float get() = rawValues[FieldMapping.PERCENTAGE_BODY_FAT]
    val muscleMass: Float get() = rawValues[FieldMapping.SKELETAL_MUSCLE_MASS]
    val totalBodyWater: Float get() = rawValues[FieldMapping.TOTAL_BODY_WATER]
    val healthScore: Float get() = rawValues[FieldMapping.HEALTH_SCORE]
    val bmi: Float get() = rawValues[FieldMapping.BODY_MASS_INDEX]
    val biologicalAge: Float get() = rawValues[FieldMapping.BIOLOGICAL_AGE]
    val bmr: Float get() = rawValues[FieldMapping.BASAL_METABOLIC_RATE]
    val height: Float get() = rawValues[FieldMapping.USER_HEIGHT]
    val proteinMass: Float get() = rawValues[FieldMapping.PROTEIN_MASS]
    val boneMineral: Float get() = rawValues[FieldMapping.BONE_MINERAL_CONTENT]
    val leanMass: Float get() = rawValues[FieldMapping.LEAN_MASS]
    val waistHipRatio: Float get() = rawValues[FieldMapping.WAIST_HIP_RATIO]
    val muscleQuality: Float get() = rawValues[FieldMapping.MUSCLE_QUALITY]
    val visceralFatLevel: Int get() = rawValues[FieldMapping.MUSCLE_MASS_ASSESSMENT].toInt()

    val rightArmLeanMass: Float get() = rawValues[FieldMapping.RIGHT_ARM_LEAN_MASS]
    val leftArmLeanMass: Float get() = rawValues[FieldMapping.LEFT_ARM_LEAN_MASS]
    val trunkLeanMass: Float get() = rawValues[FieldMapping.TRUNK_LEAN_MASS]
    val rightLegLeanMass: Float get() = rawValues[FieldMapping.RIGHT_LEG_LEAN_MASS]
    val leftLegLeanMass: Float get() = rawValues[FieldMapping.LEFT_LEG_LEAN_MASS]

    val rightArmFatMass: Float get() = rawValues[FieldMapping.RIGHT_ARM_FAT_MASS]
    val leftArmFatMass: Float get() = rawValues[FieldMapping.LEFT_ARM_FAT_MASS]
    val trunkFatMass: Float get() = rawValues[FieldMapping.TRUNK_FAT_MASS]
    val rightLegFatMass: Float get() = rawValues[FieldMapping.RIGHT_LEG_FAT_MASS]
    val leftLegFatMass: Float get() = rawValues[FieldMapping.LEFT_LEG_FAT_MASS]

    // Extended data (95-element QR codes)
    val hasExtendedData: Boolean get() = rawValues.size > 64

    val bodyCellMass: Float get() = safeGet(FieldMapping.BODY_CELL_MASS)
    val lowBodyCellMass: Float get() = safeGet(FieldMapping.LOW_BODY_CELL_MASS)
    val highBodyCellMass: Float get() = safeGet(FieldMapping.HIGH_BODY_CELL_MASS)
    val lowTotalBodyWaterValue: Float get() = safeGet(FieldMapping.LOW_TOTAL_BODY_WATER_VALUE)
    val highTotalBodyWaterValue: Float get() = safeGet(FieldMapping.HIGH_TOTAL_BODY_WATER_VALUE)
    val lowMuscleMass: Float get() = safeGet(FieldMapping.LOW_MUSCLE_MASS)
    val highMuscleMass: Float get() = safeGet(FieldMapping.HIGH_MUSCLE_MASS)
    val muscleMassStandardRatio: Float get() = safeGet(FieldMapping.MUSCLE_MASS_STANDARD_RATIO)
    val lowLeanBodyMass: Float get() = safeGet(FieldMapping.LOW_LEAN_BODY_MASS)
    val highLeanBodyMass: Float get() = safeGet(FieldMapping.HIGH_LEAN_BODY_MASS)
    val leanBodyMassStandardRatio: Float get() = safeGet(FieldMapping.LEAN_BODY_MASS_STANDARD_RATIO)
    val lowBasalMetabolism: Float get() = safeGet(FieldMapping.LOW_BASAL_METABOLISM)
    val highBasalMetabolicRate: Float get() = safeGet(FieldMapping.HIGH_BASAL_METABOLIC_RATE)
    val dailyCaloricRequirements: Float get() = safeGet(FieldMapping.DAILY_CALORIC_REQUIREMENTS)
    val recommendedDailyCalories: Float get() = safeGet(FieldMapping.RECOMMENDED_DAILY_CALORIES)
    val obesity: Float get() = safeGet(FieldMapping.OBESITY)
    val wholeBodyPhase: Float get() = safeGet(FieldMapping.WHOLE_BODY_PHASE)
    val visceralFatArea: Float get() = safeGet(FieldMapping.VISCERAL_FAT_AREA)
    val visceralFatAreaAssessment: Float get() = safeGet(FieldMapping.VISCERAL_FAT_AREA_ASSESSMENT)
    val weightAssessment: Float get() = safeGet(FieldMapping.WEIGHT_ASSESSMENT)
    val bodyFatAssessment: Float get() = safeGet(FieldMapping.BODY_FAT_ASSESSMENT)
    val skeletalMuscleAssessment: Float get() = safeGet(FieldMapping.SKELETAL_MUSCLE_ASSESSMENT)
    val proteinAssessment: Float get() = safeGet(FieldMapping.PROTEIN_ASSESSMENT)
    val inorganicSaltEvaluation: Float get() = safeGet(FieldMapping.INORGANIC_SALT_EVALUATION)
    val bodyMassIndexAssessment: Float get() = safeGet(FieldMapping.BODY_MASS_INDEX_ASSESSMENT)
    val bodyFatPercentageAssessment: Float get() = safeGet(FieldMapping.BODY_FAT_PERCENTAGE_ASSESSMENT)
    val upperLimbBalanceAssessment: Float get() = safeGet(FieldMapping.UPPER_LIMB_BALANCE_ASSESSMENT)
    val lowerLimbBalanceAssessment: Float get() = safeGet(FieldMapping.LOWER_LIMB_BALANCE_ASSESSMENT)
    val upperAndLowerLimbBalanceAssessment: Float get() = safeGet(FieldMapping.UPPER_AND_LOWER_LIMB_BALANCE_ASSESSMENT)
    val userAge: Float get() = safeGet(FieldMapping.USER_AGE)
    val userGender: Float get() = safeGet(FieldMapping.USER_GENDER)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ScanData) return false
        return rawValues.contentEquals(other.rawValues) && scanDate == other.scanDate && id == other.id
    }

    override fun hashCode(): Int {
        var result = rawValues.contentHashCode()
        result = 31 * result + scanDate.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    companion object {
        private val VALID_LENGTHS = setOf(63, 64, 95)

        fun parse(qrContent: String): ScanData? {
            return try {
                val stripped = qrContent.trim().removePrefix("[").removeSuffix("]")
                val tokens = stripped.split(",").map { it.trim() }
                if (tokens.size !in VALID_LENGTHS) return null

                val values = FloatArray(tokens.size)
                for (i in tokens.indices) {
                    values[i] = tokens[i].toFloatOrNull() ?: 0f
                }

                // Sanity checks - values must be in reasonable ranges
                val weight = values[0]  // FieldMapping.WEIGHT
                val height = values[46] // FieldMapping.USER_HEIGHT
                val bmi = values[36]    // FieldMapping.BODY_MASS_INDEX
                val bodyFat = values[3] // FieldMapping.PERCENTAGE_BODY_FAT
                val score = values[6]   // FieldMapping.HEALTH_SCORE

                if (weight !in 20f..400f) return null
                if (height !in 50f..250f) return null
                if (bmi !in 10f..60f) return null
                if (bodyFat !in 0f..80f) return null
                if (score !in 0f..100f) return null

                ScanData(rawValues = values, scanDate = System.currentTimeMillis())
            } catch (_: Exception) {
                null
            }
        }
    }
}
