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

sealed class GraficoUIState {
    object Loading : GraficoUIState()
    data class Success(
        val valores: List<Float>,
        val labels: List<String>
    ) : GraficoUIState()
    data class Error(val message: String) : GraficoUIState()
}

sealed class CompromissoState {
    object NenhumAgendado : CompromissoState()

    data class TodosConcluidos(
        val total: Int
    ) : CompromissoState()

    data class Proximo(
        val compromisso: Compromisso,
        val total: Int,
        val concluidas: Int,
        val isAtrasado: Boolean
    ) : CompromissoState()
}

sealed class ResumoState {
    object Loading : ResumoState()
    data class Success(
        val profile: ImcBaseProfile,
        val ultimoRegistroComposicao: ComposicaoCorporalRegistro?,
        val compromissoState: CompromissoState,
        val graficoImcState: GraficoUIState
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

        val tarefasHoje = compromissos.filter { it.data == hojeStr }
        val totalHoje = tarefasHoje.size
        val concluidasHoje = tarefasHoje.count { it.concluido }

        val atrasados = compromissos.filter { it.data < hojeStr && !it.concluido }.sortedBy { it.data }

        if (atrasados.isNotEmpty()) {
            return CompromissoState.Proximo(
                compromisso = atrasados.first(),
                total = totalHoje,
                concluidas = concluidasHoje,
                isAtrasado = true
            )
        }

        if (totalHoje == 0) return CompromissoState.NenhumAgendado

        val proximoHoje = tarefasHoje
            .filter { !it.concluido && it.hora_inicio >= agoraStr }
            .minByOrNull { it.hora_inicio }

        val pendenteHoje = proximoHoje ?: tarefasHoje.filter { !it.concluido }.minByOrNull { it.hora_inicio }

        return if (pendenteHoje != null) {
            CompromissoState.Proximo(
                compromisso = pendenteHoje,
                total = totalHoje,
                concluidas = concluidasHoje,
                isAtrasado = false
            )
        } else {
            CompromissoState.TodosConcluidos(totalHoje)
        }
    }

    private fun fetchResumoData() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_state.value !is ResumoState.Success) {
                _state.value = ResumoState.Loading
            }

            try {
                val profileJob = async { repository.getImcBaseDashboard() }
                val composicaoJob = async { repository.getHistoricoComposicao() }
                val compromissosJob = async { repository.getCompromissos() }
                val graficoImcJob = async { repository.getHistoricoImc() }

                val profileResponse = profileJob.await()
                val composicaoResponse = composicaoJob.await()
                val compromissosResponse = compromissosJob.await()
                val graficoImcResponse = graficoImcJob.await()

                if (!profileResponse.isSuccessful || profileResponse.body().isNullOrEmpty()) {
                    if (_state.value !is ResumoState.Success) {
                        _state.value = ResumoState.Error("Perfil não encontrado.")
                    }
                    return@launch
                }

                var profile = profileResponse.body()!!.first()
                if (profile.imcResultado < 1.0 && profile.peso > 0 && profile.altura > 0) {
                    val alturaMetros = if (profile.altura > 3.0) profile.altura / 100.0 else profile.altura
                    val imcCorrigido = profile.peso / (alturaMetros * alturaMetros)
                    profile = profile.copy(imcResultado = imcCorrigido)
                }

                val ultimoRegistroComposicao = if (composicaoResponse.isSuccessful) {
                    composicaoResponse.body()?.firstOrNull()
                } else null

                val compromissoState = if (compromissosResponse.isSuccessful) {
                    getCompromissoState(compromissosResponse.body())
                } else {
                    CompromissoState.NenhumAgendado
                }

                val graficoImcState: GraficoUIState = if (graficoImcResponse.isSuccessful) {
                    val lista = graficoImcResponse.body() ?: emptyList()
                    if (lista.isEmpty()) {
                        GraficoUIState.Error("Nenhum histórico de IMC.")
                    } else {
                        val listaInvertida = lista.reversed()
                        val valores = listaInvertida.map {
                            if (it.imcRes < 1.0) (it.imcRes * 10000).toFloat() else it.imcRes.toFloat()
                        }
                        val labels = listaInvertida.map {
                            val partes = it.dataConsulta.split("-")
                            if (partes.size >= 3) "${partes[2]}/${partes[1]}" else ""
                        }
                        GraficoUIState.Success(valores, labels)
                    }
                } else {
                    GraficoUIState.Error("Erro HTTP ${graficoImcResponse.code()}")
                }

                _state.value = ResumoState.Success(
                    profile = profile,
                    ultimoRegistroComposicao = ultimoRegistroComposicao,
                    compromissoState = compromissoState,
                    graficoImcState = graficoImcState
                )

            } catch (e: Exception) {
                if (_state.value !is ResumoState.Success) {
                    _state.value = ResumoState.Error(e.message ?: "Erro de conexão.")
                }
            }
        }
    }
}