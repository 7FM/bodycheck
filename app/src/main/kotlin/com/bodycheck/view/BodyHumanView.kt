package com.bodycheck.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.bodycheck.R

class BodyHumanView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dp = resources.displayMetrics.density
    private val sp = resources.displayMetrics.scaledDensity

    private var leftArm: Float = 0f
    private var rightArm: Float = 0f
    private var trunk: Float = 0f
    private var leftLeg: Float = 0f
    private var rightLeg: Float = 0f
    private var unit: String = "kg"

    private val bodyDrawable: Drawable? =
        AppCompatResources.getDrawable(context, R.drawable.ic_human_body_front2)

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 12f * sp
        color = context.getColor(R.color.text_primary)
        isFakeBoldText = true
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 10f * sp
        color = context.getColor(R.color.text_secondary)
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * dp
        color = Color.parseColor("#90A4AE")
        pathEffect = DashPathEffect(floatArrayOf(4f * dp, 3f * dp), 0f)
    }

    fun setSegmentData(
        leftArm: Float, rightArm: Float, trunk: Float,
        leftLeg: Float, rightLeg: Float, unit: String = "kg"
    ) {
        this.leftArm = leftArm
        this.rightArm = rightArm
        this.trunk = trunk
        this.leftLeg = leftLeg
        this.rightLeg = rightLeg
        this.unit = unit
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredW = (300 * dp).toInt()
        val desiredH = (300 * dp).toInt()
        val w = resolveSize(desiredW, widthMeasureSpec)
        val h = resolveSize(desiredH, heightMeasureSpec)
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // --- Body drawable centred, 35% of view width, aspect ratio 230:512 ---
        val bodyW = w * 0.35f
        val aspect = 512f / 230f
        val bodyH = bodyW * aspect
        val bodyLeft = (w - bodyW) / 2f
        val bodyTop = (h - bodyH) / 2f
        val bodyRight = bodyLeft + bodyW
        val bodyBottom = bodyTop + bodyH

        bodyDrawable?.let { d ->
            d.setBounds(bodyLeft.toInt(), bodyTop.toInt(), bodyRight.toInt(), bodyBottom.toInt())
            d.draw(canvas)
        }

        // --- Self-perspective: YOUR left arm on LEFT side of screen ---

        // LEFT column (right-aligned text at ~18% from left)
        val leftColX = w * 0.18f
        valuePaint.textAlign = Paint.Align.RIGHT
        labelPaint.textAlign = Paint.Align.RIGHT

        // L. Arm at 30% height
        val lArmY = h * 0.30f
        drawSideLabel(canvas, leftArm, "L. Arm", leftColX, lArmY, bodyLeft, lArmY, isLeft = true)

        // L. Leg at 75% height
        val lLegY = h * 0.75f
        drawSideLabel(canvas, leftLeg, "L. Leg", leftColX, lLegY, bodyLeft, lLegY, isLeft = true)

        // RIGHT column (left-aligned text at ~82% from left)
        val rightColX = w * 0.82f
        valuePaint.textAlign = Paint.Align.LEFT
        labelPaint.textAlign = Paint.Align.LEFT

        // R. Arm at 30% height
        val rArmY = h * 0.30f
        drawSideLabel(canvas, rightArm, "R. Arm", rightColX, rArmY, bodyRight, rArmY, isLeft = false)

        // R. Leg at 75% height
        val rLegY = h * 0.75f
        drawSideLabel(canvas, rightLeg, "R. Leg", rightColX, rLegY, bodyRight, rLegY, isLeft = false)

        // Trunk label centred on the body torso (white text for contrast on dark gray body)
        valuePaint.textAlign = Paint.Align.CENTER
        labelPaint.textAlign = Paint.Align.CENTER
        val savedValueColor = valuePaint.color
        val savedLabelColor = labelPaint.color
        valuePaint.color = Color.WHITE
        labelPaint.color = Color.parseColor("#E0E0E0")
        val trunkX = w * 0.5f
        val trunkY = bodyTop + bodyH * 0.33f
        val trunkValueStr = String.format("%.1f %s", trunk, unit)
        canvas.drawText(trunkValueStr, trunkX, trunkY, valuePaint)
        canvas.drawText("Trunk", trunkX, trunkY + labelPaint.textSize + 2f * dp, labelPaint)
        valuePaint.color = savedValueColor
        labelPaint.color = savedLabelColor
    }

    private fun drawSideLabel(
        canvas: Canvas, value: Float, label: String,
        textX: Float, textY: Float,
        bodyEdgeX: Float, lineY: Float,
        isLeft: Boolean
    ) {
        val valueStr = String.format("%.1f %s", value, unit)
        canvas.drawText(valueStr, textX, textY, valuePaint)
        canvas.drawText(label, textX, textY + labelPaint.textSize + 2f * dp, labelPaint)

        // Horizontal dashed leader line from label toward the nearest body edge
        val lineStartX = if (isLeft) {
            textX + 4f * dp
        } else {
            textX - 4f * dp
        }
        canvas.drawLine(lineStartX, textY + 2f * dp, bodyEdgeX, textY + 2f * dp, linePaint)
    }
}
