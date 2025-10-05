package com.example.lifeai_mobile.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.RegisterResponse
import com.example.lifeai_mobile.model.RetrofitInstance
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository): ViewModel() {

    private val _registerResponse = MutableStateFlow<RegisterResponse?>(null)
    val registerResponse: StateFlow<RegisterResponse?> = _registerResponse

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
}