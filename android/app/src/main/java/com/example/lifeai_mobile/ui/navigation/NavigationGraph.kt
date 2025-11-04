package com.example.lifeai_mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.lifeai_mobile.viewmodel.AuthViewModel
import com.example.lifeai_mobile.view.ChatIAScreen
import com.example.lifeai_mobile.viewmodel.ResumoViewModelFactory
import com.example.lifeai_mobile.viewmodel.ChatIAViewModel
import com.example.lifeai_mobile.viewmodel.ChatIAViewModelFactory
import com.example.lifeai_mobile.view.HomeScreen
import com.example.lifeai_mobile.view.ProfileEditScreen
import com.example.lifeai_mobile.view.SaudeScreen
import com.example.lifeai_mobile.view.UsuarioScreen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lifeai_mobile.view.AtividadeFisicaScreen
import com.example.lifeai_mobile.viewmodel.ResumoViewModel

@Composable
fun NavigationGraph(
    navController: NavHostController,
    mainNavController: NavController,
    authViewModel: AuthViewModel,
    resumoViewModelFactory: ResumoViewModelFactory,
    chatViewModelFactory: ChatIAViewModelFactory,
    bottomBarPadding: PaddingValues
) {
    val resumoViewModel: ResumoViewModel = viewModel(factory = resumoViewModelFactory)

    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Inicio.route
    ) {
        composable(BottomNavItem.Inicio.route) {
            HomeScreen(
                resumoViewModel = resumoViewModel,
                modifier = Modifier.padding(bottom = bottomBarPadding.calculateBottomPadding())
            )
        }

        composable(BottomNavItem.Saude.route) {
            SaudeScreen(
                mainNavController = mainNavController,
                navController = navController,
                resumoViewModel = resumoViewModel,
                modifier = Modifier.padding(bottom = bottomBarPadding.calculateBottomPadding())
            )
        }

        composable(BottomNavItem.ChatIA.route) {
            val chatViewModel: ChatIAViewModel = viewModel(factory = chatViewModelFactory)
            ChatIAScreen(
                viewModel = chatViewModel,
                bottomBarPadding = bottomBarPadding
            )
        }

        composable(BottomNavItem.Usuario.route) {
            UsuarioScreen(
                mainNavController = mainNavController,
                internalNavController = navController,
                authViewModel = authViewModel,
                modifier = Modifier.padding(bottom = bottomBarPadding.calculateBottomPadding())
            )
        }

        composable("profile_edit") {
            ProfileEditScreen(
                navController = navController,
                bottomBarPadding = bottomBarPadding
            )
        }

        composable(
            route = "atividade_fisica/{imc}",
            arguments = listOf(navArgument("imc") { type = NavType.FloatType })
        ) { backStackEntry ->
            val imc = backStackEntry.arguments?.getFloat("imc") ?: 0f
            AtividadeFisicaScreen(
                navController = navController,
                imc = imc,
                modifier = Modifier.padding(bottom = bottomBarPadding.calculateBottomPadding())
            )
        }
    }
}