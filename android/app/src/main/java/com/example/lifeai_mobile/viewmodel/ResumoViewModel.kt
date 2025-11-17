package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.ComposicaoCorporalRegistro
import com.example.lifeai_mobile.model.Compromisso
import com.example.lifeai_mobile.model.ImcBaseProfile
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class CompromissoState {
    object NenhumAgendado : CompromissoState()
    object TodosConcluidos : CompromissoState()
    data class Proximo(val compromisso: Compromisso) : CompromissoState()
}

sealed class ResumoState {
    object Loading : ResumoState()
    data class Success(
        val profile: ImcBaseProfile,
        val ultimoRegistroComposicao: ComposicaoCorporalRegistro?,
        val compromissoState: CompromissoState
    ) : ResumoState()
    data class Error(val message: String) : ResumoState()
}

class ResumoViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow<ResumoState>(ResumoState.Loading)
    val state: StateFlow<ResumoState> = _state

    init {
        fetchResumoData()
    }

    fun atualizarResumo() {
        fetchResumoData()
    }

    private fun getCompromissoState(compromissos: List<Compromisso>?): CompromissoState {
        if (compromissos.isNullOrEmpty()) return CompromissoState.NenhumAgendado

        val hojeStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val agoraStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        val futuros = compromissos
            .filter { !it.concluido && (it.data > hojeStr || (it.data == hojeStr && it.hora_inicio > agoraStr)) }
            .sortedWith(compareBy({ it.data }, { it.hora_inicio }))

        return if (futuros.isNotEmpty()) {
            CompromissoState.Proximo(futuros.first())
        } else {
            CompromissoState.TodosConcluidos
        }
    }

    private fun fetchResumoData() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = ResumoState.Loading
            try {
                val profileJob = async { repository.getImcBaseDashboard() }
                val composicaoJob = async { repository.getHistoricoComposicao() }
                val compromissosJob = async { repository.getCompromissos() }

                val profileResponse = profileJob.await()
                val composicaoResponse = composicaoJob.await()
                val compromissosResponse = compromissosJob.await()

                if (!profileResponse.isSuccessful || profileResponse.body().isNullOrEmpty()) {
                    _state.value = ResumoState.Error("Perfil não encontrado.")
                    return@launch
                }

                val profile = profileResponse.body()!!.first()

                val ultimoRegistroComposicao = if (composicaoResponse.isSuccessful) {
                    composicaoResponse.body()?.firstOrNull()
                } else null

                val compromissoState = if (compromissosResponse.isSuccessful) {
                    getCompromissoState(compromissosResponse.body())
                } else {
                    CompromissoState.NenhumAgendado
                }

                _state.value = ResumoState.Success(profile, ultimoRegistroComposicao, compromissoState)
            } catch (e: Exception) {
                _state.value = ResumoState.Error(e.message ?: "Erro de conexão.")
            }
        }
    }
}