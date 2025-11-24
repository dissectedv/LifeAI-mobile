package com.example.lifeai_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.ComposicaoCorporalRegistro
import com.example.lifeai_mobile.model.Compromisso
import com.example.lifeai_mobile.model.ImcRegistro
import com.example.lifeai_mobile.model.PerfilResponse
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
    data class TodosConcluidos(val total: Int) : CompromissoState()
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
        val perfil: PerfilResponse,
        val ultimoImc: ImcRegistro?,
        val ultimoRegistroComposicao: ComposicaoCorporalRegistro?,
        val compromissoState: CompromissoState,
        val graficoImcState: GraficoUIState
    ) : ResumoState()

    data class Error(val message: String) : ResumoState()
}

class ResumoViewModel(private val repository: AuthRepository) : ViewModel() {

    private val TAG = "ResumoVM"

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
            return CompromissoState.Proximo(atrasados.first(), totalHoje, concluidasHoje, true)
        }

        if (totalHoje == 0) return CompromissoState.NenhumAgendado

        val proximoHoje = tarefasHoje
            .filter { !it.concluido && it.hora_inicio >= agoraStr }
            .minByOrNull { it.hora_inicio }

        val pendenteHoje = proximoHoje ?: tarefasHoje.filter { !it.concluido }.minByOrNull { it.hora_inicio }

        return if (pendenteHoje != null) {
            CompromissoState.Proximo(pendenteHoje, totalHoje, concluidasHoje, false)
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
                val profileJob = async { repository.getProfileData() }
                val imcJob = async { repository.getHistoricoImc() }
                val composicaoJob = async { repository.getHistoricoComposicao() }
                val compromissosJob = async { repository.getCompromissos() }

                val profileResponse = profileJob.await()
                val imcHistoryResponse = imcJob.await()
                val composicaoResponse = composicaoJob.await()
                val compromissosResponse = compromissosJob.await()

                if (!profileResponse.isSuccessful || profileResponse.body() == null) {
                    _state.value = ResumoState.Error("Não foi possível carregar o perfil.")
                    return@launch
                }

                val perfilData = profileResponse.body()!!

                // --- PROCESSAMENTO DO IMC (LIMPO) ---
                var ultimoImcRegistro: ImcRegistro? = null
                val graficoState: GraficoUIState

                if (imcHistoryResponse.isSuccessful && !imcHistoryResponse.body().isNullOrEmpty()) {
                    val listaBruta = imcHistoryResponse.body()!!

                    // 1. Ordena do ANTIGO para o NOVO usando a data
                    val listaOrdenada = listaBruta.sortedBy { it.dataConsulta }

                    // 2. O último da lista ordenada é o registro mais atual
                    ultimoImcRegistro = listaOrdenada.lastOrNull()

                    // 3. Para o gráfico, pegamos os últimos 10 (que são os mais recentes na lista ordenada)
                    val listaParaGrafico = listaOrdenada.takeLast(10)

                    val valores = listaParaGrafico.map { it.imcRes.toFloat() }

                    val labels = listaParaGrafico.map {
                        // Formata "yyyy-MM-dd" para "dd/MM"
                        val partes = it.dataConsulta.split("-")
                        if (partes.size >= 3) "${partes[2]}/${partes[1]}" else ""
                    }

                    graficoState = GraficoUIState.Success(valores, labels)
                } else {
                    graficoState = GraficoUIState.Error("Sem histórico")
                }
                // ------------------------------------

                val ultimoRegistroComposicao = if (composicaoResponse.isSuccessful) {
                    composicaoResponse.body()?.firstOrNull()
                } else null

                val compromissoState = if (compromissosResponse.isSuccessful) {
                    getCompromissoState(compromissosResponse.body())
                } else {
                    CompromissoState.NenhumAgendado
                }

                _state.value = ResumoState.Success(
                    perfil = perfilData,
                    ultimoImc = ultimoImcRegistro,
                    ultimoRegistroComposicao = ultimoRegistroComposicao,
                    compromissoState = compromissoState,
                    graficoImcState = graficoState
                )

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao carregar resumo: ${e.message}")
                if (_state.value !is ResumoState.Success) {
                    _state.value = ResumoState.Error("Erro de conexão.")
                }
            }
        }
    }
}