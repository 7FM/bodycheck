package com.bodycheck.data

object FieldMapping {
    const val WEIGHT = 0
    const val FREE_FAT_MASS = 1
    const val FM_FAT_MASS = 2
    const val PERCENTAGE_BODY_FAT = 3
    const val SKELETAL_MUSCLE_MASS = 4
    const val TOTAL_BODY_WATER = 5
    const val HEALTH_SCORE = 6
    const val LEFT_ARM_LEAN_MASS_RADAR = 7
    const val RIGHT_ARM_LEAN_MASS_RADAR = 8
    const val TRUNK_LEAN_MASS_RADAR = 9
    const val LEFT_LEG_LEAN_MASS_RADAR = 10
    const val RIGHT_LEG_LEAN_MASS_RADAR = 11
    const val LEFT_ARM_FAT_MASS_RADAR = 12
    const val RIGHT_ARM_FAT_MASS_RADAR = 13
    const val TRUNK_FAT_MASS_RADAR = 14
    const val LEFT_LEG_FAT_MASS_RADAR = 15
    const val RIGHT_LEG_FAT_MASS_RADAR = 16
    const val BODY_TYPE_ANALYSIS_AXIS_X = 17
    const val BODY_TYPE_ANALYSIS_AXIS_Y = 18
    const val RIGHT_ARM_LEAN_MASS = 19
    const val LEFT_ARM_LEAN_MASS = 20
    const val TRUNK_LEAN_MASS = 21
    const val RIGHT_LEG_LEAN_MASS = 22
    const val LEFT_LEG_LEAN_MASS = 23
    const val RIGHT_ARM_FAT_MASS = 24
    const val LEFT_ARM_FAT_MASS = 25
    const val TRUNK_FAT_MASS = 26
    const val RIGHT_LEG_FAT_MASS = 27
    const val LEFT_LEG_FAT_MASS = 28
    const val LEAN_MASS = 29
    const val BODY_TYPE = 30
    const val BONE_MINERAL_CONTENT = 31
    const val INTRACELLULAR_WATER_MASS = 32
    const val EXTRACELLULAR_WATER_MASS = 33
    const val PROTEIN_MASS = 34
    const val BASAL_METABOLIC_RATE = 35
    const val BODY_MASS_INDEX = 36
    const val IDEAL_WEIGHT = 37
    const val WEIGHT_STANDARDIZATION = 38
    const val FM_STANDARDIZATION = 39
    const val SMM_STANDARDIZATION = 40
    const val WEIGHT_CONTROL_WC = 41
    const val FAT_CONTROL_FC = 42
    const val LEAN_CONTROL = 43
    const val MUSCLE_QUALITY = 44
    const val WAIST_HIP_RATIO = 45
    const val USER_HEIGHT = 46
    const val FAT_MASS_NORMAL_RANGE_LOWER_LIMIT = 47
    const val FAT_MASS_NORMAL_RANGE_UPPER_LIMIT = 48
    const val MINERAL_NORMAL_RANGE_LOWER_LIMIT = 49
    const val MINERAL_NORMAL_RANGE_UPPER_LIMIT = 50
    const val PROTEIN_NORMAL_RANGE_LOWER_LIMIT = 51
    const val PROTEIN_NORMAL_RANGE_UPPER_LIMIT = 52
    const val ECW_NORMAL_RANGE_LOWER_LIMIT = 53
    const val ECW_NORMAL_RANGE_UPPER_LIMIT = 54
    const val ICW_RANGE_LOWER = 55
    const val ICW_RANGE_UPPER = 56
    const val ECW_PERCENT = 57
    const val ICW_PERCENT = 58
    const val BMC_ALT = 59
    const val PROTEIN_PERCENT = 60
    const val BIOLOGICAL_AGE = 61
    const val MUSCLE_MASS_ASSESSMENT = 62
    const val VERSION = 63

    // Extended fields (95-element QR codes, indices 64-94)
    const val BODY_CELL_MASS = 64
    const val LOW_BODY_CELL_MASS = 65
    const val HIGH_BODY_CELL_MASS = 66
    const val LOW_TOTAL_BODY_WATER_VALUE = 67
    const val HIGH_TOTAL_BODY_WATER_VALUE = 68
    const val LOW_MUSCLE_MASS = 69
    const val HIGH_MUSCLE_MASS = 70
    const val MUSCLE_MASS_STANDARD_RATIO = 71
    const val LOW_LEAN_BODY_MASS = 72
    const val HIGH_LEAN_BODY_MASS = 73
    const val LEAN_BODY_MASS_STANDARD_RATIO = 74
    const val LOW_BASAL_METABOLISM = 75
    const val HIGH_BASAL_METABOLIC_RATE = 76
    const val DAILY_CALORIC_REQUIREMENTS = 77
    const val RECOMMENDED_DAILY_CALORIES = 78
    const val OBESITY = 79
    const val WHOLE_BODY_PHASE = 80
    const val VISCERAL_FAT_AREA = 81
    const val VISCERAL_FAT_AREA_ASSESSMENT = 82
    const val WEIGHT_ASSESSMENT = 83
    const val BODY_FAT_ASSESSMENT = 84
    const val SKELETAL_MUSCLE_ASSESSMENT = 85
    const val PROTEIN_ASSESSMENT = 86
    const val INORGANIC_SALT_EVALUATION = 87
    const val BODY_MASS_INDEX_ASSESSMENT = 88
    const val BODY_FAT_PERCENTAGE_ASSESSMENT = 89
    const val UPPER_LIMB_BALANCE_ASSESSMENT = 90
    const val LOWER_LIMB_BALANCE_ASSESSMENT = 91
    const val UPPER_AND_LOWER_LIMB_BALANCE_ASSESSMENT = 92
    const val USER_AGE = 93
    const val USER_GENDER = 94

