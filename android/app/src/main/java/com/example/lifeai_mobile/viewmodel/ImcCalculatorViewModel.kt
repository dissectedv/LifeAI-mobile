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
import kotlinx.coroutines.withContext
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
    var dataConsulta by mutableStateOf(getTodayAtUtcStart())
    var isLoading by mutableStateOf(false)
    var isHeightFieldLocked by mutableStateOf(true)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadInitialData()
    }

    private fun getTodayAtUtcStart(): Date {
        val instant = LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant()
        return Date.from(instant)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // UI updates need Main dispatcher
            withContext(Dispatchers.IO) {
                try {
                    val profileResponse = repository.getProfileData()
                    val historyResponse = repository.getHistoricoImc()

                    withContext(Dispatchers.Main) {
                        profileResponse.body()?.let { profile ->
                            idade = profile.idade.toString()
                            sexo = profile.sexo
                        }

                        historyResponse.body()?.let { lista ->
                            val ultimo = lista.maxByOrNull { it.id }
                            if (ultimo != null && ultimo.altura > 0) {
                                var alt = ultimo.altura
                                if (alt > 3) alt /= 100f
                                altura = String.format(Locale.US, "%.2f", alt)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("IMC_CALC_VM", "Erro no load inicial", e)
                }
            }
        }
    }

    fun onPesoChange(v: String) { peso = v }
    fun onAlturaChange(v: String) { altura = v }
    fun onDataChange(d: Date) { dataConsulta = d }
    fun onUnlockHeightField() { isHeightFieldLocked = false }

    private fun resetFormState() {
        peso = ""
        isHeightFieldLocked = true
        dataConsulta = getTodayAtUtcStart()
    }

    fun calculateAndRegister() {
        viewModelScope.launch {
            isLoading = true

            // 1️⃣ heavy logic on IO
            val validation = withContext(Dispatchers.IO) {

                val pesoFloat = peso.replace(',', '.').toFloatOrNull()
                var alturaFloat = altura.replace(',', '.').toFloatOrNull()

                if (pesoFloat == null || pesoFloat <= 0 || pesoFloat > 400)
                    return@withContext "Peso inválido."

                if (alturaFloat == null || alturaFloat <= 0)
                    return@withContext "Altura inválida."

                if (alturaFloat > 3) alturaFloat /= 100f

                if (alturaFloat !in 0.5f..2.5f)
                    return@withContext "Altura fora do intervalo."

                val imc = pesoFloat / (alturaFloat * alturaFloat)

                val classificacao = when {
                    imc < 18.5f -> "Abaixo do peso"
                    imc < 25f -> "Peso normal"
                    imc < 30f -> "Sobrepeso"
                    else -> "Obesidade"
                }

                val request = RegistroImcRequest(
                    peso = pesoFloat.toDouble(),
                    altura = alturaFloat.toDouble(),
                    imc = imc.toDouble(),
                    classificacao = classificacao
                )

                // attempt network
                try {
                    val resp = repository.createImcRecord(request)
                    if (resp.isSuccessful) null else "Erro ao registrar IMC."
                } catch (e: Exception) {
                    "Falha na conexão."
                }
            }

            // 2️⃣ apply results on Main
            if (validation != null) {
                isLoading = false
                _eventFlow.emit(UiEvent.ShowSnackbar(validation))
                return@launch
            }

            resetFormState()
            isLoading = false

            _eventFlow.emit(UiEvent.ShowSnackbar("IMC registrado com sucesso!"))
            _eventFlow.emit(UiEvent.NavigateBack)
        }
    }
}
