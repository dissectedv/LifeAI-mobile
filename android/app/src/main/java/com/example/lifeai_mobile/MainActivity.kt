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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lifeai_mobile.ui.theme.LifeAImobileTheme
import com.example.lifeai_mobile.view.*
import com.example.lifeai_mobile.viewmodel.AuthViewModel
import com.example.lifeai_mobile.viewmodel.AuthViewModelFactory
import com.example.lifeai_mobile.viewmodel.ChatIAViewModelFactory
import com.example.lifeai_mobile.viewmodel.ImcCalculatorViewModel
import com.example.lifeai_mobile.viewmodel.ImcCalculatorViewModelFactory
import com.example.lifeai_mobile.viewmodel.OnboardingViewModel
import com.example.lifeai_mobile.viewmodel.OnboardingViewModelFactory
import com.example.lifeai_mobile.viewmodel.ResumoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MyApplication
        val authViewModelFactory = AuthViewModelFactory(app.authRepository, app.sessionManager)
        val onboardingViewModelFactory =
            OnboardingViewModelFactory(app.authRepository, app.sessionManager)
        val resumoViewModelFactory = ResumoViewModelFactory(app.authRepository)
        val imcCalculatorViewModelFactory = ImcCalculatorViewModelFactory(app.authRepository)

        val chatViewModelFactory = ChatIAViewModelFactory(app.authRepository)

        setContent {
            LifeAImobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF10161C)
                ) {
                    val token by app.sessionManager.authToken.collectAsState(initial = "LOADING")
                    val onboardingCompleted by app.sessionManager.onboardingCompleted.collectAsState(initial = null)
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(token, onboardingCompleted) {
                        if (token != "LOADING" && onboardingCompleted != null) {
                            startDestination = when {
                                token.isNullOrBlank() -> "welcome"
                                !onboardingCompleted!! -> "disclaimer"
                                else -> "home"
                            }
                        }
                    }

                    if (startDestination != null) {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = startDestination!!,
                            enterTransition = { fadeIn(animationSpec = tween(500)) },
                            exitTransition = { fadeOut(animationSpec = tween(500)) }
                        ) {
                            composable("welcome") { WelcomeScreen(navController) }

                            composable("createAccount") {
                                val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
                                RegisterScreen(navController, authViewModel)
                            }

                            composable("disclaimer") { DisclaimerScreen(navController) }

                            composable("loginAccount") {
                                val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
                                LoginScreen(navController, authViewModel)
                            }

                            composable("onboarding") {
                                val onboardingViewModel: OnboardingViewModel = viewModel(factory = onboardingViewModelFactory)
                                OnboardingScreen(navController, onboardingViewModel)
                            }

                            // Tela principal com BottomNavigation
                            composable("home") {
                                val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
                                MainAppScreen(
                                    mainNavController = navController, // usado para logout e rotas globais
                                    authViewModel = authViewModel,
                                    resumoViewModelFactory = resumoViewModelFactory,
                                    chatViewModelFactory = chatViewModelFactory
                                )
                            }

                            composable("imc_calculator") {
                                val viewModel: ImcCalculatorViewModel = viewModel(factory = imcCalculatorViewModelFactory)
                                ImcCalculatorScreen(navController = navController, viewModel = viewModel)
                            }

                            composable("profile_edit") {
                                ProfileEditScreen(navController = navController)
                            }
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
