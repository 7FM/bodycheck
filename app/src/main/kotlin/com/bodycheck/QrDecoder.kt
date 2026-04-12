package com.bodycheck

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageProxy

interface QrDecoder {
    fun decodeFromImage(context: Context, uri: Uri, onResult: (String?) -> Unit)
    fun decodeFromCameraFrame(imageProxy: ImageProxy): String?
}
