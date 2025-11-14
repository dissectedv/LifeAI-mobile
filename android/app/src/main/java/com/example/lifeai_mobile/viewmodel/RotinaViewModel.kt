package com.example.lifeai_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.Compromisso
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Define os estados da tela: Carregando, Sucesso (com dados) ou Erro

private const val TAG = "RotinaViewModel"
sealed class RotinaUIState {
    object Loading : RotinaUIState()
    data class Success(val compromissos: List<Compromisso>) : RotinaUIState()
    data class Error(val message: String) : RotinaUIState()
}

class RotinaViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RotinaUIState>(RotinaUIState.Loading)
    val uiState: StateFlow<RotinaUIState> = _uiState.asStateFlow()

    init {
        carregarCompromissos()
    }

    fun carregarCompromissos() {
        Log.d(TAG, "Iniciando carregarCompromissos()...")
        viewModelScope.launch {

            _uiState.value = RotinaUIState.Loading
            try {
                val response = authRepository.getCompromissos()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = RotinaUIState.Success(response.body()!!)
                } else {
                    _uiState.value = RotinaUIState.Error("Falha ao carregar: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = RotinaUIState.Error("Erro de rede: ${e.message}")
            }
        }
    }

    fun adicionarCompromisso(titulo: String, data: String, horaInicio: String, horaFim: String) {
        Log.d(TAG, "Iniciando adicionarCompromisso(): $titulo")
        viewModelScope.launch {
            val novo = Compromisso(
                titulo = titulo,
                data = data,
                hora_inicio = horaInicio,
                hora_fim = horaFim
            )

            try {
                val response = authRepository.createCompromisso(novo)
                if (response.isSuccessful) {
                    carregarCompromissos()
                } else {
                    Log.e("RotinaViewModel", "Erro ao criar: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao adicionar compromisso: ${e.message}", e)
            }
        }
    }

    fun deletarCompromisso(compromisso: Compromisso) {
        Log.d(TAG, "Iniciando deletarCompromisso(): ${compromisso.id}")
        viewModelScope.launch {
            try {
                val id = compromisso.id ?: return@launch
                val response = authRepository.deleteCompromisso(id)
                if (response.isSuccessful) {
                    carregarCompromissos()
                } else {
                    Log.e("RotinaViewModel", "Erro ao deletar: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("RotinaViewModel", "Exceção ao deletar: ${e.message}")
            }
        }
    }
}