package com.bodycheck

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import java.util.concurrent.atomic.AtomicBoolean

class ScannerActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private val found = AtomicBoolean(false)

    companion object {
        const val EXTRA_QR_TEXT = "qr_text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyThemeMode(this)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContentView(R.layout.activity_scanner)
        ThemeHelper.applyGenderTheme(this)
        previewView = findViewById(R.id.previewView)
        startCamera()
    }

    private fun startCamera() {
        val controller = LifecycleCameraController(this)
        controller.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            processImage(imageProxy, controller)
        }
        controller.bindToLifecycle(this)
        previewView.controller = controller
    }

    private fun processImage(imageProxy: ImageProxy, controller: LifecycleCameraController) {
        if (found.get()) {
            imageProxy.close()
            return
        }

        try {
            val result = QrDecoderImpl.decodeFromCameraFrame(imageProxy)
            if (result != null && found.compareAndSet(false, true)) {
                controller.unbind()
                setResult(RESULT_OK, Intent().apply { putExtra(EXTRA_QR_TEXT, result) })
                finish()
            }
        } finally {
            imageProxy.close()
        }
    }
}
