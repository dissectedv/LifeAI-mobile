package com.example.lifeai_mobile.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lifeai_mobile.ui.navigation.BottomNavigationBar
import com.example.lifeai_mobile.ui.navigation.NavigationGraph
import com.example.lifeai_mobile.viewmodel.AuthViewModel
import com.example.lifeai_mobile.viewmodel.ChatIAViewModelFactory
import com.example.lifeai_mobile.viewmodel.ResumoViewModelFactory

@Composable
fun MainAppScreen(
    mainNavController: NavController,
    authViewModel: AuthViewModel,
    resumoViewModelFactory: ResumoViewModelFactory,
    chatViewModelFactory: ChatIAViewModelFactory
) {
    val bottomBarNavController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0D1C27)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavigationBar(navController = bottomBarNavController)
            },
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                NavigationGraph(
                    navController = bottomBarNavController,
                    mainNavController = mainNavController,
                    authViewModel = authViewModel,
                    resumoViewModelFactory = resumoViewModelFactory,
                    chatViewModelFactory = chatViewModelFactory,
                    bottomBarPadding = innerPadding
                )
            }
        }
    }
}
