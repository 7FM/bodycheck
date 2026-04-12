package com.bodycheck

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bodycheck.data.ScanData
import com.bodycheck.data.ScanDatabase
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var db: ScanDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: ScanHistoryAdapter

    private val scans = mutableListOf<ScanData>()

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
        private const val REQUEST_IMAGE_PICK = 200
        private const val REQUEST_EXPORT = 300
        private const val REQUEST_IMPORT = 400
        private const val REQUEST_QR_SCAN = 500
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyThemeMode(this)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_main)

        db = ScanDatabase(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.app_name)
        setSupportActionBar(toolbar)
        ThemeHelper.applyGenderTheme(this)

        val btnScanQr = findViewById<Button>(R.id.btnScanQr)
        val btnImportImage = findViewById<Button>(R.id.btnImportImage)
        recyclerView = findViewById(R.id.rvHistory)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        adapter = ScanHistoryAdapter(
            scans = scans,
            onClick = { scan ->
                val intent = Intent(this, ScanResultActivity::class.java)
                intent.putExtra("scan_id", scan.id)
                startActivity(intent)
            },
            onLongClick = { scan ->
                showDeleteDialog(scan)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnScanQr.setOnClickListener { startQrScan() }
        btnImportImage.setOnClickListener { startImageImport() }
    }

    override fun onResume() {
        super.onResume()
        refreshHistory()
    }

    private fun refreshHistory() {
        scans.clear()
        scans.addAll(db.getAllScans())
        adapter.notifyDataSetChanged()

        if (scans.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun startQrScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
            return
        }
        launchScanner()
    }

    private fun launchScanner() {
        val intent = Intent(this, ScannerActivity::class.java)
        startActivityForResult(intent, REQUEST_QR_SCAN)
    }

    private fun startImageImport() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchScanner()
            } else {
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return

        when (requestCode) {
            REQUEST_QR_SCAN -> {
                val qrText = data?.getStringExtra(ScannerActivity.EXTRA_QR_TEXT) ?: return
                handleQrContent(qrText)
            }
            REQUEST_IMAGE_PICK -> {
                val uri = data?.data ?: return
                decodeQrFromImage(uri)
            }
            REQUEST_EXPORT -> {
                val uri = data?.data ?: return
                performExport(uri)
            }
            REQUEST_IMPORT -> {
                val uri = data?.data ?: return
                performImport(uri)
            }
        }
    }

    private fun decodeQrFromImage(uri: Uri) {
        QrDecoderImpl.decodeFromImage(this, uri) { qrText ->
            runOnUiThread {
                if (qrText != null) {
                    handleQrContent(qrText)
                } else {
                    Toast.makeText(this, R.string.scan_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleQrContent(content: String) {
        val scanData = ScanData.parse(content)
        if (scanData == null) {
            Toast.makeText(this, R.string.parse_failed, Toast.LENGTH_SHORT).show()
            return
        }

        val id = db.insertScan(scanData)
        val intent = Intent(this, ScanResultActivity::class.java)
        intent.putExtra("scan_id", id)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_trends -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            R.id.action_export -> {
                startExport()
                true
            }
            R.id.action_import -> {
                startImport()
                true
            }
            R.id.action_gender -> {
                showGenderDialog()
                true
            }
            R.id.action_theme -> {
                showThemeDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startExport() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "bodycheck_export.json")
        }
        startActivityForResult(intent, REQUEST_EXPORT)
    }

    private fun startImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, REQUEST_IMPORT)
    }

    private fun performExport(uri: Uri) {
        try {
            val json = db.exportAllAsJson()
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            val count = JSONArray(json).length()
            Toast.makeText(this, getString(R.string.export_success, count), Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun performImport(uri: Uri) {
        try {
            val json = contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return
            val count = db.importFromJson(json)
            Toast.makeText(this, getString(R.string.import_success, count), Toast.LENGTH_SHORT).show()
            refreshHistory()
        } catch (_: Exception) {
            Toast.makeText(this, R.string.import_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showGenderDialog() {
        val prefs = getSharedPreferences("bodycheck_prefs", MODE_PRIVATE)
        val current = prefs.getString("gender", "male")
        val options = arrayOf("Male", "Female")
        val selected = if (current == "female") 1 else 0
        AlertDialog.Builder(this)
            .setTitle("Gender")
            .setSingleChoiceItems(options, selected) { dialog, which ->
                prefs.edit().putString("gender", if (which == 1) "female" else "male").apply()
                Toast.makeText(this, "Gender set to ${options[which]}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showThemeDialog() {
        val prefs = getSharedPreferences("bodycheck_prefs", MODE_PRIVATE)
        val current = prefs.getString("theme_mode", "system")
        val options = arrayOf("System", "Light", "Dark")
        val selected = when (current) {
            "light" -> 1
            "dark" -> 2
            else -> 0
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.theme)
            .setSingleChoiceItems(options, selected) { dialog, which ->
                val mode = when (which) {
                    1 -> "light"
                    2 -> "dark"
                    else -> "system"
                }
                prefs.edit().putString("theme_mode", mode).apply()
                ThemeHelper.applyThemeMode(this)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showDeleteDialog(scan: ScanData) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_scan)
            .setMessage(R.string.delete_scan_confirm)
            .setPositiveButton(R.string.delete) { _, _ ->
                db.deleteScan(scan.id)
                refreshHistory()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /** RecyclerView adapter for the history list on the main screen. */
    private class ScanHistoryAdapter(
        private val scans: List<ScanData>,
        private val onClick: (ScanData) -> Unit,
        private val onLongClick: (ScanData) -> Unit
    ) : RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder>() {

        private val dateFormat = SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault())

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDate: TextView = view.findViewById(R.id.tvDate)
            val tvWeight: TextView = view.findViewById(R.id.tvWeight)
            val tvBmi: TextView = view.findViewById(R.id.tvBmi)
            val tvHealthScore: TextView = view.findViewById(R.id.tvHealthScore)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_scan_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val scan = scans[position]
            holder.tvDate.text = dateFormat.format(Date(scan.scanDate))
            holder.tvWeight.text = String.format("%.1f kg", scan.weight)
            holder.tvBmi.text = String.format("BMI %.1f", scan.bmi)
            holder.tvHealthScore.text = String.format("%.0f pts", scan.healthScore)
            holder.itemView.setOnClickListener { onClick(scan) }
            holder.itemView.setOnLongClickListener {
                onLongClick(scan)
                true
            }
        }

        override fun getItemCount(): Int = scans.size
    }
}
