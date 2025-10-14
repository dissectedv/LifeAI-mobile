package com.example.lifeai_mobile.viewmodel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.lifeai_mobile.ui.navigation.BottomNavigationBar
import com.example.lifeai_mobile.ui.navigation.NavigationGraph
import com.example.lifeai_mobile.view.AuthViewModel

@Composable
fun MainAppScreen(mainNavController: NavController, authViewModel: AuthViewModel) {
    val bottomBarNavController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0D1C27)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavigationBar(navController = bottomBarNavController)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavigationGraph(
                    navController = bottomBarNavController,
                    mainNavController = mainNavController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}