package com.bodycheck

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

object QrDecoderImpl : QrDecoder {

    override fun decodeFromImage(context: Context, uri: Uri, onResult: (String?) -> Unit) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            BarcodeScanning.getClient().process(image)
                .addOnSuccessListener { barcodes ->
                    onResult(barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }?.rawValue)
                }
                .addOnFailureListener { onResult(null) }
        } catch (_: Exception) {
            onResult(null)
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    override fun decodeFromCameraFrame(imageProxy: ImageProxy): String? {
        // ML Kit processes asynchronously, but for CameraX analyzer we need sync.
        // Use blocking approach with CountDownLatch.
        val mediaImage = imageProxy.image ?: return null
        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        var result: String? = null
        val latch = java.util.concurrent.CountDownLatch(1)
        BarcodeScanning.getClient().process(inputImage)
            .addOnSuccessListener { barcodes ->
                result = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }?.rawValue
            }
            .addOnCompleteListener { latch.countDown() }
        latch.await(500, java.util.concurrent.TimeUnit.MILLISECONDS)
        return result
    }
}
