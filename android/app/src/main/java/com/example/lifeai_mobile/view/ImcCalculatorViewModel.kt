package com.example.lifeai_mobile.view

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

// Eventos para a UI (ex: mostrar mensagens)
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    object NavigateBack : UiEvent()
}

class ImcCalculatorViewModel(private val repository: AuthRepository) : ViewModel() {

    // Estados dos campos do formulário
    var idade by mutableStateOf("")
    var sexo by mutableStateOf("")
    var peso by mutableStateOf("")
    var altura by mutableStateOf("")
    var dataConsulta by mutableStateOf(Date()) // Começa com a data de hoje

    var isLoading by mutableStateOf(false)

    // Canal para enviar eventos para a UI
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        // Busca os dados base do usuário (idade e sexo) ao iniciar
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.getImcBaseDashboard()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val profile = response.body()!!.first()
                idade = profile.idade.toString()
                sexo = profile.sexo
            }
        }
    }

    fun onPesoChange(newValue: String) { peso = newValue }
    fun onAlturaChange(newValue: String) { altura = newValue }
    fun onDataChange(newDate: Date) { dataConsulta = newDate }

    fun calculateAndRegister() {

        Log.d("IMC_CALC_DEBUG", "Iniciando cálculo. Peso: '$peso', Altura: '$altura', Data: '$dataConsulta'")

        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true

            // --- Validação (lógica do Angular) ---
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
            // Normaliza a altura para metros
            if (alturaFloat > 3) alturaFloat /= 100f

            if (alturaFloat < 0.5f || alturaFloat > 2.5f) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Altura fora do intervalo (0.5m a 2.5m)"))
                isLoading = false
                return@launch
            }

            // Formata a data para "YYYY-MM-DD"
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
                    _eventFlow.emit(UiEvent.ShowSnackbar("IMC registrado com sucesso!"))
                    // 👇 EMITIR O EVENTO DE NAVEGAÇÃO
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