package com.example.lifeai_mobile.viewmodel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lifeai_mobile.view.AuthViewModel

@Composable
fun UsuarioScreen(mainNavController: NavController, authViewModel: AuthViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            // 1. Chama a função centralizada no ViewModel para fazer logout
            authViewModel.logout()

            // 2. Navega de volta para a tela de boas-vindas e limpa todoo o historico
            mainNavController.navigate("welcome") {
                popUpTo(0) { // Limpa toda a backstack
                    inclusive = true
                }
            }
        }) {
            Text("Sair (Logout)")
        }
    }
}