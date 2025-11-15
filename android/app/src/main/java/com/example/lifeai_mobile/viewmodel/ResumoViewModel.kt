package com.example.lifeai_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.ImcBaseProfile
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ResumoState {
    object Loading : ResumoState()
    data class Success(val profile: ImcBaseProfile) : ResumoState()
    data class Error(val message: String) : ResumoState()
}

class ResumoViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow<ResumoState>(ResumoState.Loading)
    val state: StateFlow<ResumoState> = _state

    init {
        fetchProfile()
    }

    private fun fetchProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getImcBaseDashboard()
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val profile = response.body()!!.first()
                    _state.value = ResumoState.Success(profile)
                    Log.d("API_RESPONSE", "Dados do perfil recebidos: $profile")
                } else {
                    _state.value = ResumoState.Error("Perfil não encontrado.")
                }
            } catch (e: Exception) {
                _state.value = ResumoState.Error(e.message ?: "Erro de conexão.")
            }
        }
    }
}