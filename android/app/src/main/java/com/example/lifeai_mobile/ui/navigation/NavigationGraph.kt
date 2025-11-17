package com.example.lifeai_mobile.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.lifeai_mobile.view.*
import com.example.lifeai_mobile.viewmodel.*

@Composable
fun NavigationGraph(
    navController: NavHostController,
    mainNavController: NavController,
    authViewModel: AuthViewModel,
    resumoViewModelFactory: ResumoViewModelFactory,
    chatViewModelFactory: ChatIAViewModelFactory,
    dietaViewModelFactory: DietaViewModelFactory,
    rotinaViewModelFactory: RotinaViewModelFactory,
    bottomBarPadding: PaddingValues
) {
    // -----------------------------
    // ViewModels
    // -----------------------------
    val resumoViewModel: ResumoViewModel = viewModel(factory = resumoViewModelFactory)
    val chatViewModel: ChatIAViewModel = viewModel(factory = chatViewModelFactory)
    val dietaViewModel: DietaViewModel = viewModel(factory = dietaViewModelFactory)
    val rotinaViewModel: RotinaViewModel = viewModel(factory = rotinaViewModelFactory)

    // -----------------------------
    // NavHost
    // -----------------------------
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Inicio.route
    ) {

        // -----------------------------
        // Início
        // -----------------------------
        composable(BottomNavItem.Inicio.route) {
            LaunchedEffect(Unit) {
                resumoViewModel.atualizarResumo()
            }

            HomeScreen(
                navController = navController,
                mainNavController = mainNavController,
                resumoViewModel = resumoViewModel,
                modifier = Modifier.padding(bottom = bottomBarPadding.calculateBottomPadding())
            )
        }

        // -----------------------------
        // Saúde
        // -----------------------------
        composable(BottomNavItem.Saude.route) {
            SaudeScreen(
                mainNavController = mainNavController,
                navController = navController,
                resumoViewModel = resumoViewModel,
                modifier = Modifier.padding(bottom = bottomBarPadding.calculateBottomPadding())
            )
        }

        // -----------------------------
        // Chat IA
        // -----------------------------
        composable(BottomNavItem.ChatIA.route) {
            ChatIAScreen(
                viewModel = chatViewModel,
                bottomBarPadding = bottomBarPadding
            )
        }

        // -----------------------------
        // Usuário
        // -----------------------------
        composable(BottomNavItem.Usuario.route) {
            UsuarioScreen(
                mainNavController = mainNavController,
                authViewModel = authViewModel,
                modifier = Modifier.padding(bottom = bottomBarPadding.calculateBottomPadding())
            )
        }

        // -----------------------------
        // Dieta
        // -----------------------------
        composable("dieta_screen") {
            DietaScreen(
                navController = navController,
                viewModel = dietaViewModel,
                modifier = Modifier
            )
        }

        // -----------------------------
        // Atividade Física
        // -----------------------------
        composable(
            route = "atividade_fisica/{imc}",
            arguments = listOf(navArgument("imc") { type = NavType.FloatType })
        ) { entry ->
            val imc = entry.arguments?.getFloat("imc") ?: 0f

            AtividadeFisicaScreen(
                navController = navController,
                imc = imc,
                modifier = Modifier
            )
        }

        // -----------------------------
        // Rotina
        // -----------------------------
        composable("rotina_screen") {
            RotinaScreen(
                navController = navController,
                viewModel = rotinaViewModel,
                modifier = Modifier
            )
        }
    }
}