package edu.wpi.cs.cs4518.stepcounter_starter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

class HourlyStepChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val barPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }
    private val axisPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private var hourlySteps: FloatArray = FloatArray(24) { 0f }
    private var maxSteps: Float = 0f
    private var tickMax: Float = 0f // New variable to store the adjusted maximum for ticks

    fun setHourlySteps(steps: FloatArray) {
        hourlySteps = steps
        maxSteps = steps.maxOrNull() ?: 0f
        if (maxSteps == 0f) maxSteps = 1f // Avoid division by zero

        // Calculate the tick maximum: round up to nearest 1000, then add 1000
        tickMax = (((maxSteps.toInt() / 1000) + 1) * 1000).toFloat()

        // Debug logging
        Log.d("HourlyStepChart", "maxSteps: $maxSteps, tickMax: $tickMax")
        Log.d("HourlyStepChart", "hourlySteps: ${hourlySteps.joinToString()}")

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Dynamic padding based on the smaller dimension
        val padding = if (width < height) width * 0.1f else height * 0.1f

        // Adjust text size based on view size for better scaling
        textPaint.textSize = if (width < height) width * 0.04f else height * 0.04f

        val barWidth = (width - 2 * padding) / 24f

        // Draw X-axis (hours)
        canvas.drawLine(padding, height - padding, width - padding, height - padding, axisPaint)

        // Draw Y-axis (steps)
        canvas.drawLine(padding, padding, padding, height - padding, axisPaint)

        // Draw bars (scaled to tickMax instead of maxSteps)
        for (hour in 0..23) {
            val barHeight = (hourlySteps[hour] / tickMax) * (height - 2 * padding)
            val left = padding + hour * barWidth
            val top = height - padding - barHeight
            val right = left + barWidth * 0.8f
            val bottom = height - padding

            // Draw bar
            canvas.drawRect(left, top, right, bottom, barPaint)

            // Draw hour label
            if (hour % 3 == 0) { // Show labels every 3 hours for clarity
                canvas.drawText("$hour", left + barWidth / 2, height - padding / 2, textPaint)
            }
        }

        // Calculate number of ticks (in increments of 1000)
        val tickInterval = 200f
        val numTicks = (tickMax / tickInterval).toInt()

        // Draw Y-axis labels (steps)
        for (i in 0..numTicks) {
            val stepValue = i * tickInterval
            val y = height - padding - (stepValue / tickMax) * (height - 2 * padding)
            canvas.drawText(stepValue.toInt().toString(), padding / 2, y, textPaint)
        }
    }
}