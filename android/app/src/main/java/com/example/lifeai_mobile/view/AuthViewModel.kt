package com.example.lifeai_mobile.view

import SessionManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.LoginResponse
import com.example.lifeai_mobile.model.RegisterResponse
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    init {
        Log.d("INSTANCE_DEBUG", "AuthViewModel está usando SessionManager: $sessionManager")
    }
    private val _registerResponse = MutableStateFlow<RegisterResponse?>(null)
    val registerResponse: StateFlow<RegisterResponse?> = _registerResponse

    private val _loginResponse = MutableStateFlow<LoginResponse?>(null)
    val loginResponse: StateFlow<LoginResponse?> = _loginResponse

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                val response = repository.registerUser(username, email, password)
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.access?.let { token ->
                        sessionManager.saveAuthToken(token)
                    }
                    _registerResponse.value = body
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (!errorBody.isNullOrBlank()) {
                        try {
                            val jsonObj = JSONObject(errorBody)
                            if (jsonObj.has("message")) {
                                val message = jsonObj.getString("message").lowercase()
                                if (message.contains("nome de usuário") || message.contains("e-mail")) {
                                    _errorMessage.value = "E-mail e/ou nome de usuário já em uso."
                                } else {
                                    _errorMessage.value = jsonObj.getString("message")
                                }
                            } else if (jsonObj.has("password")) {
                                _errorMessage.value = "A senha fornecida é muito fraca ou comum."
                            } else {
                                _errorMessage.value = "Ocorreu um erro nos dados fornecidos."
                            }
                        } catch (e: Exception) {
                            _errorMessage.value = "Ocorreu um erro no registro."
                        }
                    } else {
                        _errorMessage.value = "Ocorreu um erro no registro."
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Falha na conexão. Verifique sua internet."
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                val response = repository.loginUser(email, password)
                if (response.isSuccessful) {
                    val body = response.body()
                    body?.access?.let { token ->
                        sessionManager.saveAuthToken(token)
                    }
                    _loginResponse.value = body
                } else {
                    _errorMessage.value = "E-mail e/ou senha incorretos."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Falha na conexão: ${e.message}"
            }
        }
    }

    fun resetRegisterState() {
        _registerResponse.value = null
    }

    fun resetLoginState() {
        _loginResponse.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearAuthToken()
        }
    }
}