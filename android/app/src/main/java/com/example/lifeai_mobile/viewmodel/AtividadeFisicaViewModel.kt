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
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AtividadeFisicaViewModel(private val repository: AuthRepository) : ViewModel() {

    // Estados para controlar a UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Função chamada quando o usuário clica em "Salvar Treino".
     * Calcula calorias e envia para a API.
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
                // 1. Calcular Calorias Queimadas
                // Fórmula: (Segundos / 60) * CaloriasPorMinuto
                val durationMinutes = durationSeconds / 60.0
                val caloriesBurned = (durationMinutes * exercise.caloriesBurnedPerMinute).toInt()

                // Garante pelo menos 1 caloria se o treino for muito curto mas válido
                val finalCalories = if (caloriesBurned == 0) 1 else caloriesBurned

                // 2. Gerar Timestamp atual (ISO 8601)
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val currentTime = sdf.format(Date())

                // 3. Montar o objeto de requisição
                val request = ExerciseSessionRequest(
                    exerciseName = exercise.name,
                    durationSeconds = durationSeconds,
                    caloriesBurned = finalCalories,
                    createdAt = currentTime
                )

                // 4. Chamar o repositório
                val response = repository.saveExerciseSession(request)

                if (response.isSuccessful) {
                    _saveSuccess.value = true
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

    // Reseta o estado de sucesso para não navegar/exibir mensagem repetidamente
    fun resetState() {
        _saveSuccess.value = false
        _errorMessage.value = null
    }
}