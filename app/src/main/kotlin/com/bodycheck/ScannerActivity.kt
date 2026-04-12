package com.bodycheck

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
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
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                processImage(imageProxy, cameraProvider)
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImage(imageProxy: ImageProxy, cameraProvider: ProcessCameraProvider) {
        if (found.get()) {
            imageProxy.close()
            return
        }

        try {
            val result = QrDecoderImpl.decodeFromCameraFrame(imageProxy)
            if (result != null && found.compareAndSet(false, true)) {
                cameraProvider.unbindAll()
                setResult(RESULT_OK, Intent().apply { putExtra(EXTRA_QR_TEXT, result) })
                finish()
            }
        } finally {
            imageProxy.close()
        }
    }
}
