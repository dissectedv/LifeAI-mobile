package com.example.lifeai_mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lifeai_mobile.view.AuthViewModel
import com.example.lifeai_mobile.viewmodel.ChatIAScreen
import com.example.lifeai_mobile.view.ResumoViewModelFactory
import com.example.lifeai_mobile.viewmodel.ChatIAViewModel
import com.example.lifeai_mobile.viewmodel.ChatIAViewModelFactory
import com.example.lifeai_mobile.viewmodel.HomeScreen
import com.example.lifeai_mobile.viewmodel.ProfileEditScreen
import com.example.lifeai_mobile.viewmodel.SaudeScreen
import com.example.lifeai_mobile.viewmodel.UsuarioScreen

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
            val viewModel: ChatIAViewModel = viewModel(factory = ChatIAViewModelFactory())
            ChatIAScreen(viewModel = viewModel)
        }

        composable(BottomNavItem.Usuario.route) {
            UsuarioScreen(
                mainNavController = mainNavController,
                internalNavController = navController,
                authViewModel = authViewModel
            )
        }

        composable("profile_edit") {
            ProfileEditScreen(navController = navController)
        }
    }
}
