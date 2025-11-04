package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.ImcRegistro
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoricoImcViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _registrosImc = MutableStateFlow<List<ImcRegistro>>(emptyList())
    val registrosImc: StateFlow<List<ImcRegistro>> = _registrosImc

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Estado para o loading do botão de exclusão
    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    fun buscarHistorico() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val resposta = repository.getHistoricoImc()
                _registrosImc.value = resposta
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Lidar com erro de busca
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun deletarRegistro(id: Int) {
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                // 1. Deleta o item no backend
                repository.deleteImcRegistro(id)

                // 2. Busca a lista ATUALIZADA no backend
                val novaLista = repository.getHistoricoImc()

                // 3. Atualiza o StateFlow com a nova lista
                _registrosImc.value = novaLista

                // 4. REMOVEMOS a chamada separada para buscarHistorico()
                // buscarHistorico() // <-- Removido

            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: Lidar com erro de exclusão
            } finally {
                _isDeleting.value = false
            }
        }
    }
}