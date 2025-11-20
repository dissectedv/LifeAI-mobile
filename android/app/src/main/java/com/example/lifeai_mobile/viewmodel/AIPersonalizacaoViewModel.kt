package com.example.lifeai_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.PerfilRequest      // <--- NOVO
import com.example.lifeai_mobile.model.RegistroImcRequest // <--- NOVO
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
                // 1. Busca Perfil e Histórico de IMC em paralelo
                val profileJob = async { repository.getProfileData() }
                val imcJob = async { repository.getHistoricoImc() }

                val profileResponse = profileJob.await()
                val imcResponse = imcJob.await()

                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    val profile = profileResponse.body()!!

                    // Preenche dados do Perfil
                    nome.value = profile.nome
                    idade.value = profile.idade.toString()
                    sexo.value = profile.sexo
                    objetivo.value = profile.objetivo

                    // Preenche dados de Peso/Altura (pegando do último registro de IMC)
                    if (imcResponse.isSuccessful && !imcResponse.body().isNullOrEmpty()) {
                        val ultimoRegistro = imcResponse.body()!!.last() // Assume ordem cronológica

                        peso.value = ultimoRegistro.peso.toString()

                        // Altura vem do backend. Se vier em cm (ex 180), ok.
                        // Se vier em metros (1.80), o user vê em metros.
                        // Vamos assumir que o backend manda como foi salvo.
                        altura.value = ultimoRegistro.altura.toString()
                    }

                    _uiState.value = PersonalizacaoState.Content
                } else {
                    _uiState.value = PersonalizacaoState.Error("Erro ao carregar dados: ${profileResponse.code()}")
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
                val alturaInput = altura.value.toDoubleOrNull() ?: 0.0

                // Normalização de altura para o cálculo do IMC (deve ser em Metros)
                val alturaMetros = if (alturaInput > 3.0) alturaInput / 100.0 else alturaInput
                // Altura para salvar no banco (geralmente CM, mas depende do padrão. Vamos salvar o input direto ou converter pra CM se for < 3)
                val alturaParaSalvar = if (alturaInput < 3.0) alturaInput * 100 else alturaInput

                if (nome.value.isBlank() || idadeInt <= 0 || pesoDouble <= 0 || alturaMetros <= 0) {
                    _uiState.value = PersonalizacaoState.Error("Preencha todos os campos corretamente.")
                    return@launch
                }

                val imcCalculado = pesoDouble / (alturaMetros * alturaMetros)
                val classificacaoCalc = when {
                    imcCalculado < 18.5 -> "Abaixo do peso"
                    imcCalculado < 25.0 -> "Peso normal"
                    imcCalculado < 30.0 -> "Sobrepeso"
                    else -> "Obesidade"
                }

                // PASSO 1: Atualizar Perfil
                val perfilRequest = PerfilRequest(
                    nome = nome.value,
                    idade = idadeInt,
                    sexo = sexo.value,
                    objetivo = objetivo.value
                )
                val resPerfil = repository.updateProfileData(perfilRequest)

                if (!resPerfil.isSuccessful) {
                    _uiState.value = PersonalizacaoState.Error("Erro ao salvar perfil: ${resPerfil.code()}")
                    return@launch
                }

                // PASSO 2: Criar novo registro de IMC (Atualizando peso/altura)
                val imcRequest = RegistroImcRequest(
                    peso = pesoDouble,
                    altura = alturaParaSalvar,
                    imc = imcCalculado,
                    classificacao = classificacaoCalc
                )
                val resImc = repository.createImcRecord(imcRequest)

                if (resImc.isSuccessful) {
                    Log.d("LifeAI_Update", "✅ Sucesso! Dados atualizados.")
                    _uiState.value = PersonalizacaoState.Success
                } else {
                    Log.e("LifeAI_Update", "❌ Falha ao salvar IMC: ${resImc.code()}")
                    // Consideramos sucesso se o perfil salvou, mas avisamos do erro?
                    // Ou damos erro geral? Vamos dar erro geral para forçar consistência.
                    _uiState.value = PersonalizacaoState.Error("Perfil salvo, mas erro ao atualizar medidas.")
                }

            } catch (e: Exception) {
                Log.e("LifeAI_Update", "❌ Erro de exceção: ${e.message}")
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