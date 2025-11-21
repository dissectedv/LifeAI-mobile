package com.example.lifeai_mobile.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.RegistroImcRequest
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    object NavigateBack : UiEvent()
}

class ImcCalculatorViewModel(private val repository: AuthRepository) : ViewModel() {

    var idade by mutableStateOf("")
    var sexo by mutableStateOf("")
    var peso by mutableStateOf("")
    var altura by mutableStateOf("")

    private fun getTodayAtUtcStart(): Date {
        val instant = LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant()
        return Date.from(instant)
    }

    var dataConsulta by mutableStateOf(getTodayAtUtcStart())
    var isLoading by mutableStateOf(false)
    var isHeightFieldLocked by mutableStateOf(true)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profileResponse = repository.getProfileData()
                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    val profile = profileResponse.body()!!
                    idade = profile.idade.toString()
                    sexo = profile.sexo
                }

                val historyResponse = repository.getHistoricoImc()
                if (historyResponse.isSuccessful && !historyResponse.body().isNullOrEmpty()) {
                    val lista = historyResponse.body()!!

                    // Pega o registro com maior ID (o mais recente)
                    val ultimoRegistro = lista.maxByOrNull { it.id }

                    if (ultimoRegistro != null && ultimoRegistro.altura > 0) {
                        var alturaMetros = ultimoRegistro.altura
                        if (alturaMetros > 3) {
                            alturaMetros /= 100f
                        }
                        val alturaFormatada = String.format(Locale.US, "%.2f", alturaMetros)
                        onAlturaChange(alturaFormatada)
                    }
                }
            } catch (e: Exception) {
                Log.e("IMC_CALC_VM", "Erro ao carregar dados iniciais", e)
            }
        }
    }

    fun onPesoChange(newValue: String) { peso = newValue }
    fun onAlturaChange(newValue: String) { altura = newValue }
    fun onDataChange(newDate: Date) { dataConsulta = newDate }
    fun onUnlockHeightField() { isHeightFieldLocked = false }

    private fun resetFormState() {
        peso = ""
        isHeightFieldLocked = true
        dataConsulta = getTodayAtUtcStart()
    }

    fun calculateAndRegister() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true

            val pesoFloat = peso.replace(',', '.').toFloatOrNull()
            var alturaFloat = altura.replace(',', '.').toFloatOrNull()

            if (pesoFloat == null || pesoFloat <= 0 || pesoFloat > 400) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Peso inválido."))
                isLoading = false
                return@launch
            }
            if (alturaFloat == null || alturaFloat <= 0) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Altura inválida."))
                isLoading = false
                return@launch
            }

            // Normaliza para Metros para o cálculo
            if (alturaFloat > 3) alturaFloat /= 100f

            if (alturaFloat < 0.5f || alturaFloat > 2.5f) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Altura fora do intervalo comum."))
                isLoading = false
                return@launch
            }

            val imcValor = pesoFloat / (alturaFloat * alturaFloat)

            val classificacao = when {
                imcValor < 18.5f -> "Abaixo do peso"
                imcValor < 25f -> "Peso normal"
                imcValor < 30f -> "Sobrepeso"
                else -> "Obesidade"
            }

            // O Backend agora espera METROS e faz o cálculo certo lá também.
            val request = RegistroImcRequest(
                peso = pesoFloat.toDouble(),
                altura = alturaFloat.toDouble(),
                imc = imcValor.toDouble(),
                classificacao = classificacao
            )

            try {
                val response = repository.createImcRecord(request)
                if (response.isSuccessful) {
                    resetFormState()
                    _eventFlow.emit(UiEvent.ShowSnackbar("IMC registrado com sucesso!"))
                    _eventFlow.emit(UiEvent.NavigateBack)
                } else {
                    _eventFlow.emit(UiEvent.ShowSnackbar("Erro ao registrar IMC."))
                }
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Falha na conexão."))
            } finally {
                isLoading = false
            }
        }
    }
}