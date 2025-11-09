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
import com.example.lifeai_mobile.model.ImcBaseProfile
import com.example.lifeai_mobile.repository.AuthRepository
import com.example.lifeai_mobile.utils.SessionManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
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
                _state.value = DietaState.Empty
            } else {
                try {
                    val dieta = gson.fromJson(savedJson, DietaResponse::class.java)
                    _state.value = DietaState.Success(dieta)
                } catch (e: JsonSyntaxException) {
                    _state.value = DietaState.Error("Erro ao ler dieta salva. Tente gerar uma nova.")
                    sessionManager.saveDietJson(null)
                }
            }
        }
    }

    fun gerarPlanoDeDieta() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = DietaState.Generating
            try {
                val profileResponse = authRepository.getImcBaseDashboard()
                if (!profileResponse.isSuccessful || profileResponse.body().isNullOrEmpty()) {
                    _state.value = DietaState.Error("Perfil de usuário não encontrado.")
                    return@launch
                }
                val profile = profileResponse.body()!!.first()
                val prompt = construirPrompt(profile)
                val request = ChatRequest(pergunta = prompt, sessaoId = sessaoId)
                val response = authRepository.postDietaRequest(request)
                if (response.isSuccessful && response.body() != null) {
                    val dietaResponse = response.body()!!
                    val jsonString = gson.toJson(dietaResponse)
                    sessionManager.saveDietJson(jsonString)
                    _state.value = DietaState.Success(dietaResponse)

                    sendDietaProntaNotification()

                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    _state.value = DietaState.Error("Falha ao gerar dieta: $errorBody")
                }
            } catch (e: Exception) {
                _state.value = DietaState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    fun apagarPlanoEGerarNovo() {
        viewModelScope.launch {
            sessionManager.saveDietJson(null)
            gerarPlanoDeDieta()
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

        // --- CORREÇÃO DO TEXTO AQUI ---
        val builder = NotificationCompat.Builder(context, MyApplication.DIETA_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Sua dieta está pronta!")
            .setContentText("Volte para a tela de Dieta para conferir o seu novo plano.") // <-- MENSAGEM CORRIGIDA
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        // --- FIM DA CORREÇÃO ---

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }

    private fun construirPrompt(profile: ImcBaseProfile): String {
        return """
            Baseado neste perfil de usuário:
            - Idade: ${profile.idade}
            - Sexo: ${profile.sexo}
            - Peso: ${profile.peso} kg
            - Altura: ${profile.altura} cm
            - Objetivo: ${profile.objetivo}

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