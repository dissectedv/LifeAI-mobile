package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

// ESTADOS ATUALIZADOS para refletir a nova lógica
sealed class DietaState {
    object Loading : DietaState() // Verificando o SessionManager
    object Empty : DietaState()   // Nenhuma dieta salva (mostra botão "Gerar")
    object Generating : DietaState() // Chamando a API do Gemini (mostra "Gerando...")
    data class Success(val dieta: DietaResponse) : DietaState()
    data class Error(val message: String) : DietaState()
}

class DietaViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager // <-- ADICIONADO
) : ViewModel() {

    private val _state = MutableStateFlow<DietaState>(DietaState.Loading)
    val state: StateFlow<DietaState> = _state

    private val sessaoId = "dieta-session-v1"
    private val gson = Gson()

    init {
        // VERIFICA SE JÁ EXISTE UMA DIETA SALVA
        viewModelScope.launch {
            val savedJson = sessionManager.savedDietJson.first()
            if (savedJson == null) {
                _state.value = DietaState.Empty // Não tem dieta, mostra o botão "Gerar"
            } else {
                try {
                    // Tenta converter o JSON salvo
                    val dieta = gson.fromJson(savedJson, DietaResponse::class.java)
                    _state.value = DietaState.Success(dieta) // Já tem, mostra a dieta
                } catch (e: JsonSyntaxException) {
                    // O JSON salvo está corrompido
                    _state.value = DietaState.Error("Erro ao ler dieta salva. Tente gerar uma nova.")
                    sessionManager.saveDietJson(null) // Limpa o JSON inválido
                }
            }
        }
    }

    /**
     * Chamado pelo botão "Gerar Plano" na tela.
     */
    fun gerarPlanoDeDieta() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = DietaState.Generating // Mostra o "Gerando com IA..."
            try {
                // 1. Buscar o perfil
                val profileResponse = authRepository.getImcBaseDashboard()
                if (!profileResponse.isSuccessful || profileResponse.body().isNullOrEmpty()) {
                    _state.value = DietaState.Error("Perfil de usuário não encontrado.")
                    return@launch
                }
                val profile = profileResponse.body()!!.first()

                // 2. Construir prompt e chamar API
                val prompt = construirPrompt(profile)
                val request = ChatRequest(pergunta = prompt, sessaoId = sessaoId)
                val response = authRepository.postDietaRequest(request)

                // 3. Processar resposta
                if (response.isSuccessful && response.body() != null) {
                    val dietaResponse = response.body()!!

                    // 4. SALVAR A RESPOSTA CORRETA NO SESSION MANAGER
                    val jsonString = gson.toJson(dietaResponse)
                    sessionManager.saveDietJson(jsonString)

                    // 5. MOSTRAR A TELA DE SUCESSO
                    _state.value = DietaState.Success(dietaResponse)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    _state.value = DietaState.Error("Falha ao gerar dieta: $errorBody")
                }

            } catch (e: Exception) {
                _state.value = DietaState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }

    /**
     * Chamado pelo botão "Gerar Novo Plano" (opcional).
     */
    fun apagarPlanoEGerarNovo() {
        viewModelScope.launch {
            sessionManager.saveDietJson(null) // Limpa o plano antigo
            gerarPlanoDeDieta() // Gera um novo
        }
    }

    /**
     * Monta a instrução exata para a IA, usando os dados do perfil.
     */
    private fun construirPrompt(profile: ImcBaseProfile): String {
        // --- PROMPT ATUALIZADO ---
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