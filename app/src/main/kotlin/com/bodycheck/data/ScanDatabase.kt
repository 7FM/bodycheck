package com.bodycheck.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

class ScanDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE $TABLE_SCANS (
                $_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SCAN_DATE INTEGER NOT NULL,
                $COL_RAW_DATA TEXT NOT NULL,
                $COL_WEIGHT REAL,
                $COL_BMI REAL,
                $COL_HEALTH_SCORE REAL
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SCANS")
        onCreate(db)
    }

    fun insertScan(scanData: ScanData): Long {
        val rawJson = scanData.rawValues.joinToString(",", "[", "]")
        val values = ContentValues().apply {
            put(COL_SCAN_DATE, scanData.scanDate)
            put(COL_RAW_DATA, rawJson)
            put(COL_WEIGHT, scanData.weight)
            put(COL_BMI, scanData.bmi)
            put(COL_HEALTH_SCORE, scanData.healthScore)
        }
        return writableDatabase.insert(TABLE_SCANS, null, values)
    }

    fun getAllScans(): List<ScanData> {
        val scans = mutableListOf<ScanData>()
        val cursor = readableDatabase.query(
            TABLE_SCANS, null, null, null, null, null,
            "$COL_SCAN_DATE DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                parseCursorRow(it)?.let { scan -> scans.add(scan) }
            }
        }
        return scans
    }

    fun getScanById(id: Long): ScanData? {
        val cursor = readableDatabase.query(
            TABLE_SCANS, null, "$_ID = ?", arrayOf(id.toString()),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) return parseCursorRow(it)
        }
        return null
    }

    fun deleteScan(id: Long) {
        writableDatabase.delete(TABLE_SCANS, "$_ID = ?", arrayOf(id.toString()))
    }

    fun exportAllAsJson(): String {
        val scans = getAllScans()
        val jsonArray = JSONArray()
        for (scan in scans) {
            val obj = JSONObject()
            obj.put("scan_date", scan.scanDate)
            val rawArray = JSONArray()
            for (v in scan.rawValues) {
                rawArray.put(v.toDouble())
            }
            obj.put("raw_data", rawArray)
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    fun importFromJson(json: String): Int {
        val jsonArray = JSONArray(json)
        var count = 0
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val scanDate = obj.getLong("scan_date")
            val rawDataArray = obj.getJSONArray("raw_data")
            val values = FloatArray(rawDataArray.length())
            for (j in 0 until rawDataArray.length()) {
                values[j] = rawDataArray.getDouble(j).toFloat()
            }

            // Check for duplicate by scan_date
            val cursor = readableDatabase.query(
                TABLE_SCANS, arrayOf(_ID),
                "$COL_SCAN_DATE = ?", arrayOf(scanDate.toString()),
                null, null, null
            )
            val exists = cursor.use { it.moveToFirst() }
            if (exists) continue

            val scanData = ScanData(rawValues = values, scanDate = scanDate)
            insertScan(scanData)
            count++
        }
        return count
    }

    private fun parseCursorRow(cursor: android.database.Cursor): ScanData? {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID))
        val scanDate = cursor.getLong(cursor.getColumnIndexOrThrow(COL_SCAN_DATE))
        val rawData = cursor.getString(cursor.getColumnIndexOrThrow(COL_RAW_DATA))
        val stripped = rawData.removePrefix("[").removeSuffix("]")
        val tokens = stripped.split(",").map { it.trim() }
        if (tokens.isEmpty()) return null

        val values = FloatArray(tokens.size)
        for (i in tokens.indices) {
            values[i] = tokens[i].toFloatOrNull() ?: 0f
        }
        return ScanData(rawValues = values, scanDate = scanDate, id = id)
    }

    companion object {
        private const val DATABASE_NAME = "bodycheck.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_SCANS = "scans"
        private const val _ID = "_id"
        private const val COL_SCAN_DATE = "scan_date"
        private const val COL_RAW_DATA = "raw_data"
        private const val COL_WEIGHT = "weight"
        private const val COL_BMI = "bmi"
        private const val COL_HEALTH_SCORE = "health_score"
    }
}
