package com.example.lifeai_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.ComposicaoCorporalRegistro
import com.example.lifeai_mobile.model.ComposicaoCorporalRequest
import com.example.lifeai_mobile.model.ImcBaseProfile
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

    private var perfilUsuario: ImcBaseProfile? = null
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
        val response = repository.getImcBaseDashboard()
        if (response.isSuccessful && !response.body().isNullOrEmpty()) {
            perfilUsuario = response.body()!!.first()
        } else {
            throw Exception("Perfil de usuário não encontrado.")
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
                        _state.value = ComposicaoCorporalState.Success(
                            ultimoRegistro = ultimoRegistro,
                            analise = gerarAnalise(ultimoRegistro),
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

    private fun gerarAnalise(registro: ComposicaoCorporalRegistro): AnaliseComposicao {
        val sexo = perfilUsuario?.sexo ?: "Masculino"
        return AnaliseComposicao(
            analiseGordura = getAnaliseGorduraCorporal(registro.gorduraPercentual, sexo),
            analiseMusculo = getAnaliseMusculo(registro.musculoPercentual, sexo),
            analiseAgua = getAnaliseAgua(registro.aguaPercentual)
        )
    }

    private fun getAnaliseGorduraCorporal(gordura: Float, sexo: String): AnaliseItem {
        if (gordura <= 0) return AnaliseItem("N/A", AnaliseStatus.BOM, "Não registrado")

        val (status, mensagem) = if (sexo == "Masculino") {
            when {
                gordura < 8 -> AnaliseStatus.BAIXO to "Nível de gordura muito baixo. Essencial para saúde."
                gordura <= 20 -> AnaliseStatus.OTIMO to "Excelente! Nível ideal para atletas."
                gordura <= 25 -> AnaliseStatus.BOM to "Nível saudável e sustentável."
                gordura <= 30 -> AnaliseStatus.ALERTA to "Nível acima do ideal. Fique atento."
                else -> AnaliseStatus.ALERTA to "Nível elevado. Risco aumentado para saúde."
            }
        } else {
            when {
                gordura < 15 -> AnaliseStatus.BAIXO to "Nível de gordura muito baixo. Essencial para saúde."
                gordura <= 25 -> AnaliseStatus.OTIMO to "Excelente! Nível ideal."
                gordura <= 32 -> AnaliseStatus.BOM to "Nível saudável e sustentável."
                gordura <= 38 -> AnaliseStatus.ALERTA to "Nível acima do ideal. Fique atenta."
                else -> AnaliseStatus.ALERTA to "Nível elevado. Risco aumentado para saúde."
            }
        }
        return AnaliseItem(String.format("%.1f%%", gordura), status, mensagem)
    }

    private fun getAnaliseMusculo(musculo: Float, sexo: String): AnaliseItem {
        if (musculo <= 0) return AnaliseItem("N/A", AnaliseStatus.BOM, "Não registrado")

        val (status, mensagem) = if (sexo == "Masculino") {
            when {
                musculo < 38 -> AnaliseStatus.BAIXO to "Abaixo da média. Foque em treinos de força."
                musculo <= 44 -> AnaliseStatus.BOM to "Dentro da média saudável."
                else -> AnaliseStatus.OTIMO to "Excelente! Nível de massa muscular acima da média."
            }
        } else {
            when {
                musculo < 28 -> AnaliseStatus.BAIXO to "Abaixo da média. Foque em treinos de força."
                musculo <= 34 -> AnaliseStatus.BOM to "Dentro da média saudável."
                else -> AnaliseStatus.OTIMO to "Excelente! Nível de massa muscular acima da média."
            }
        }
        return AnaliseItem(String.format("%.1f%%", musculo), status, mensagem)
    }

    private fun getAnaliseAgua(agua: Float): AnaliseItem {
        if (agua <= 0) return AnaliseItem("N/A", AnaliseStatus.BOM, "Não registrado")

        val (status, mensagem) = when {
            agua < 45 -> AnaliseStatus.BAIXO to "Nível de hidratação baixo. Beba mais água."
            agua <= 65 -> AnaliseStatus.BOM to "Nível de hidratação ideal."
            else -> AnaliseStatus.OTIMO to "Nível de hidratação excelente."
        }
        return AnaliseItem(String.format("%.1f%%", agua), status, mensagem)
    }
}