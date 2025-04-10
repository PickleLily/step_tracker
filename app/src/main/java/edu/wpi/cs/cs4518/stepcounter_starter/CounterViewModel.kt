package edu.wpi.cs.cs4518.stepcounter_starter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.LinkedList

class CounterViewModel : ViewModel() {
	private val _stepCount = MutableLiveData<Int>(0)
	val stepCount: LiveData<Int> = _stepCount

	private val sensorDataBuffer = LinkedList<FloatArray>()
	private val BUFFER_SIZE = 100  // Process data in chunks of 100 samples

	fun addSensorData(x: Float, y: Float, z: Float) {
		// Calculate magnitude and add to buffer
		val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
		sensorDataBuffer.add(floatArrayOf(magnitude))

		if (sensorDataBuffer.size >= BUFFER_SIZE) {
			val steps = StepCounterAlgorithm.detectSteps(sensorDataBuffer.toList(), System.currentTimeMillis())
			_stepCount.value = (_stepCount.value ?: 0) + steps
			sensorDataBuffer.clear()  // Clear buffer after processing
		}
	}

	fun resetSteps() {
		_stepCount.value = 0
		sensorDataBuffer.clear()
	}
}