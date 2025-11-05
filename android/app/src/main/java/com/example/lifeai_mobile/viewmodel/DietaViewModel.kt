package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.ChatRequest
import com.example.lifeai_mobile.model.DietaResponse
import com.example.lifeai_mobile.model.ImcBaseProfile
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DietaState {
    object Idle : DietaState()
    object Loading : DietaState()
    data class Success(val dieta: DietaResponse) : DietaState()
    data class Error(val message: String) : DietaState()
}

class DietaViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<DietaState>(DietaState.Idle)
    val state: StateFlow<DietaState> = _state

    // Vamos usar um ID de sessão fixo para a dieta
    private val sessaoId = "dieta-session-v1"

    fun gerarPlanoDeDieta() {
        if (_state.value is DietaState.Loading || _state.value is DietaState.Success) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _state.value = DietaState.Loading
            try {
                // 1. Buscar o perfil do usuário
                val profileResponse = authRepository.getImcBaseDashboard()

                if (!profileResponse.isSuccessful || profileResponse.body().isNullOrEmpty()) {
                    _state.value = DietaState.Error("Perfil de usuário não encontrado.")
                    return@launch
                }

                val profile = profileResponse.body()!!.first()

                // 2. Construir o prompt
                val prompt = construirPrompt(profile)

                // 3. Criar o request
                val request = ChatRequest(pergunta = prompt, sessaoId = sessaoId)

                // 4. CHAMAR A API REAL
                val response = authRepository.postDietaRequest(request)

                // --- SIMULAÇÃO (Removida) ---
                // _state.value = DietaState.Error("Endpoint da API (postDietaRequest) ainda não implementado.\nO prompt que seria enviado é:\n$prompt")
                // --- FIM DA SIMULAÇÃO ---

                // --- CÓDIGO REAL (Habilitado) ---
                if (response.isSuccessful && response.body() != null) {
                    // Sucesso! A API retornou o JSON da DietaResponse
                    _state.value = DietaState.Success(response.body()!!)
                } else {
                    // A API falhou (o Gemini pode ter retornado texto em vez de JSON)
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    _state.value = DietaState.Error("Falha ao gerar dieta: $errorBody")
                }
                // --- FIM DO CÓDIGO REAL ---

            } catch (e: Exception) {
                _state.value = DietaState.Error(e.message ?: "Erro desconhecido")
            }
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
                    {"titulo": "Café da Manhã", "opcoes": ["Opção 1: Ovos mexidos...", "Opção 2: Iogurte..."]},
                    {"titulo": "Almoço", "opcoes": ["Opção 1: 150g de Frango...", "Opção 2: ..."]},
                    {"titulo": "Jantar", "opcoes": ["Opção 1: Sopa..."]},
                    {"titulo": "Lanches", "opcoes": ["Opção 1: Fruta...", "Opção 2: Nuts..."]}
                  ]
                }
              ]
            }
        """.trimIndent()
    }
}