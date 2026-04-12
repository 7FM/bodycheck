package com.bodycheck.backup

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FullBackupDataOutput
import android.os.ParcelFileDescriptor
import android.util.Log
import com.bodycheck.data.ScanDatabase
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BodyCheckBackupAgent : BackupAgent() {

    companion object {
        private const val TAG = "BodyCheckBackup"
        private const val BACKUP_FILENAME = "bodycheck_backup.json"
        private const val PREFS_FILENAME = "bodycheck_prefs.json"
    }

    override fun onFullBackup(data: FullBackupDataOutput) {
        val db = ScanDatabase(this)
        try {
            val json = db.exportAllAsJson()
            val tempFile = File(filesDir, BACKUP_FILENAME)
            FileOutputStream(tempFile).use { it.write(json.toByteArray()) }
            fullBackupFile(tempFile, data)
            tempFile.delete()

            // Backup preferences (gender)
            val prefs = getSharedPreferences("bodycheck_prefs", MODE_PRIVATE)
            val prefsJson = JSONObject()
            prefsJson.put("gender", prefs.getString("gender", "male"))
            val prefsFile = File(filesDir, PREFS_FILENAME)
            FileOutputStream(prefsFile).use { it.write(prefsJson.toString().toByteArray()) }
            fullBackupFile(prefsFile, data)
            prefsFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "onFullBackup failed", e)
        } finally {
            db.close()
        }
    }

    override fun onRestoreFile(
        data: ParcelFileDescriptor,
        size: Long,
        destination: File,
        type: Int,
        mode: Long,
        mtime: Long
    ) {
        super.onRestoreFile(data, size, destination, type, mode, mtime)
    }

    override fun onRestoreFinished() {
        val tempFile = File(filesDir, BACKUP_FILENAME)
        if (tempFile.exists()) {
            try {
                val json = FileInputStream(tempFile).bufferedReader().readText()
                val db = ScanDatabase(this)
                val count = db.importFromJson(json)
                db.close()
                Log.d(TAG, "Restored $count scans from backup")
            } catch (e: Exception) {
                Log.e(TAG, "Restore from JSON failed", e)
            } finally {
                tempFile.delete()
            }
        }

        // Restore preferences
        val prefsFile = File(filesDir, PREFS_FILENAME)
        if (prefsFile.exists()) {
            try {
                val prefsJson = JSONObject(FileInputStream(prefsFile).bufferedReader().readText())
                val prefs = getSharedPreferences("bodycheck_prefs", MODE_PRIVATE)
                prefs.edit().putString("gender", prefsJson.optString("gender", "male")).apply()
                Log.d(TAG, "Restored preferences from backup")
            } catch (e: Exception) {
                Log.e(TAG, "Restore preferences failed", e)
            } finally {
                prefsFile.delete()
            }
        }
    }

    override fun onBackup(
        oldState: ParcelFileDescriptor?,
        data: BackupDataOutput,
        newState: ParcelFileDescriptor
    ) {
        // Not used by Seedvault
    }

    override fun onRestore(
        data: BackupDataInput,
        appVersionCode: Int,
        newState: ParcelFileDescriptor
    ) {
        // Not used by Seedvault
    }
}
