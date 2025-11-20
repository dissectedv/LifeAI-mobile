package com.example.lifeai_mobile.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.MyApplication
import com.example.lifeai_mobile.R
import com.example.lifeai_mobile.model.ChatRequest
import com.example.lifeai_mobile.model.DietaResponse
import com.example.lifeai_mobile.model.PerfilResponse
import com.example.lifeai_mobile.repository.AuthRepository
import com.example.lifeai_mobile.utils.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class DietaState {
    object Loading : DietaState()
    object Empty : DietaState()
    object Generating : DietaState()
    data class Success(val dieta: DietaResponse) : DietaState()
    data class Error(val message: String) : DietaState()
}

// Classe interna simples para ajudar a montar o prompt
private data class DadosParaDieta(
    val nome: String,
    val idade: Int,
    val sexo: String,
    val objetivo: String,
    val peso: Double,
    val altura: Double
)

class DietaViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<DietaState>(DietaState.Loading)
    val state: StateFlow<DietaState> = _state

    private val sessaoId = "dieta-session-v1"
    private val gson = Gson()

    init {
        viewModelScope.launch {
            val savedJson = sessionManager.savedDietJson.first()
            if (savedJson == null) {
                // Se não tem local, tenta buscar do servidor (sem forçar nova)
                gerarPlanoDeDieta(forceNew = false)
            } else {
                try {
                    val dieta = gson.fromJson(savedJson, DietaResponse::class.java)
                    _state.value = DietaState.Success(dieta)
                } catch (e: JsonSyntaxException) {
                    _state.value = DietaState.Error("Erro ao ler dieta salva. Tentando buscar do servidor...")
                    gerarPlanoDeDieta(forceNew = false)
                }
            }
        }
    }

    // ADICIONADO PARÂMETRO forceNew
    fun gerarPlanoDeDieta(forceNew: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = DietaState.Generating
            try {
                // 1. Buscar dados de Perfil e IMC em paralelo
                val profileJob = async { authRepository.getProfileData() }
                val imcJob = async { authRepository.getHistoricoImc() }

                val profileResponse = profileJob.await()
                val imcResponse = imcJob.await()

                if (!profileResponse.isSuccessful || profileResponse.body() == null) {
                    _state.value = DietaState.Error("Perfil de usuário não encontrado.")
                    return@launch
                }

                val perfil = profileResponse.body()!!

                // Pegar peso e altura do último registro
                var pesoAtual = 0.0
                var alturaAtual = 0.0

                if (imcResponse.isSuccessful && !imcResponse.body().isNullOrEmpty()) {
                    val ultimoRegistro = imcResponse.body()!!.last() // Assume ordem cronológica

                    // Correção de valores se necessário (igual ao ResumoViewModel)
                    val imcReg = if (ultimoRegistro.imcRes < 5.0) {
                        ultimoRegistro.copy(imcRes = ultimoRegistro.imcRes * 10000)
                    } else ultimoRegistro

                    pesoAtual = imcReg.peso
                    alturaAtual = imcReg.altura
                }

                // Monta o objeto consolidado para o prompt
                val dadosParaPrompt = DadosParaDieta(
                    nome = perfil.nome,
                    idade = perfil.idade,
                    sexo = perfil.sexo,
                    objetivo = perfil.objetivo,
                    peso = pesoAtual,
                    altura = alturaAtual
                )

                val prompt = construirPrompt(dadosParaPrompt)

                // CORREÇÃO AQUI: Passando forceNew para o ChatRequest
                val request = ChatRequest(
                    pergunta = prompt,
                    sessaoId = sessaoId,
                    forceNew = forceNew
                )

                val response = authRepository.postDietaRequest(request)

                if (response.isSuccessful && response.body() != null) {
                    val dietaResponse = response.body()!!

                    // Salva localmente também para acesso offline
                    val jsonParaSalvar = gson.toJson(dietaResponse)
                    sessionManager.saveDietJson(jsonParaSalvar)

                    _state.value = DietaState.Success(dietaResponse)

                    // Só manda notificação se realmente gerou uma nova (status 201) ou se forçado
                    if (response.code() == 201 || forceNew) {
                        sendDietaProntaNotification()
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    _state.value = DietaState.Error("Falha ao obter dieta: $errorBody")
                }
            } catch (e: Exception) {
                _state.value = DietaState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun apagarPlanoEGerarNovo() {
        viewModelScope.launch {
            sessionManager.saveDietJson(null)
            // CORREÇÃO AQUI: Agora passamos true para forçar o backend a gerar uma nova
            gerarPlanoDeDieta(forceNew = true)
        }
    }

    private fun sendDietaProntaNotification() {
        val context = getApplication<Application>().applicationContext

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("DietaViewModel", "Permissão de notificação NÃO concedida.")
            return
        }
        val builder = NotificationCompat.Builder(context, MyApplication.DIETA_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Sua dieta está pronta!")
            .setContentText("Volte para a tela de Dieta para conferir o seu novo plano.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }

    private fun construirPrompt(dados: DadosParaDieta): String {
        return """
            Baseado neste perfil de usuário:
            - Idade: ${dados.idade}
            - Sexo: ${dados.sexo}
            - Peso: ${dados.peso} kg
            - Altura: ${dados.altura} cm
            - Objetivo: ${dados.objetivo}

            Gere um plano de dieta de 7 dias (Segunda a Domingo).
            Responda APENAS em formato JSON, seguindo esta estrutura 
            (use snake_case nas chaves JSON):
            
            {
              "plano_diario": [
                {
                  "dia": "Segunda",
                  "resumo_kcal": 2100,
                  "macros": {"proteina_g": 150, "carbo_g": 180, "gordura_g": 70},
                  "refeicoes": [
                    {
                      "titulo": "Café da Manhã", 
                      "opcoes_acessiveis": ["Exemplo: 2 ovos mexidos e 1 banana."],
                      "opcoes_ideais": ["Exemplo: 30g de Whey Protein e 50g de aveia."]
                    },
                    {
                      "titulo": "Almoço", 
                      "opcoes_acessiveis": ["Exemplo: 150g de frango e 100g de arroz."],
                      "opcoes_ideais": ["Exemplo: 150g de salmão e 100g de quinoa."]
                    },
                    {
                      "titulo": "Jantar", 
                      "opcoes_acessiveis": ["Exemplo: Sopa de legumes."],
                      "opcoes_ideais": ["Exemplo: Omelete com queijo cottage."]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
    }
}