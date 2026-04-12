package com.bodycheck

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer

object QrDecoderImpl : QrDecoder {

    private val hints = mapOf(
        DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.QR_CODE),
        DecodeHintType.TRY_HARDER to true
    )

    override fun decodeFromImage(context: Context, uri: Uri, onResult: (String?) -> Unit) {
        try {
            val bitmap = loadAndScale(context, uri)
            if (bitmap == null) { onResult(null); return }

            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)

            val reader = MultiFormatReader().apply { setHints(hints) }
            val result = tryDecode(reader, BinaryBitmap(HybridBinarizer(source)))
                ?: tryDecode(reader, BinaryBitmap(GlobalHistogramBinarizer(source)))
                ?: tryDecode(reader, BinaryBitmap(HybridBinarizer(source.invert())))

            onResult(result?.text)
        } catch (_: Exception) {
            onResult(null)
        }
    }

    override fun decodeFromCameraFrame(imageProxy: ImageProxy): String? {
        val reader = MultiFormatReader().apply { setHints(hints) }
        return try {
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val source = PlanarYUVLuminanceSource(
                bytes, imageProxy.width, imageProxy.height,
                0, 0, imageProxy.width, imageProxy.height, false
            )
            reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))?.text
        } catch (_: Exception) {
            null
        } finally {
            reader.reset()
        }
    }

    private fun tryDecode(reader: MultiFormatReader, bitmap: BinaryBitmap): com.google.zxing.Result? {
        return try { reader.decodeWithState(bitmap) } catch (_: Exception) { null } finally { reader.reset() }
    }

    private fun loadAndScale(context: Context, uri: Uri): android.graphics.Bitmap? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        if (opts.outWidth <= 0 || opts.outHeight <= 0) return null
        var sampleSize = 1
        while (opts.outWidth / sampleSize > 1500 || opts.outHeight / sampleSize > 1500) sampleSize *= 2
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, decodeOpts) }
    }
}
