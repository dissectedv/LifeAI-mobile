package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.ImcBaseProfile
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PersonalizacaoState {
    object Loading : PersonalizacaoState()
    object Content : PersonalizacaoState()
    object Saving : PersonalizacaoState()
    object Success : PersonalizacaoState()
    data class Error(val message: String) : PersonalizacaoState()
}

class AIPersonalizacaoViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PersonalizacaoState>(PersonalizacaoState.Loading)
    val uiState: StateFlow<PersonalizacaoState> = _uiState.asStateFlow()

    val nome = MutableStateFlow("")
    val idade = MutableStateFlow("")
    val peso = MutableStateFlow("")
    val altura = MutableStateFlow("")
    val sexo = MutableStateFlow("")
    val objetivo = MutableStateFlow("")

    init {
        carregarDadosAtuais()
    }

    fun carregarDadosAtuais() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PersonalizacaoState.Loading
            try {
                val response = repository.getProfileData()
                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!

                    nome.value = profile.nome
                    idade.value = profile.idade.toString()
                    peso.value = profile.peso.toString()
                    altura.value = profile.altura.toString()
                    sexo.value = profile.sexo
                    objetivo.value = profile.objetivo

                    _uiState.value = PersonalizacaoState.Content
                } else {
                    _uiState.value = PersonalizacaoState.Error("Erro ao carregar dados: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = PersonalizacaoState.Error(e.message ?: "Erro de conexão")
            }
        }
    }

    fun salvarAlteracoes() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PersonalizacaoState.Saving
            try {
                val idadeInt = idade.value.toIntOrNull() ?: 0
                val pesoDouble = peso.value.toDoubleOrNull() ?: 0.0
                val alturaDouble = altura.value.toDoubleOrNull() ?: 0.0

                if (nome.value.isBlank() || idadeInt <= 0 || pesoDouble <= 0 || alturaDouble <= 0) {
                    _uiState.value = PersonalizacaoState.Error("Preencha todos os campos corretamente.")
                    return@launch
                }

                val imcCalculado = pesoDouble / (alturaDouble * alturaDouble)
                val classificacaoCalc = when {
                    imcCalculado < 18.5 -> "Abaixo do peso"
                    imcCalculado < 25.0 -> "Peso normal"
                    imcCalculado < 30.0 -> "Sobrepeso"
                    else -> "Obesidade"
                }

                val profileAtualizado = ImcBaseProfile(
                    nome = nome.value,
                    idade = idadeInt,
                    peso = pesoDouble,
                    altura = alturaDouble,
                    sexo = sexo.value,
                    objetivo = objetivo.value,
                    imcResultado = imcCalculado,
                    classificacao = classificacaoCalc
                )

                val response = repository.updateProfileData(profileAtualizado)

                if (response.isSuccessful) {
                    _uiState.value = PersonalizacaoState.Success
                } else {
                    _uiState.value = PersonalizacaoState.Error("Falha ao salvar: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = PersonalizacaoState.Error(e.message ?: "Erro ao salvar dados")
            }
        }
    }

    fun resetState() {
        if (_uiState.value is PersonalizacaoState.Success || _uiState.value is PersonalizacaoState.Error) {
            _uiState.value = PersonalizacaoState.Content
        }
    }
}