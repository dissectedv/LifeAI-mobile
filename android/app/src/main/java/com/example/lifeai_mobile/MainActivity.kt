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
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lifeai_mobile.ui.theme.LifeAImobileTheme
import com.example.lifeai_mobile.view.*
import com.example.lifeai_mobile.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MyApplication
        val authViewModelFactory = AuthViewModelFactory(app.authRepository, app.sessionManager)
        val onboardingViewModelFactory = OnboardingViewModelFactory(app.authRepository, app.sessionManager)
        val resumoViewModelFactory = ResumoViewModelFactory(app.authRepository)
        val imcCalculatorViewModelFactory = ImcCalculatorViewModelFactory(app.authRepository)
        val chatViewModelFactory = ChatIAViewModelFactory(app.authRepository)
        val historicoImcViewModelFactory = HistoricoImcViewModelFactory(app.authRepository)

        // --- CORREÇÃO AQUI ---
        // 1. Passamos o 'app' (Application) para a factory
        val dietaViewModelFactory = DietaViewModelFactory(app, app.authRepository, app.sessionManager)
        // --- FIM DA CORREÇÃO ---

        setContent {
            LifeAImobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF10161C)
                ) {
                    // --- CORREÇÃO DA LÓGICA DE CARREGAMENTO ---

                    // 1. Voltamos a usar "LOADING" como o estado inicial do token.
                    val token by app.sessionManager.authToken.collectAsState(initial = "LOADING")
                    val onboardingCompleted by app.sessionManager.onboardingCompleted.collectAsState(initial = null)

                    // 2. O estado de carregamento agora é explícito
                    val isLoading = token == "LOADING" || onboardingCompleted == null
                    // --- FIM DA CORREÇÃO ---

                    if (!isLoading) {
                        // 3. O 'startDestination' agora é um 'val', é calculado UMA VEZ.
                        // (Aqui 'token' será "" ou "abc...", mas nunca "LOADING")
                        val startDestination = remember(token, onboardingCompleted) {
                            when {
                                token.isNullOrBlank() -> "welcome"
                                !onboardingCompleted!! -> "disclaimer" // sabemos que não é nulo aqui
                                else -> "home"
                            }
                        }

                        val navController = rememberNavController()
                        val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)

                        // 4. Este LaunchedEffect monitora e NAVEGA (correto)
                        LaunchedEffect(token, onboardingCompleted) {
                            val currentRoute = navController.currentBackStackEntry?.destination?.route

                            if (!token.isNullOrBlank() && onboardingCompleted == true && currentRoute != "home") {
                                // Usuário está logado e completou onboarding, vá para home
                                navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            } else if (token.isNullOrBlank() && (currentRoute != "loginAccount" && currentRoute != "createAccount" && currentRoute != "welcome")) {
                                // Usuário foi deslogado, vá para welcome
                                navController.navigate("welcome") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = startDestination, // 5. Inicia com o destino calculado
                            enterTransition = { fadeIn(animationSpec = tween(500)) },
                            exitTransition = { fadeOut(animationSpec = tween(500)) }
                        ) {
                            composable("welcome") { WelcomeScreen(navController) }
                            composable("createAccount") {
                                RegisterScreen(navController, authViewModel)
                            }
                            composable("disclaimer") { DisclaimerScreen(navController) }
                            composable("loginAccount") {
                                LoginScreen(navController, authViewModel)
                            }
                            composable("onboarding") {
                                val onboardingViewModel: OnboardingViewModel = viewModel(factory = onboardingViewModelFactory)
                                OnboardingScreen(navController, onboardingViewModel)
                            }
                            composable("home") {
                                MainAppScreen(
                                    mainNavController = navController,
                                    authViewModel = authViewModel,
                                    resumoViewModelFactory = resumoViewModelFactory,
                                    chatViewModelFactory = chatViewModelFactory,
                                    dietaViewModelFactory = dietaViewModelFactory
                                )
                            }
                            composable("imc_calculator") {
                                val imcViewModel: ImcCalculatorViewModel = viewModel(factory = imcCalculatorViewModelFactory)
                                val historicoViewModel: HistoricoImcViewModel = viewModel(factory = historicoImcViewModelFactory)
                                ImcCalculatorScreen(
                                    navController = navController,
                                    viewModel = imcViewModel,
                                    historicoViewModel = historicoViewModel
                                )
                            }
                        }
                    } else {
                        // 6. O app vai ficar aqui (corretamente)
                        // até o DataStore carregar.
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