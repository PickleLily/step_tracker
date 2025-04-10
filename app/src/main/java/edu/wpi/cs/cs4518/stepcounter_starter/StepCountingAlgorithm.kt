package edu.wpi.cs.cs4518.stepcounter_starter

import android.util.Log
import com.github.psambit9791.jdsp.signal.Smooth
import com.github.psambit9791.jdsp.signal.peaks.FindPeak

object StepCounterAlgorithm {
	private const val TAG = "StepCounterAlgorithm"
	private const val WINDOW_SIZE = 5          // Smoothing window size
	private const val HEIGHT_THRESHOLD = 2.0   // Minimum peak height in m/s²
	private const val MIN_PEAK_DISTANCE = 15   // Minimum samples between peaks
	private const val DEBOUNCE_TIME = 400      // Minimum time between steps (ms)
	private var lastStepTime = 0L              // Last step timestamp
	private var lastPeakIndex = -MIN_PEAK_DISTANCE - 1  // Last peak index for distance check

	fun detectSteps(sensorData: List<FloatArray>, currentTime: Long): Int {
		try {
			// Extract magnitudes and convert to DoubleArray
			val magnitudes = sensorData.map { it[0].toDouble() }.toDoubleArray()

			// Apply smoothing with a rectangular window
			val smooth = Smooth(magnitudes, WINDOW_SIZE, "rectangular")
			val smoothedData = smooth.smoothSignal()

			// Detect peaks
			val fp = FindPeak(smoothedData)
			val peaks = fp.detectPeaks()

			// Filter peaks based on height, distance, and debounce time
			var stepCount = 0
			for (peakIdx in peaks.peaks) {
				val peakHeight = smoothedData[peakIdx]
				val peakTime = currentTime - (sensorData.size - peakIdx) * 20  // Assuming 50Hz

				// Check height, distance from last peak, and debounce time
				if (peakHeight >= HEIGHT_THRESHOLD &&
					peakIdx - lastPeakIndex >= MIN_PEAK_DISTANCE &&
					peakTime - lastStepTime >= DEBOUNCE_TIME
				) {
					stepCount++
					lastStepTime = peakTime
					lastPeakIndex = peakIdx
				}
			}

			Log.d(TAG, "Steps detected: $stepCount")
			return stepCount
		} catch (e: Exception) {
			Log.e(TAG, "Error in step detection: ${e.message}")
			return 0
		}
	}
}