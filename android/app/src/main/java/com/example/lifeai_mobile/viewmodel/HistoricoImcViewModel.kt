package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.ImcRegistro
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ImcHistoryState {
    object Loading : ImcHistoryState()
    data class Success(val historico: List<ImcRegistro>) : ImcHistoryState()
    data class Error(val message: String) : ImcHistoryState()
}

class HistoricoImcViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ImcHistoryState>(ImcHistoryState.Loading)
    val state: StateFlow<ImcHistoryState> = _state.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    init {
        buscarHistorico()
    }

    fun buscarHistorico() {
        viewModelScope.launch {
            _state.value = ImcHistoryState.Loading
            try {
                val response = repository.getHistoricoImc()
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    _state.value = ImcHistoryState.Success(lista)
                } else {
                    _state.value = ImcHistoryState.Error("Falha ao buscar histórico: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = ImcHistoryState.Error(e.message ?: "Falha ao buscar histórico")
            }
        }
    }

    fun deletarRegistro(id: Int) {
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                repository.deleteImcRegistro(id)

                val response = repository.getHistoricoImc()
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    _state.value = ImcHistoryState.Success(lista)
                } else {
                    _state.value = ImcHistoryState.Error("Falha ao atualizar lista: ${response.code()}")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = ImcHistoryState.Error(e.message ?: "Falha ao deletar registro")
            } finally {
                _isDeleting.value = false
            }
        }
    }
}