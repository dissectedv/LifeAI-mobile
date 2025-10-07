package com.example.lifeai_mobile.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.LoginResponse
import com.example.lifeai_mobile.model.RegisterResponse
import com.example.lifeai_mobile.model.RetrofitInstance
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository): ViewModel() {

    private val _registerResponse = MutableStateFlow<RegisterResponse?>(null)
    val registerResponse: StateFlow<RegisterResponse?> = _registerResponse

    private val _loginResponse = MutableStateFlow<LoginResponse?>(null)

    val loginResponse: StateFlow<LoginResponse?> = _loginResponse

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.registerUser(username, email, password)
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.access?.let { RetrofitInstance.setToken(it) }
                    _registerResponse.value = body
                } else {
                    _errorMessage.value = "Erro: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.loginUser(email, password)
                if (response.isSuccessful) {
                    val body = response.body()
                    // Salva o token de acesso para futuras requisições
                    body?.access?.let { RetrofitInstance.setToken(it) }
                    _loginResponse.value = body
                } else {
                    // Tenta ler a mensagem de erro do corpo da resposta
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = "Erro: ${response.code()} - $errorBody"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Falha na conexão: ${e.message}"
            }
        }
    }
}