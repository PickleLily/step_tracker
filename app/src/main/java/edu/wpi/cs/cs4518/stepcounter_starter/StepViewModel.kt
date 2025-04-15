package edu.wpi.cs.cs4518.stepcounter_starter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Calendar

class StepViewModel : ViewModel() {
    private val _currentDate = MutableLiveData<Long>().apply {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        value = calendar.timeInMillis
    }
    val currentDate: LiveData<Long> = _currentDate

    private val _stepData = MutableLiveData<List<StepData>>()
    val stepData: LiveData<List<StepData>> = _stepData

    private val _totalSteps = MutableLiveData<Int>()
    val totalSteps: LiveData<Int> = _totalSteps

    fun setDate(date: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        _currentDate.value = calendar.timeInMillis
    }

    fun updateStepData(stepData: List<StepData>) {
        _stepData.value = stepData
    }

    fun updateTotalSteps(steps: Int) {
        _totalSteps.value = steps
    }
}