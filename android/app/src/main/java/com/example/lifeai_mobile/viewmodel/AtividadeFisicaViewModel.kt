package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.Exercise
import com.example.lifeai_mobile.model.ExerciseSessionRequest
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

    // Estados para controlar a UI (Loading e Erros)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- NOVOS ESTADOS: RESUMO DO DIA ---
    private val _dailyCalories = MutableStateFlow(0)
    val dailyCalories: StateFlow<Int> = _dailyCalories.asStateFlow()

    private val _dailyTimeMinutes = MutableStateFlow(0)
    val dailyTimeMinutes: StateFlow<Int> = _dailyTimeMinutes.asStateFlow()

    init {
        // Assim que a tela abre, carrega o resumo
        fetchDailySummary()
    }

    /**
     * Busca o histórico no backend e calcula os totais de HOJE
     */
    private fun fetchDailySummary() {
        viewModelScope.launch {
            try {
                val response = repository.getExerciseHistory()
                if (response.isSuccessful && response.body() != null) {
                    val history = response.body()!!

                    // Filtra apenas os treinos de HOJE
                    val todaysWorkouts = history.filter { isDateToday(it.createdAt) }

                    // Soma os totais
                    val totalCals = todaysWorkouts.sumOf { it.caloriesBurned }
                    val totalSeconds = todaysWorkouts.sumOf { it.durationSeconds }

                    _dailyCalories.value = totalCals
                    _dailyTimeMinutes.value = (totalSeconds / 60).toInt()
                }
            } catch (e: Exception) {
                // Falha silenciosa no resumo para não atrapalhar o uso
                e.printStackTrace()
            }
        }
    }

    /**
     * Função auxiliar para verificar se a data (String UTC do Django) é "Hoje" no horário local do celular
     */
    private fun isDateToday(dateString: String): Boolean {
        return try {
            // Formato padrão do Django REST Framework: "2025-11-22T10:00:00Z"
            // Tenta parsear com milissegundos ou sem, por segurança usamos um padrão genérico
            // Mas vamos assumir o padrão ISO simples aqui.
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC") // O Backend manda em UTC

            // Corrige caso venha com 'Z' no final ou frações de segundos que o SimpleDateFormat odeia
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

    /**
     * Função chamada quando o usuário clica em "Salvar Treino".
     */
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
                // 1. Calcular Calorias
                val durationMinutes = durationSeconds / 60.0
                val caloriesBurned = (durationMinutes * exercise.caloriesBurnedPerMinute).toInt()
                val finalCalories = if (caloriesBurned == 0) 1 else caloriesBurned

                // 2. Gerar Timestamp (UTC)
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val currentTime = sdf.format(Date())

                // 3. Request
                val request = ExerciseSessionRequest(
                    exerciseName = exercise.name,
                    durationSeconds = durationSeconds,
                    caloriesBurned = finalCalories,
                    createdAt = currentTime
                )

                val response = repository.saveExerciseSession(request)

                if (response.isSuccessful) {
                    _saveSuccess.value = true
                    // ATUALIZA O RESUMO IMEDIATAMENTE
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