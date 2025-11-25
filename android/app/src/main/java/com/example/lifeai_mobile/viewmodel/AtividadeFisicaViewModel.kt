package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.Exercise
import com.example.lifeai_mobile.model.ExerciseSessionRequest
import com.example.lifeai_mobile.model.ExerciseSessionResponse
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AtividadeFisicaViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _dailyCalories = MutableStateFlow(0)
    val dailyCalories: StateFlow<Int> = _dailyCalories.asStateFlow()

    private val _dailyTimeMinutes = MutableStateFlow(0)
    val dailyTimeMinutes: StateFlow<Int> = _dailyTimeMinutes.asStateFlow()

    private val _todaysExercises = MutableStateFlow<List<ExerciseSessionResponse>>(emptyList())
    val todaysExercises: StateFlow<List<ExerciseSessionResponse>> = _todaysExercises.asStateFlow()

    val dailyCalorieGoal = 500
    val dailyTimeGoal = 60

    init {
        fetchDailySummary()
    }

    private fun fetchDailySummary() {
        viewModelScope.launch {
            try {
                val response = repository.getExerciseHistory()
                if (response.isSuccessful && response.body() != null) {
                    val history = response.body()!!

                    val todaysWorkouts = history.filter { isDateToday(it.createdAt) }

                    _todaysExercises.value = todaysWorkouts.sortedByDescending { it.createdAt }

                    val totalCals = todaysWorkouts.sumOf { it.caloriesBurned }
                    val totalSeconds = todaysWorkouts.sumOf { it.durationSeconds }

                    _dailyCalories.value = totalCals
                    _dailyTimeMinutes.value = (totalSeconds / 60).toInt()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isDateToday(dateString: String): Boolean {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")

            val cleanDateString = dateString.substringBefore(".").replace("Z", "")
            val date = format.parse(cleanDateString) ?: return false

            val currentCalendar = Calendar.getInstance()
            val targetCalendar = Calendar.getInstance()
            targetCalendar.time = date

            (currentCalendar.get(Calendar.YEAR) == targetCalendar.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.DAY_OF_YEAR) == targetCalendar.get(Calendar.DAY_OF_YEAR))
        } catch (e: Exception) {
            false
        }
    }

    fun finalizarExercicio(exercise: Exercise, durationSeconds: Long) {
        if (durationSeconds < 10) {
            _errorMessage.value = "Treino muito curto para ser salvo (min. 10s)."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _saveSuccess.value = false

            try {
                val durationMinutes = durationSeconds / 60.0
                val caloriesBurned = (durationMinutes * exercise.caloriesBurnedPerMinute).toInt()
                val finalCalories = if (caloriesBurned == 0) 1 else caloriesBurned

                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val currentTime = sdf.format(Date())

                val request = ExerciseSessionRequest(
                    exerciseName = exercise.name,
                    durationSeconds = durationSeconds,
                    caloriesBurned = finalCalories,
                    createdAt = currentTime
                )

                val response = repository.saveExerciseSession(request)

                if (response.isSuccessful) {
                    _saveSuccess.value = true
                    fetchDailySummary()
                } else {
                    _errorMessage.value = "Erro ao salvar: ${response.code()}"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Erro de conexão: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _saveSuccess.value = false
        _errorMessage.value = null
    }
}