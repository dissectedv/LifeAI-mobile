package com.example.lifeai_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.PerfilRequest
import com.example.lifeai_mobile.model.RegistroImcRequest
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class PersonalizacaoState {
    object Loading : PersonalizacaoState()
    object Content : PersonalizacaoState()
    object Saving : PersonalizacaoState()
    object Success : PersonalizacaoState()
    data class Error(val message: String) : PersonalizacaoState()
    object NoChanges : PersonalizacaoState()
}

private data class FormSnapshot(
    val nome: String,
    val idade: String,
    val peso: String,
    val altura: String,
    val sexo: String,
    val objetivo: String,
    val restricoes: String,
    val observacoes: String
)

class AIPersonalizacaoViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val TAG = "PersonalizacaoVM"

    private val _uiState = MutableStateFlow<PersonalizacaoState>(PersonalizacaoState.Loading)
    val uiState: StateFlow<PersonalizacaoState> = _uiState.asStateFlow()

    val nome = MutableStateFlow("")
    val idade = MutableStateFlow("")
    val peso = MutableStateFlow("")
    val altura = MutableStateFlow("")
    val sexo = MutableStateFlow("")
    val objetivo = MutableStateFlow("")
    val restricoes = MutableStateFlow("")
    val observacoes = MutableStateFlow("")

    private var dadosOriginais: FormSnapshot? = null

    init {
        carregarDadosAtuais()
    }

    fun carregarDadosAtuais() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PersonalizacaoState.Loading

            try {
                val profileJob = async { repository.getProfileData() }
                val imcJob = async { repository.getHistoricoImc() }

                val profileResponse = profileJob.await()
                val imcResponse = imcJob.await()

                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    val profile = profileResponse.body()!!

                    nome.value = profile.nome
                    idade.value = profile.idade.toString()
                    sexo.value = profile.sexo
                    objetivo.value = profile.objetivo
                    restricoes.value = profile.restricoesAlimentares ?: ""
                    observacoes.value = profile.observacaoSaude ?: ""

                    var pesoStr = ""
                    var alturaStr = ""

                    if (imcResponse.isSuccessful && !imcResponse.body().isNullOrEmpty()) {
                        val ultimoRegistro = imcResponse.body()!!.last()
                        pesoStr = ultimoRegistro.peso.toString()
                        alturaStr = formatarAltura(ultimoRegistro.altura)

                        peso.value = pesoStr
                        altura.value = alturaStr
                    }

                    dadosOriginais = FormSnapshot(
                        nome = profile.nome,
                        idade = profile.idade.toString(),
                        peso = pesoStr,
                        altura = alturaStr,
                        sexo = profile.sexo,
                        objetivo = profile.objetivo,
                        restricoes = profile.restricoesAlimentares ?: "",
                        observacoes = profile.observacaoSaude ?: ""
                    )

                    _uiState.value = PersonalizacaoState.Content
                } else {
                    _uiState.value = PersonalizacaoState.Error("Erro ao carregar: ${profileResponse.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = PersonalizacaoState.Error(e.message ?: "Erro de conexão")
            }
        }
    }

    fun salvarAlteracoes() {
        val dadosAtuais = FormSnapshot(
            nome = nome.value.trim(),
            idade = idade.value.trim(),
            peso = peso.value.trim(),
            altura = altura.value.trim(),
            sexo = sexo.value,
            objetivo = objetivo.value.trim(),
            restricoes = restricoes.value.trim(),
            observacoes = observacoes.value.trim()
        )

        if (dadosOriginais != null && dadosOriginais == dadosAtuais) {
            _uiState.value = PersonalizacaoState.NoChanges
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PersonalizacaoState.Saving

            try {
                val idadeInt = idade.value.toIntOrNull() ?: 0

                val pesoDouble = peso.value.replace(',', '.').toDoubleOrNull() ?: 0.0
                val alturaInput = altura.value.replace(',', '.').toDoubleOrNull() ?: 0.0

                val alturaMetros = if (alturaInput > 3.0) alturaInput / 100.0 else alturaInput

                if (nome.value.isBlank() || idadeInt <= 0) {
                    _uiState.value = PersonalizacaoState.Error("Nome e idade são obrigatórios.")
                    return@launch
                }

                val perfilRequest = PerfilRequest(
                    nome = nome.value,
                    idade = idadeInt,
                    sexo = sexo.value,
                    objetivo = objetivo.value,
                    restricoesAlimentares = if (restricoes.value.isBlank()) null else restricoes.value,
                    observacaoSaude = if (observacoes.value.isBlank()) null else observacoes.value
                )
                val resPerfil = repository.updateProfileData(perfilRequest)

                if (!resPerfil.isSuccessful) {
                    _uiState.value = PersonalizacaoState.Error("Erro ao salvar perfil.")
                    return@launch
                }

                val pesoMudou = dadosOriginais?.peso != peso.value.trim()
                val alturaMudou = dadosOriginais?.altura != altura.value.trim()

                if ((pesoMudou || alturaMudou) && pesoDouble > 0 && alturaMetros > 0) {

                    val imcCalculado = pesoDouble / (alturaMetros * alturaMetros)
                    val classificacaoCalc = calcularClassificacao(imcCalculado)

                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val dataFormatada = sdf.format(Date())

                    val imcRequest = RegistroImcRequest(
                        peso = pesoDouble,
                        altura = alturaMetros,
                        imc = imcCalculado,
                        classificacao = classificacaoCalc,
                        data = dataFormatada
                    )

                    val resImc = repository.createImcRecord(imcRequest)
                    if (!resImc.isSuccessful) {
                        _uiState.value = PersonalizacaoState.Error("Perfil salvo, mas falha ao registrar IMC.")
                        return@launch
                    }
                }

                dadosOriginais = dadosAtuais
                _uiState.value = PersonalizacaoState.Success

            } catch (e: Exception) {
                _uiState.value = PersonalizacaoState.Error("Erro: ${e.message}")
            }
        }
    }

    fun resetState() {
        if (_uiState.value !is PersonalizacaoState.Loading &&
            _uiState.value !is PersonalizacaoState.Saving) {
            _uiState.value = PersonalizacaoState.Content
        }
    }

    private fun calcularClassificacao(imc: Double): String {
        return when {
            imc < 18.5 -> "Abaixo do peso"
            imc < 25.0 -> "Peso normal"
            imc < 30.0 -> "Sobrepeso"
            else -> "Obesidade"
        }
    }

    private fun formatarAltura(valor: Double): String {
        return if (valor < 3.0) {
            String.format("%.2f", valor)
        } else {
            valor.toInt().toString()
        }
    }
}