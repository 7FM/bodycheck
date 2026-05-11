package com.bodycheck

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.bodycheck.data.ScanData
import com.bodycheck.data.ScanDatabase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.android.controller.ActivityController
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class ScreenshotTest {

    private val outDir = File(
        System.getProperty("user.dir"),
        "../fastlane/metadata/android/en-US/images/phoneScreenshots"
    ).also { it.mkdirs() }

    private fun seedDb(): Long {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        ctx.deleteDatabase("bodycheck.db")
        val db = ScanDatabase(ctx)
        val now = System.currentTimeMillis()
        val DAY = 86_400_000L
        val scans = listOf(
            Triple(now - 150 * DAY, 44.2f, 76.0f),
            Triple(now - 120 * DAY, 43.6f, 78.0f),
            Triple(now - 90 * DAY,  43.0f, 81.0f),
            Triple(now - 60 * DAY,  42.6f, 82.0f),
            Triple(now - 30 * DAY,  42.2f, 83.0f),
            Triple(now,             42.0f, 84.0f),
        )
        var lastId = -1L
        for ((ts, weight, score) in scans) {
            lastId = db.insertScan(ScanData(rawValues = fakeRaw(weight, score), scanDate = ts))
        }
        return lastId
    }

    private fun fakeRaw(weight: Float, score: Float): FloatArray {
        val v = FloatArray(64) { 42f }
        v[0]  = weight; v[1] = 42f; v[2] = 4.2f; v[3] = 24.2f
        v[4]  = 24f;    v[5] = 24.2f; v[6] = score
        floatArrayOf(4.2f, 4.2f, 24.2f, 14.2f, 14.2f,
                     0.42f, 0.42f, 2.4f, 1.42f, 1.42f).copyInto(v, 7)
        v[17] = 1f; v[18] = 2f   // body-type matrix → "Standard" cell
        floatArrayOf(4.2f, 4.2f, 24.2f, 14.2f, 14.2f).copyInto(v, 19)
        floatArrayOf(0.42f, 0.42f, 2.4f, 1.42f, 1.42f).copyInto(v, 24)
        v[29] = 42f; v[30] = 4f; v[31] = 2.4f
        v[32] = 14.2f; v[33] = 10f
        v[34] = 6.42f; v[35] = 1424f
        v[36] = 21f       // BMI
        v[37] = 42f
        v[38] = 100f; v[39] = 100f; v[40] = 100f
        v[41] = 0f; v[42] = 0f; v[43] = 0f
        v[44] = 84f; v[45] = 0.84f
        v[46] = 142f      // height
        v[47] = 2f; v[48] = 8f
        v[49] = 1.8f; v[50] = 3f
        v[51] = 5f; v[52] = 9f
        v[53] = 8f; v[54] = 14f
        v[55] = 14f; v[56] = 24f
        v[57] = 42f; v[58] = 58f
        v[59] = 2.4f; v[60] = 20f
        v[61] = 24f; v[62] = 2f   // visceral fat level (1-5, 2 = Good)
        v[63] = 42f
        return v
    }

    private fun <T : Activity> snap(clazz: Class<T>, file: File, intent: Intent? = null, scrollY: Int = 0) {
        val controller: ActivityController<T> = if (intent != null) {
            Robolectric.buildActivity(clazz, intent)
        } else {
            Robolectric.buildActivity(clazz)
        }
        controller.setup().visible()
        shadowOf(Looper.getMainLooper()).idle()
        val activity = controller.get()
        val root = activity.window.decorView
        val w = 1080
        val h = 2340
        root.measure(
            View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)
        )
        root.layout(0, 0, w, h)
        shadowOf(Looper.getMainLooper()).idle()
        if (scrollY > 0) {
            findScrollView(root)?.let {
                it.scrollTo(0, scrollY)
                shadowOf(Looper.getMainLooper()).idle()
            }
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        root.draw(Canvas(bmp))
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        debugTree(root, 0)
        println("Captured ${file.name}: ${root.width}x${root.height}")
        controller.pause().stop().destroy()
    }

    private fun findScrollView(v: View): android.widget.ScrollView? {
        if (v is android.widget.ScrollView) return v
        if (v is android.view.ViewGroup) {
            for (i in 0 until v.childCount) findScrollView(v.getChildAt(i))?.let { return it }
        }
        return null
    }

    private fun debugTree(v: View, depth: Int) {
        val pad = "  ".repeat(depth)
        println("$pad${v::class.java.simpleName} id=${try { v.context.resources.getResourceEntryName(v.id) } catch (_: Throwable) { v.id }} ${v.left},${v.top}-${v.right},${v.bottom} (${v.width}x${v.height}) vis=${v.visibility}")
        if (v is android.view.ViewGroup) for (i in 0 until v.childCount) debugTree(v.getChildAt(i), depth + 1)
    }

    @Test
    fun capture_main_history() {
        seedDb()
        snap(MainActivity::class.java, File(outDir, "01_main_history.png"))
    }

    @Test
    fun capture_scan_result() {
        val id = seedDb()
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ScanResultActivity::class.java
        ).putExtra("scan_id", id)
        snap(ScanResultActivity::class.java, File(outDir, "02_scan_result.png"), intent)
    }

    @Test
    fun capture_scan_result_body() {
        val id = seedDb()
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ScanResultActivity::class.java
        ).putExtra("scan_id", id)
        snap(ScanResultActivity::class.java, File(outDir, "03_scan_result_body.png"), intent,
            scrollY = 1700)
    }

    @Test
    fun capture_history_trends() {
        seedDb()
        snap(HistoryActivity::class.java, File(outDir, "04_history_trends.png"))
    }
}
