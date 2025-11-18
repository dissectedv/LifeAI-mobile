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

        // -----------------------------
        // Application (para repos e sessionManager)
        // -----------------------------
        val app = application as MyApplication

        // -----------------------------
        // Factories
        // -----------------------------
        val authVMFactory = AuthViewModelFactory(app.authRepository, app.sessionManager)
        val onboardingVMFactory = OnboardingViewModelFactory(app.authRepository, app.sessionManager)
        val resumoVMFactory = ResumoViewModelFactory(app.authRepository)
        val imcVMFactory = ImcCalculatorViewModelFactory(app.authRepository)
        val chatVMFactory = ChatIAViewModelFactory(app.authRepository)
        val historicoVMFactory = HistoricoImcViewModelFactory(app.authRepository)
        val dietaVMFactory = DietaViewModelFactory(app, app.authRepository, app.sessionManager)
        val rotinaVMFactory = RotinaViewModelFactory(app.authRepository)
        val corpoVMFactory = ComposicaoCorporalViewModelFactory(app.authRepository)

        setContent {
            LifeAImobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF10161C)
                ) {

                    // -----------------------------
                    // Tokens / Estados do SessionManager
                    // -----------------------------
                    val token by app.sessionManager.authToken.collectAsState(initial = "LOADING")
                    val onboardingCompleted by app.sessionManager.onboardingCompleted.collectAsState(initial = null)
                    val isLoading = token == "LOADING" || onboardingCompleted == null

                    // -----------------------------
                    // Carregamento inicial
                    // -----------------------------
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                        return@Surface
                    }

                    // -----------------------------
                    // Escolhe rota inicial
                    // -----------------------------
                    val startDestination = remember(token, onboardingCompleted) {
                        when {
                            token.isNullOrBlank() -> "welcome"
                            onboardingCompleted == false -> "disclaimer"
                            else -> "home"
                        }
                    }

                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel(factory = authVMFactory)

                    // -----------------------------
                    // Regras automáticas de navegação
                    // -----------------------------
                    LaunchedEffect(token, onboardingCompleted) {
                        val current = navController.currentBackStackEntry?.destination?.route

                        if (!token.isNullOrBlank() && onboardingCompleted == true && current != "home") {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }

                        if (token.isNullOrBlank() &&
                            current !in listOf("loginAccount", "createAccount", "welcome")
                        ) {
                            navController.navigate("welcome") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    }

                    // -----------------------------
                    // NavHost global
                    // -----------------------------
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
                            val onboardingVM: OnboardingViewModel = viewModel(factory = onboardingVMFactory)
                            OnboardingScreen(navController, onboardingVM)
                        }

                        composable("home") {
                            MainAppScreen(
                                mainNavController = navController,
                                authViewModel = authViewModel,
                                resumoViewModelFactory = resumoVMFactory,
                                chatViewModelFactory = chatVMFactory,
                                dietaViewModelFactory = dietaVMFactory,
                                rotinaViewModelFactory = rotinaVMFactory
                            )
                        }

                        composable("imc_calculator") {
                            val imcVM: ImcCalculatorViewModel = viewModel(factory = imcVMFactory)
                            val historicoVM: HistoricoImcViewModel = viewModel(factory = historicoVMFactory)

                            ImcCalculatorScreen(
                                navController = navController,
                                viewModel = imcVM,
                                historicoViewModel = historicoVM
                            )
                        }

                        composable("composicao_corporal_screen") {
                            val compVM: ComposicaoCorporalViewModel = viewModel(factory = corpoVMFactory)
                            ComposicaoCorporalScreen(navController, compVM)
                        }

                        composable("rotina_screen") {
                            val rotinaVM: RotinaViewModel = viewModel(factory = rotinaVMFactory)
                            RotinaScreen(navController, rotinaVM)
                        }

                        composable("dieta_screen") {
                            val dietaVM: DietaViewModel = viewModel(factory = dietaVMFactory)
                            DietaScreen(navController, dietaVM)
                        }

                        composable(
                            route = "atividade_fisica/{imc}",
                            arguments = listOf(navArgument("imc") { type = NavType.FloatType })
                        ) { entry ->
                            val imcValue = entry.arguments?.getFloat("imc") ?: 0f
                            AtividadeFisicaScreen(navController, imcValue)
                        }

                        composable("premium") {
                            PremiumScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}