    val FIELD_NAMES = arrayOf(
        "Weight (kg)",                        // 0
        "Free Fat Mass (kg)",                 // 1
        "Fat Mass (kg)",                      // 2
        "Body Fat (%)",                       // 3
        "Skeletal Muscle Mass (kg)",          // 4
        "Total Body Water (L)",               // 5
        "Health Score",                       // 6
        "Left Arm Lean Mass Radar",           // 7
        "Right Arm Lean Mass Radar",          // 8
        "Trunk Lean Mass Radar",              // 9
        "Left Leg Lean Mass Radar",           // 10
        "Right Leg Lean Mass Radar",          // 11
        "Left Arm Fat Mass Radar",            // 12
        "Right Arm Fat Mass Radar",           // 13
        "Trunk Fat Mass Radar",               // 14
        "Left Leg Fat Mass Radar",            // 15
        "Right Leg Fat Mass Radar",           // 16
        "Body Type Analysis X",               // 17
        "Body Type Analysis Y",               // 18
        "Right Arm Lean Mass (kg)",           // 19
        "Left Arm Lean Mass (kg)",            // 20
        "Trunk Lean Mass (kg)",               // 21
        "Right Leg Lean Mass (kg)",           // 22
        "Left Leg Lean Mass (kg)",            // 23
        "Right Arm Fat Mass (kg)",            // 24
        "Left Arm Fat Mass (kg)",             // 25
        "Trunk Fat Mass (kg)",                // 26
        "Right Leg Fat Mass (kg)",            // 27
        "Left Leg Fat Mass (kg)",             // 28
        "Lean Mass (kg)",                     // 29
        "Body Type",                          // 30
        "Bone Mineral Content (kg)",          // 31
        "Intracellular Water (L)",            // 32
        "Extracellular Water (L)",            // 33
        "Protein Mass (kg)",                  // 34
        "Basal Metabolic Rate (kcal/day)",    // 35
        "BMI",                                // 36
        "Ideal Weight (kg)",                  // 37
        "Weight Standardization (%)",         // 38
        "Fat Mass Standardization (%)",       // 39
        "SMM Standardization (%)",            // 40
        "Weight Control (kg)",                // 41
        "Fat Control (kg)",                   // 42
        "Lean Control (kg)",                  // 43
        "Muscle Quality",                     // 44
        "Waist-Hip Ratio",                    // 45
        "Height (cm)",                        // 46
        "Fat Mass Normal Lower",              // 47
        "Fat Mass Normal Upper",              // 48
        "Mineral Normal Lower",               // 49
        "Mineral Normal Upper",               // 50
        "Protein Normal Lower",               // 51
        "Protein Normal Upper",               // 52
        "ECW Normal Lower",                   // 53
        "ECW Normal Upper",                   // 54
        "ICW Normal Lower",                   // 55
        "ICW Normal Upper",                   // 56
        "ECW (%)",                            // 57
        "ICW (%)",                            // 58
        "Bone Mineral Content",               // 59
        "Protein (%)",                        // 60
        "Biological Age",                     // 61
        "Muscle Mass Assessment",             // 62
        "Version",                            // 63
        "Body Cell Mass (kg)",                // 64
        "Low Body Cell Mass",                 // 65
        "High Body Cell Mass",                // 66
        "Low Total Body Water Value",         // 67
        "High Total Body Water Value",        // 68
        "Low Muscle Mass",                    // 69
        "High Muscle Mass",                   // 70
        "Muscle Mass Standard Ratio",         // 71
        "Low Lean Body Mass",                 // 72
        "High Lean Body Mass",                // 73
        "Lean Body Mass Standard Ratio",      // 74
        "Low Basal Metabolism",               // 75
        "High Basal Metabolic Rate",          // 76
        "Daily Caloric Requirements",         // 77
        "Recommended Daily Calories",         // 78
        "Obesity",                            // 79
        "Whole Body Phase",                   // 80
        "Visceral Fat Area",                  // 81
        "Visceral Fat Area Assessment",       // 82
        "Weight Assessment",                  // 83
        "Body Fat Assessment",                // 84
        "Skeletal Muscle Assessment",         // 85
        "Protein Assessment",                 // 86
        "Inorganic Salt Evaluation",          // 87
        "BMI Assessment",                     // 88
        "Body Fat Percentage Assessment",     // 89
        "Upper Limb Balance Assessment",      // 90
        "Lower Limb Balance Assessment",      // 91
        "Upper & Lower Limb Balance",         // 92
        "User Age",                           // 93
        "User Gender"                         // 94
    )
}
