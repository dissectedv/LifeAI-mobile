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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        val dietaViewModelFactory = DietaViewModelFactory(app, app.authRepository, app.sessionManager)
        val rotinaViewModelFactory = RotinaViewModelFactory(app.authRepository)
        val composicaoCorporalViewModelFactory = ComposicaoCorporalViewModelFactory(app.authRepository)

        setContent {
            LifeAImobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF10161C)
                ) {
                    val token by app.sessionManager.authToken.collectAsState(initial = "LOADING")
                    val onboardingCompleted by app.sessionManager.onboardingCompleted.collectAsState(initial = null)

                    val isLoading = token == "LOADING" || onboardingCompleted == null

                    if (!isLoading) {
                        val startDestination = remember(token, onboardingCompleted) {
                            when {
                                token.isNullOrBlank() -> "welcome"
                                !onboardingCompleted!! -> "disclaimer"
                                else -> "home"
                            }
                        }

                        val navController = rememberNavController()
                        val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)

                        LaunchedEffect(token, onboardingCompleted) {
                            val current = navController.currentBackStackEntry?.destination?.route
                            if (!token.isNullOrBlank() && onboardingCompleted == true && current != "home") {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            } else if (token.isNullOrBlank() && current !in listOf("loginAccount", "createAccount", "welcome")) {
                                navController.navigate("welcome") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            enterTransition = { fadeIn(animationSpec = tween(500)) },
                            exitTransition = { fadeOut(animationSpec = tween(500)) }
                        ) {
                            composable("welcome") {
                                WelcomeScreen(navController)
                            }

                            composable("createAccount") {
                                RegisterScreen(navController, authViewModel)
                            }

                            composable("disclaimer") {
                                DisclaimerScreen(navController)
                            }

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
                                    dietaViewModelFactory = dietaViewModelFactory,
                                    rotinaViewModelFactory = rotinaViewModelFactory
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

                            composable("composicao_corporal_screen") {
                                val composicaoViewModel: ComposicaoCorporalViewModel = viewModel(factory = composicaoCorporalViewModelFactory)
                                ComposicaoCorporalScreen(
                                    navController = navController,
                                    viewModel = composicaoViewModel
                                )
                            }

                            composable("rotina_screen") {
                                val rotinaViewModel: RotinaViewModel = viewModel(factory = rotinaViewModelFactory)
                                RotinaScreen(
                                    navController = navController,
                                    viewModel = rotinaViewModel
                                )
                            }

                            composable("dieta_screen") {
                                val dietaViewModel: DietaViewModel = viewModel(factory = dietaViewModelFactory)
                                DietaScreen(
                                    navController = navController,
                                    viewModel = dietaViewModel
                                )
                            }

                            composable(
                                route = "atividade_fisica/{imc}",
                                arguments = listOf(navArgument("imc") { type = NavType.FloatType })
                            ) { backStackEntry ->
                                val imcValue = backStackEntry.arguments?.getFloat("imc") ?: 0f
                                AtividadeFisicaScreen(
                                    navController = navController,
                                    imc = imcValue
                                )
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
