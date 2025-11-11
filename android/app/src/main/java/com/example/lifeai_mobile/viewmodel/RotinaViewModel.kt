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
sealed class RotinaUIState {
    object Loading : RotinaUIState()
    data class Success(val compromissos: List<Compromisso>) : RotinaUIState()
    data class Error(val message: String) : RotinaUIState()
}

class RotinaViewModel(
    // A gente recebe o repositório, mas não vamos usar ele ainda
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RotinaUIState>(RotinaUIState.Loading)
    val uiState: StateFlow<RotinaUIState> = _uiState.asStateFlow()

    // --- NOSSA LISTA EM MEMÓRIA (PARA A DEMO) ---
    private val compromissosEmMemoria = mutableListOf<Compromisso>()
    // ------------------------------------------

    init {
        // Carrega os compromissos (da memória) assim que o ViewModel é criado
        carregarCompromissos()
    }

    fun carregarCompromissos() {
        viewModelScope.launch {
            _uiState.value = RotinaUIState.Loading

            // --- LÓGICA DA DEMO ---
            // Simplesmente atualiza a tela com a nossa lista em memória
            _uiState.value = RotinaUIState.Success(compromissosEmMemoria.toList())
            // --- FIM DA LÓGICA DA DEMO ---

            /*
            // TODO (Backend): Quando o back estiver pronto, substituir a lógica acima por isso:
            try {
                val response = authRepository.getCompromissos()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = RotinaUIState.Success(response.body()!!)
                } else {
                    _uiState.value = RotinaUIState.Error("Falha ao carregar compromissos: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = RotinaUIState.Error("Erro de rede: ${e.message}")
            }
            */
        }
    }

    fun adicionarCompromisso(titulo: String, data: String, horaInicio: String, horaFim: String) {
        viewModelScope.launch {
            val novoCompromisso = Compromisso(
                titulo = titulo,
                data = data, // Formato "YYYY-MM-DD"
                hora_inicio = horaInicio, // Formato "HH:MM"
                hora_fim = horaFim, // Formato "HH:MM"
                concluido = false
            )

            // --- LÓGICA DA DEMO ---
            // Adiciona na lista em memória e atualiza a tela
            compromissosEmMemoria.add(novoCompromisso)
            _uiState.value = RotinaUIState.Success(compromissosEmMemoria.toList())
            // --- FIM DA LÓGICA DA DEMO ---

            /*
            // TODO (Backend): Quando o back estiver pronto, substituir a lógica acima por isso:
            try {
                val response = authRepository.createCompromisso(novoCompromisso)
                if (response.isSuccessful) {
                    carregarCompromissos() // Recarrega do servidor
                } else {
                    Log.e("RotinaViewModel", "Falha ao criar compromisso: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("RotinaViewModel", "Exceção ao criar compromisso: ${e.message}")
            }
            */
        }
    }

    fun deletarCompromisso(compromisso: Compromisso) {
        viewModelScope.launch {

            // --- LÓGICA DA DEMO ---
            // Remove da lista em memória pelo ID e atualiza a tela
            compromissosEmMemoria.remove(compromisso)
            _uiState.value = RotinaUIState.Success(compromissosEmMemoria.toList())
            // --- FIM DA LÓGICA DA DEMO ---

            /*
            // TODO (Backend): Quando o back estiver pronto, substituir a lógica acima por isso:
            // (Note que o 'id' aqui será um Int do banco, não um UUID)
            try {
                val response = authRepository.deleteCompromisso(compromisso.id.toIntOrNull() ?: 0)
                if (response.isSuccessful) {
                    carregarCompromissos() // Recarrega do servidor
                } else {
                    Log.e("RotinaViewModel", "Falha ao deletar compromisso: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("RotinaViewModel", "Exceção ao deletar compromisso: ${e.message}")
            }
            */
        }
    }
}