package com.example.lifeai_mobile.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.ImcRecordRequest
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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
    var dataConsulta by mutableStateOf(Date())

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
                val response = repository.getImcBaseDashboard()
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val profile = response.body()!!.first()
                    idade = profile.idade.toString()
                    sexo = profile.sexo

                    if (profile.altura > 0) {
                        var alturaMetros = profile.altura
                        if (alturaMetros > 3) {
                            alturaMetros /= 100f
                        }
                        val alturaFormatada = String.format(Locale.US, "%.2f", alturaMetros)
                        onAlturaChange(alturaFormatada)
                    }
                }
            } catch (e: Exception) {
                Log.e("IMC_CALC_VM", "Falha ao carregar dados iniciais", e)
            }
        }
    }

    fun onPesoChange(newValue: String) { peso = newValue }
    fun onAlturaChange(newValue: String) { altura = newValue }

    fun onDataChange(newDate: Date) {
        val adjustedDate = Date(newDate.time + TimeZone.getDefault().getOffset(newDate.time))
        dataConsulta = adjustedDate
    }

    fun onUnlockHeightField() {
        isHeightFieldLocked = false
    }

    private fun resetFormState() {
        peso = ""
        isHeightFieldLocked = true
        dataConsulta = Date()
    }

    fun calculateAndRegister() {
        Log.d("IMC_CALC_DEBUG", "Iniciando cálculo. Peso: '$peso', Altura: '$altura', Data: '$dataConsulta'")

        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true

            val pesoFloat = peso.replace(',', '.').toFloatOrNull()
            var alturaFloat = altura.replace(',', '.').toFloatOrNull()

            if (pesoFloat == null || pesoFloat <= 0 || pesoFloat > 400) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Peso inválido. Ex: 70.5"))
                isLoading = false
                return@launch
            }
            if (alturaFloat == null || alturaFloat <= 0) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Altura inválida. Ex: 1.75"))
                isLoading = false
                return@launch
            }
            if (alturaFloat > 3) alturaFloat /= 100f
            if (alturaFloat < 0.5f || alturaFloat > 2.5f) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Altura fora do intervalo (0.5m a 2.5m)"))
                isLoading = false
                return@launch
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataFormatada = dateFormat.format(dataConsulta)

            val request = ImcRecordRequest(
                idade = idade.toInt(),
                sexo = sexo,
                peso = pesoFloat,
                altura = alturaFloat,
                dataConsulta = dataFormatada
            )

            Log.d("IMC_CALC_DEBUG", "Validação OK. Enviando para a API: $request")

            try {
                val response = repository.createImcRecord(request)
                if (response.isSuccessful) {
                    Log.d("IMC_CALC_DEBUG", "Sucesso! Resposta: ${response.body()}")
                    resetFormState()
                    _eventFlow.emit(UiEvent.ShowSnackbar("IMC registrado com sucesso!"))
                    _eventFlow.emit(UiEvent.NavigateBack)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Corpo do erro vazio"
                    Log.e("IMC_CALC_DEBUG", "API retornou erro. Código: ${response.code()}. Corpo: $errorBody")
                    _eventFlow.emit(UiEvent.ShowSnackbar("Erro ao registrar IMC."))
                }
            } catch (e: Exception) {
                Log.e("IMC_CALC_DEBUG", "Exceção ao chamar a API", e)
                _eventFlow.emit(UiEvent.ShowSnackbar("Falha na conexão."))
            } finally {
                isLoading = false
            }
        }
    }
}