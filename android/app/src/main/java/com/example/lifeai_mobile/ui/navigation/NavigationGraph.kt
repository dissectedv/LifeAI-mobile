package com.example.lifeai_mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lifeai_mobile.view.AuthViewModel
import com.example.lifeai_mobile.view.ResumoViewModelFactory
import com.example.lifeai_mobile.viewmodel.*

@Composable
fun NavigationGraph(
    navController: NavHostController,
    mainNavController: NavController,
    authViewModel: AuthViewModel,
    resumoViewModelFactory: ResumoViewModelFactory
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Inicio.route
    ) {
        composable(BottomNavItem.Inicio.route) {
            HomeScreen(resumoViewModelFactory = resumoViewModelFactory)
        }
        composable(BottomNavItem.Saude.route) {
            SaudeScreen(mainNavController = mainNavController)
        }
        composable(BottomNavItem.ChatIA.route) {
            ChatIAScreen()
        }
        composable(BottomNavItem.Usuario.route) {
            UsuarioScreen(mainNavController, authViewModel)
        }
    }
}