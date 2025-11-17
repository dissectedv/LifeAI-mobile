package com.example.lifeai_mobile.view

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lifeai_mobile.ui.navigation.BottomNavigationBar
import com.example.lifeai_mobile.ui.navigation.BottomNavItem
import com.example.lifeai_mobile.ui.navigation.NavigationGraph
import com.example.lifeai_mobile.viewmodel.*

@Composable
fun MainAppScreen(
    mainNavController: NavController,
    authViewModel: AuthViewModel,
    resumoViewModelFactory: ResumoViewModelFactory,
    chatViewModelFactory: ChatIAViewModelFactory,
    dietaViewModelFactory: DietaViewModelFactory,
    rotinaViewModelFactory: RotinaViewModelFactory
) {
    // -----------------------------
    // NavController da BottomBar
    // -----------------------------
    val bottomBarNavController = rememberNavController()

    // -----------------------------
    // Rota atual (para decidir quando mostrar a BottomBar)
    // -----------------------------
    val currentRoute = bottomBarNavController.currentBackStackEntryFlow
        .collectAsState(initial = bottomBarNavController.currentBackStackEntry)
        .value?.destination?.route

    val routesWithBottomBar = listOf(
        BottomNavItem.Inicio.route,
        BottomNavItem.Saude.route,
        BottomNavItem.ChatIA.route,
        BottomNavItem.Usuario.route
    )

    val showBottomBar = currentRoute in routesWithBottomBar

    // -----------------------------
    // UI
    // -----------------------------
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0D1C27)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomBar) {
                    BottomNavigationBar(navController = bottomBarNavController)
                }
            },
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            NavigationGraph(
                navController = bottomBarNavController,
                mainNavController = mainNavController,
                authViewModel = authViewModel,
                resumoViewModelFactory = resumoViewModelFactory,
                chatViewModelFactory = chatViewModelFactory,
                dietaViewModelFactory = dietaViewModelFactory,
                rotinaViewModelFactory = rotinaViewModelFactory,
                bottomBarPadding = innerPadding
            )
        }
    }
}