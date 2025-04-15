package edu.wpi.cs.cs4518.stepcounter_starter

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import edu.wpi.cs.cs4518.stepcounter_starter.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {
	private lateinit var binding: ActivityMainBinding
	private val counterViewModel: CounterViewModel by viewModels()
	private lateinit var stepViewModel: StepViewModel

	private lateinit var sensorManager: SensorManager
	private var linearAccelerometer: Sensor? = null
	private var isSensorActive = false
	private lateinit var dbHelper: StepDatabaseHelper
	private lateinit var sharedPreferences: SharedPreferences
	private var previousStepCount: Int = 0 // Track the previous step count to calculate incremental steps

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// Initialize database helper
		dbHelper = StepDatabaseHelper(this)

		// Initialize SharedPreferences
		sharedPreferences = getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE)

		// Initialize ViewModels
		stepViewModel = ViewModelProvider(this).get(StepViewModel::class.java)

		// Initialize sensor
		sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
		linearAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

		// Observe step count from CounterViewModel and save incremental steps to database
		counterViewModel.stepCount.observe(this, Observer { count ->
			// Update live step counter
			binding.liveStepCounter.text = "Live Steps: $count"

			// Calculate incremental steps
			val incrementalSteps = if (previousStepCount == 0) count else count - previousStepCount
			previousStepCount = count

			// Insert incremental steps into the database
			lifecycleScope.launch {
				if (incrementalSteps > 0) { // Only insert if there are new steps
					dbHelper.insertStep(System.currentTimeMillis(), incrementalSteps)
				}
				updateUIForDate(stepViewModel.currentDate.value ?: System.currentTimeMillis())
			}
		})

		// Observe date changes and update UI
		stepViewModel.currentDate.observe(this, Observer { date ->
			val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
			binding.currentDate.text = dateFormat.format(date)
			lifecycleScope.launch {
				updateUIForDate(date)
			}
		})

		// Start button
		binding.buttonStart.setOnClickListener {
			if (!isSensorActive) {
				Log.d(TAG, "Start button clicked")
				linearAccelerometer?.let {
					sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
					isSensorActive = true
					Log.d(TAG, "Sensor registered")
				} ?: Log.e(TAG, "Linear Accelerometer not available")
			}
		}

		// Stop button
		binding.buttonStop.setOnClickListener {
			if (isSensorActive) {
				Log.d(TAG, "Stop button clicked")
				sensorManager.unregisterListener(this)
				isSensorActive = false
				Log.d(TAG, "Sensor unregistered")
			}
		}

		// Reset button
		binding.buttonReset.setOnClickListener {
			Log.d(TAG, "Reset button clicked")
			counterViewModel.resetSteps()
			previousStepCount = 0 // Reset the previous step count
			binding.liveStepCounter.text = "Live Steps: 0" // Reset the live counter display
		}

		// Swipe gesture detector
		val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
			override fun onFling(
				e1: MotionEvent?,
				e2: MotionEvent,
				velocityX: Float,
				velocityY: Float
			): Boolean {
				if (e1 != null && e2 != null) {
					val dayMillis = 24 * 60 * 60 * 1000L
					val currentDate = stepViewModel.currentDate.value ?: System.currentTimeMillis()
					val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
					val today = Calendar.getInstance().apply {
						set(Calendar.HOUR_OF_DAY, 0)
						set(Calendar.MINUTE, 0)
						set(Calendar.SECOND, 0)
						set(Calendar.MILLISECOND, 0)
					}.timeInMillis

					when {
						e1.x - e2.x > 100 -> { // Swipe left (previous day)
							stepViewModel.setDate(currentDate - dayMillis)
							return true
						}
						e2.x - e1.x > 100 && currentDate < today -> { // Swipe right (next day, but not past today)
							stepViewModel.setDate(currentDate + dayMillis)
							return true
						}
					}
				}
				return false
			}
		})

		binding.root.setOnTouchListener { _, event ->
			gestureDetector.onTouchEvent(event)
			true
		}

		// Insert dummy data only if it hasn't been inserted before
		lifecycleScope.launch {
			val isDummyDataInserted = sharedPreferences.getBoolean("isDummyDataInserted", false)
			if (!isDummyDataInserted) {
				// Clear existing data before inserting new dummy data
				dbHelper.clearAllData()
				insertDummyData()
				// Mark dummy data as inserted
				sharedPreferences.edit().putBoolean("isDummyDataInserted", true).apply()
			}
			updateUIForDate(stepViewModel.currentDate.value ?: System.currentTimeMillis())
		}
	}

	private suspend fun updateUIForDate(date: Long) {
		val startOfDay = Calendar.getInstance().apply {
			timeInMillis = date
			set(Calendar.HOUR_OF_DAY, 0)
			set(Calendar.MINUTE, 0)
			set(Calendar.SECOND, 0)
			set(Calendar.MILLISECOND, 0)
		}.timeInMillis
		val endOfDay = startOfDay + 24 * 60 * 60 * 1000

		// Update total steps
		val totalSteps = dbHelper.getTotalStepsForDay(startOfDay, endOfDay)
		binding.totalSteps.text = "Total Steps: $totalSteps"

		// Update chart
		val steps = dbHelper.getStepsForDay(startOfDay, endOfDay)
		val hourlySteps = FloatArray(24) { 0f }
		steps.forEach { step ->
			val hour = Calendar.getInstance().apply { timeInMillis = step.timestamp }.get(Calendar.HOUR_OF_DAY)
			hourlySteps[hour] += step.steps.toFloat()
		}

		// Debug: Log the hourly steps to verify aggregation
		hourlySteps.forEachIndexed { index, value ->
			Log.d(TAG, "Hour $index: $value steps")
		}

		// Set hourly steps on the custom chart
		binding.barChart.setHourlySteps(hourlySteps)
	}

	private suspend fun insertDummyData() {
		val calendar = Calendar.getInstance()
		for (dayOffset in -5..0) { // 5 days before today, up to today
			calendar.timeInMillis = System.currentTimeMillis() + dayOffset * 24 * 60 * 60 * 1000L
			calendar.set(Calendar.HOUR_OF_DAY, 0)
			calendar.set(Calendar.MINUTE, 0)
			calendar.set(Calendar.SECOND, 0)
			calendar.set(Calendar.MILLISECOND, 0)
			val baseTime = calendar.timeInMillis

			var dailyTotal = 0
			for (hour in 0..23) {
				calendar.timeInMillis = baseTime
				calendar.set(Calendar.HOUR_OF_DAY, hour)
				// Reasonable step counts: more steps during active hours (8 AM - 8 PM), fewer at night
				val steps = if (hour in 8..19) {
					(300..1000).random() // Active hours: 300–1000 steps per hour
				} else {
					(0..100).random() // Inactive hours: 0–100 steps per hour
				}
				dbHelper.insertStep(calendar.timeInMillis, steps)
				dailyTotal += steps
			}
			Log.d(TAG, "Dummy data for day offset $dayOffset: $dailyTotal steps")
		}
	}

	override fun onSensorChanged(event: SensorEvent?) {
		event?.let {
			counterViewModel.addSensorData(it.values[0], it.values[1], it.values[2])
		}
	}

	override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
		// Not needed
	}

	override fun onDestroy() {
		super.onDestroy()
		if (isSensorActive) {
			sensorManager.unregisterListener(this)
		}
	}

	companion object {
		private const val TAG = "MainActivity"
	}
}