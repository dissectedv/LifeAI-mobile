package com.example.lifeai_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lifeai_mobile.ui.theme.LifeAImobileTheme
import com.example.lifeai_mobile.utils.SessionManager
import com.example.lifeai_mobile.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sessionManager = SessionManager(applicationContext)

        setContent {
            LifeAImobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF10161C)
                ) {
                    val token by sessionManager.authToken.collectAsState(initial = "LOADING")
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(token) {
                        if (token != "LOADING") {
                            startDestination = if (token.isNullOrBlank()) "welcome" else "home"
                        }
                    }

                    if (startDestination != null) {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = startDestination!!,
                            enterTransition = { fadeIn(animationSpec = tween(500)) },
                            exitTransition = { fadeOut(animationSpec = tween(500)) },
                            popEnterTransition = { fadeIn(animationSpec = tween(500)) },
                            popExitTransition = { fadeOut(animationSpec = tween(500)) }
                        ) {
                            composable("welcome") { WelcomeScreen(navController) }
                            composable("createAccount") { RegisterScreen(navController) }
                            composable("disclaimer") { DisclaimerScreen(navController) }
                            composable("loginAccount") { LoginScreen(navController) }
                            composable("onboarding") { OnboardingScreen(navController) }
                            composable("home") { MainAppScreen(navController) }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}