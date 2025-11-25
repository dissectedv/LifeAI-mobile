package com.example.lifeai_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.ComposicaoCorporalRegistro
import com.example.lifeai_mobile.model.ComposicaoCorporalRequest
import com.example.lifeai_mobile.model.PerfilResponse
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10

sealed class ComposicaoCorporalState {
    object Loading : ComposicaoCorporalState()
    object Empty : ComposicaoCorporalState()
    data class Success(
        val ultimoRegistro: ComposicaoCorporalRegistro?,
        val analise: AnaliseComposicao?,
        val historico: List<ComposicaoCorporalRegistro>
    ) : ComposicaoCorporalState()
    data class Error(val message: String) : ComposicaoCorporalState()
}

data class AnaliseComposicao(
    val analiseGordura: AnaliseItem,
    val analiseMusculo: AnaliseItem,
    val analiseAgua: AnaliseItem
)

data class AnaliseItem(
    val valor: String,
    val status: AnaliseStatus,
    val mensagem: String
)

enum class AnaliseStatus {
    OTIMO, BOM, ALERTA, BAIXO
}

class ComposicaoCorporalViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ComposicaoCorporalState>(ComposicaoCorporalState.Loading)
    val state: StateFlow<ComposicaoCorporalState> = _state.asStateFlow()

    private var perfilUsuario: PerfilResponse? = null

    private val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _gorduraPercentual = MutableStateFlow("")
    val gorduraPercentual: StateFlow<String> = _gorduraPercentual.asStateFlow()

    private val _musculoPercentual = MutableStateFlow("")
    val musculoPercentual: StateFlow<String> = _musculoPercentual.asStateFlow()

    private val _aguaPercentual = MutableStateFlow("")
    val aguaPercentual: StateFlow<String> = _aguaPercentual.asStateFlow()

    private val _gorduraVisceral = MutableStateFlow("")
    val gorduraVisceral: StateFlow<String> = _gorduraVisceral.asStateFlow()

    private val _alturaEstimador = MutableStateFlow("")
    val alturaEstimador: StateFlow<String> = _alturaEstimador.asStateFlow()

    private val _pescocoEstimador = MutableStateFlow("")
    val pescocoEstimador: StateFlow<String> = _pescocoEstimador.asStateFlow()

    private val _cinturaEstimador = MutableStateFlow("")
    val cinturaEstimador: StateFlow<String> = _cinturaEstimador.asStateFlow()

    private val _quadrilEstimador = MutableStateFlow("")
    val quadrilEstimador: StateFlow<String> = _quadrilEstimador.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                loadProfile()
                loadHistory()
            } catch (e: Exception) {
                _state.value = ComposicaoCorporalState.Error(e.message ?: "Falha ao carregar dados")
            }
        }
    }

    private suspend fun loadProfile() {
        val response = repository.getProfileData()
        if (response.isSuccessful && response.body() != null) {
            perfilUsuario = response.body()
        } else {
            Log.e("ComposicaoVM", "Falha ao carregar perfil: ${response.code()}")
        }
    }

    fun loadHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = ComposicaoCorporalState.Loading
            try {
                val response = repository.getHistoricoComposicao()
                if (response.isSuccessful) {
                    val historico = response.body() ?: emptyList()
                    if (historico.isEmpty()) {
                        _state.value = ComposicaoCorporalState.Empty
                    } else {
                        val ultimoRegistro = historico.first()
                        val penultimoRegistro = historico.getOrNull(1)

                        _state.value = ComposicaoCorporalState.Success(
                            ultimoRegistro = ultimoRegistro,
                            analise = gerarAnalise(ultimoRegistro, penultimoRegistro),
                            historico = historico
                        )
                    }
                } else {
                    _state.value = ComposicaoCorporalState.Error("Falha ao buscar histórico: ${response.message()}")
                }
            } catch (e: Exception) {
                _state.value = ComposicaoCorporalState.Error(e.message ?: "Erro de conexão")
            }
        }
    }

    fun onGorduraChange(valor: String) { _gorduraPercentual.value = valor }
    fun onMusculoChange(valor: String) { _musculoPercentual.value = valor }
    fun onAguaChange(valor: String) { _aguaPercentual.value = valor }
    fun onVisceralChange(valor: String) { _gorduraVisceral.value = valor }
    fun onAlturaEstimadorChange(valor: String) { _alturaEstimador.value = valor }
    fun onPescocoEstimadorChange(valor: String) { _pescocoEstimador.value = valor }
    fun onCinturaEstimadorChange(valor: String) { _cinturaEstimador.value = valor }
    fun onQuadrilEstimadorChange(valor: String) { _quadrilEstimador.value = valor }

    fun calcularEstimativaGordura(): Float? {
        val altura = _alturaEstimador.value.toFloatOrNull() ?: return null
        val pescoco = _pescocoEstimador.value.toFloatOrNull() ?: return null
        val cintura = _cinturaEstimador.value.toFloatOrNull() ?: return null
        val quadril = _quadrilEstimador.value.toFloatOrNull()
        val sexo = perfilUsuario?.sexo ?: "Masculino"

        return try {
            if (sexo == "Masculino") {
                (86.010 * log10(cintura - pescoco) - 70.041 * log10(altura) + 36.76).toFloat()
            } else {
                if (quadril == null || quadril <= 0) return null
                (163.205 * log10(cintura + quadril - pescoco) - 97.684 * log10(altura) - 78.387).toFloat()
            }
        } catch (e: Exception) {
            Log.e("ComposicaoVM", "Erro ao calcular estimativa", e)
            null
        }
    }

    fun salvarRegistroEstimado() {
        val gordura = calcularEstimativaGordura()
        if (gordura == null) {
            viewModelScope.launch {
                _state.value = ComposicaoCorporalState.Error("Valores inválidos para estimativa.")
            }
            return
        }

        val request = ComposicaoCorporalRequest(
            gorduraPercentual = gordura,
            musculoPercentual = 0f,
            aguaPercentual = 0f,
            gorduraVisceral = 0,
            estimado = true
        )
        salvarRegistro(request)
    }

    fun salvarRegistroManual() {
        val gordura = _gorduraPercentual.value.toFloatOrNull() ?: 0f
        val musculo = _musculoPercentual.value.toFloatOrNull() ?: 0f
        val agua = _aguaPercentual.value.toFloatOrNull() ?: 0f
        val visceral = _gorduraVisceral.value.toIntOrNull() ?: 0

        val request = ComposicaoCorporalRequest(
            gorduraPercentual = gordura,
            musculoPercentual = musculo,
            aguaPercentual = agua,
            gorduraVisceral = visceral,
            estimado = false
        )
        salvarRegistro(request)
    }

    private fun salvarRegistro(request: ComposicaoCorporalRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = ComposicaoCorporalState.Loading
            try {
                val response = repository.createComposicaoRecord(request)
                if (response.isSuccessful) {
                    clearFormFields()
                    loadHistory()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Falha ao salvar"
                    _state.value = ComposicaoCorporalState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _state.value = ComposicaoCorporalState.Error(e.message ?: "Falha na conexão")
            }
        }
    }

    private fun clearFormFields() {
        _gorduraPercentual.value = ""
        _musculoPercentual.value = ""
        _aguaPercentual.value = ""
        _gorduraVisceral.value = ""
        _alturaEstimador.value = ""
        _pescocoEstimador.value = ""
        _cinturaEstimador.value = ""
        _quadrilEstimador.value = ""
    }

    fun parseApiDate(dateString: String): Date {
        return try {
            apiDateFormatter.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    private fun gerarAnalise(
        registro: ComposicaoCorporalRegistro,
        registroAnterior: ComposicaoCorporalRegistro?
    ): AnaliseComposicao {
        val sexo = perfilUsuario?.sexo ?: "Masculino"
        return AnaliseComposicao(
            analiseGordura = getAnaliseGorduraCorporal(
                registro.gorduraPercentual,
                registroAnterior?.gorduraPercentual,
                sexo
            ),
            analiseMusculo = getAnaliseMusculo(
                registro.musculoPercentual,
                registroAnterior?.musculoPercentual,
                sexo
            ),
            analiseAgua = getAnaliseAgua(
                registro.aguaPercentual,
                registroAnterior?.aguaPercentual
            )
        )
    }

    private fun getAnaliseGorduraCorporal(gorduraAtual: Float, gorduraAnterior: Float?, sexo: String): AnaliseItem {
        if (gorduraAtual <= 0) return AnaliseItem("N/A", AnaliseStatus.BOM, "Não registrado")

        val (status, mensagemBase) = if (sexo == "Masculino") {
            when {
                gorduraAtual < 8 -> AnaliseStatus.BAIXO to "Nível de gordura muito baixo. Essencial para saúde."
                gorduraAtual <= 20 -> AnaliseStatus.OTIMO to "Excelente! Nível ideal para atletas."
                gorduraAtual <= 25 -> AnaliseStatus.BOM to "Nível saudável e sustentável."
                gorduraAtual <= 30 -> AnaliseStatus.ALERTA to "Nível acima do ideal. Fique atento."
                else -> AnaliseStatus.ALERTA to "Nível elevado. Risco aumentado para saúde."
            }
        } else {
            when {
                gorduraAtual < 15 -> AnaliseStatus.BAIXO to "Nível de gordura muito baixo. Essencial para saúde."
                gorduraAtual <= 25 -> AnaliseStatus.OTIMO to "Excelente! Nível ideal."
                gorduraAtual <= 32 -> AnaliseStatus.BOM to "Nível saudável e sustentável."
                gorduraAtual <= 38 -> AnaliseStatus.ALERTA to "Nível acima do ideal. Fique atenta."
                else -> AnaliseStatus.ALERTA to "Nível elevado. Risco aumentado para saúde."
            }
        }

        val mensagemFinal = if (gorduraAnterior != null && gorduraAnterior > 0) {
            val diferenca = gorduraAtual - gorduraAnterior
            when {
                diferenca < -0.1 -> "$mensagemBase Você reduziu ${String.format(Locale.US, "%.1f", -diferenca)}%."
                diferenca > 0.1 -> "$mensagemBase Você aumentou ${String.format(Locale.US, "%.1f", diferenca)}%."
                else -> "$mensagemBase Você se manteve estável."
            }
        } else {
            mensagemBase
        }

        return AnaliseItem(String.format(Locale.US, "%.1f%%", gorduraAtual), status, mensagemFinal)
    }

    private fun getAnaliseMusculo(musculoAtual: Float, musculoAnterior: Float?, sexo: String): AnaliseItem {
        if (musculoAtual <= 0) return AnaliseItem("N/A", AnaliseStatus.BOM, "Não registrado")

        val (status, mensagemBase) = if (sexo == "Masculino") {
            when {
                musculoAtual < 38 -> AnaliseStatus.BAIXO to "Abaixo da média. Foque em treinos de força."
                musculoAtual <= 44 -> AnaliseStatus.BOM to "Dentro da média saudável."
                else -> AnaliseStatus.OTIMO to "Excelente! Nível de massa muscular acima da média."
            }
        } else {
            when {
                musculoAtual < 28 -> AnaliseStatus.BAIXO to "Abaixo da média. Foque em treinos de força."
                musculoAtual <= 34 -> AnaliseStatus.BOM to "Dentro da média saudável."
                else -> AnaliseStatus.OTIMO to "Excelente! Nível de massa muscular acima da média."
            }
        }

        val mensagemFinal = if (musculoAnterior != null && musculoAnterior > 0) {
            val diferenca = musculoAtual - musculoAnterior
            when {
                diferenca > 0.1 -> "$mensagemBase Você ganhou ${String.format(Locale.US, "%.1f", diferenca)}%!"
                diferenca < -0.1 -> "$mensagemBase Você perdeu ${String.format(Locale.US, "%.1f", -diferenca)}%."
                else -> "$mensagemBase Você se manteve estável."
            }
        } else {
            mensagemBase
        }

        return AnaliseItem(String.format(Locale.US, "%.1f%%", musculoAtual), status, mensagemFinal)
    }

    private fun getAnaliseAgua(aguaAtual: Float, aguaAnterior: Float?): AnaliseItem {
        if (aguaAtual <= 0) return AnaliseItem("N/A", AnaliseStatus.BOM, "Não registrado")

        val (status, mensagemBase) = when {
            aguaAtual < 45 -> AnaliseStatus.BAIXO to "Nível de hidratação baixo. Beba mais água."
            aguaAtual <= 65 -> AnaliseStatus.BOM to "Nível de hidratação ideal."
            else -> AnaliseStatus.OTIMO to "Nível de hidratação excelente."
        }

        val mensagemFinal = if (aguaAnterior != null && aguaAnterior > 0) {
            val diferenca = aguaAtual - aguaAnterior
            when {
                diferenca > 0.1 -> "$mensagemBase Seu nível subiu ${String.format(Locale.US, "%.1f", diferenca)}%."
                diferenca < -0.1 -> "$mensagemBase Seu nível caiu ${String.format(Locale.US, "%.1f", -diferenca)}%."
                else -> "$mensagemBase Você se manteve estável."
            }
        } else {
            mensagemBase
        }

        return AnaliseItem(String.format(Locale.US, "%.1f%%", aguaAtual), status, mensagemFinal)
    }
}