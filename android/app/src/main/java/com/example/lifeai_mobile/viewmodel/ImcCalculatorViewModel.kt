package com.example.lifeai_mobile.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.RegistroImcRequest // <--- CORREÇÃO: Importando o modelo novo
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
                val response = repository.getImcBaseDashboard()
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val profile = response.body()!!.first()
                    idade = profile.idade.toString()
                    sexo = profile.sexo

                    if (profile.altura > 0) {
                        var alturaMetros = profile.altura
                        // Se vier em CM (ex: 180), converte para metros visualmente (1.80)
                        if (alturaMetros > 3) {
                            alturaMetros /= 100f
                        }
                        val alturaFormatada = String.format(Locale.US, "%.2f", alturaMetros)
                        onAlturaChange(alturaFormatada) // Atualiza a UI
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
        dataConsulta = newDate
    }

    fun onUnlockHeightField() {
        isHeightFieldLocked = false
    }

    private fun resetFormState() {
        peso = ""
        isHeightFieldLocked = true
        dataConsulta = getTodayAtUtcStart()
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

            // Garante que alturaFloat esteja em Metros para o cálculo do IMC
            if (alturaFloat > 3) alturaFloat /= 100f

            if (alturaFloat < 0.5f || alturaFloat > 2.5f) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Altura fora do intervalo (0.5m a 2.5m)"))
                isLoading = false
                return@launch
            }

            // --- LÓGICA DE CÁLCULO (Adicionada para atender o novo modelo) ---
            val imcValor = pesoFloat / (alturaFloat * alturaFloat)

            val classificacao = when {
                imcValor < 18.5f -> "Abaixo do peso"
                imcValor < 25f -> "Peso normal"
                imcValor < 30f -> "Sobrepeso"
                else -> "Obesidade"
            }
            // ----------------------------------------------------------------

            // Define o formatter para usar UTC
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val dataFormatada = dateFormat.format(dataConsulta)

            // --- CRIAÇÃO DO NOVO REQUEST ---
            // O Backend espera a altura em CM (baseado no Onboarding), então multiplicamos por 100
            // O Backend não pede mais idade e sexo no registro de IMC (pega do perfil)
            val request = RegistroImcRequest(
                peso = pesoFloat.toDouble(),
                altura = (alturaFloat * 100).toDouble(),
                imc = imcValor.toDouble(),
                classificacao = classificacao
                // dataConsulta = dataFormatada -> Se o backend aceitar data, descomente e adicione no data class
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