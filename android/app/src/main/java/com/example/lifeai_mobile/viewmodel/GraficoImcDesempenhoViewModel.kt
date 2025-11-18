package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GraficoUIState {
    object Loading : GraficoUIState()
    data class Success(
        val valores: List<Float>,
        val labels: List<String>
    ) : GraficoUIState()
    data class Error(val message: String) : GraficoUIState()
}

class GraficoImcDesempenhoViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GraficoUIState>(GraficoUIState.Loading)
    val uiState: StateFlow<GraficoUIState> = _uiState.asStateFlow()

    init {
        carregarHistorico()
    }

    private fun carregarHistorico() {
        viewModelScope.launch {
            try {
                _uiState.value = GraficoUIState.Loading

                val response = repository.getImcHistorico()

                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()

                    val valores = lista.map { (it.imc ?: 0.0).toFloat() }
                    val labels = lista.map {
                        it.data?.split("-")?.lastOrNull() ?: ""
                    }

                    _uiState.value = GraficoUIState.Success(valores, labels)

                } else {
                    _uiState.value = GraficoUIState.Error("Erro HTTP ${response.code()}")
                }

            } catch (e: Exception) {
                _uiState.value = GraficoUIState.Error(e.message ?: "Erro ao carregar")
            }
        }
    }
}
