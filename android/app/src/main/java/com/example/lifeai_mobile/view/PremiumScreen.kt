package com.example.lifeai_mobile.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// --- Data class para os planos ---
private data class PlanFeature(
    val text: String,
    val isAvailable: Boolean
)

// --- Classe para estilizar o Card ---
private data class PlanStyle(
    val title: String,
    val subtitle: String,
    val isPremium: Boolean,
    val features: List<PlanFeature>,
    val gradient: Brush,
    val borderColor: Color
)

// --- Definição dos planos (com o estilo da HomeScreen) ---
private val freePlan = PlanStyle(
    title = "Free",
    subtitle = "Seu plano atual", // <-- MUDANÇA 1
    isPremium = false,
    gradient = Brush.verticalGradient( // <-- MUDANÇA 2
        listOf(Color(0xFF4A90E2).copy(alpha = 0.20f), Color(0xFF12212F))
    ),
    borderColor = Color(0xFF4A90E2).copy(alpha = 0.4f), // <-- MUDANÇA 3
    features = listOf(
        PlanFeature("Chat IA (3-5 mensagens/dia)", true),
        PlanFeature("Registro manual de calorias", true),
        PlanFeature("Lista fixa de exercícios", true),
        PlanFeature("Máximo de 5 compromissos/dia", true),
        PlanFeature("Banners e anúncios", true),
        PlanFeature("Planos de Dieta e Treino por IA", false),
        PlanFeature("Relatórios PDF e Insights Avançados", false)
    )
)

private val premiumPlan = PlanStyle(
    title = "Premium",
    subtitle = "R$ 9,90/mês",
    isPremium = true,
    gradient = Brush.verticalGradient( // <-- MUDANÇA 2
        listOf(Color(0xFFFFC107).copy(alpha = 0.25f), Color(0xFF1B2A3D))
    ),
    borderColor = Color(0xFFFFC107), // <-- MUDANÇA 3
    features = listOf(
        PlanFeature("Chat IA Ilimitado (modelos avançados)", true),
        PlanFeature("Planos de Dieta e Treino por IA", true),
        PlanFeature("Rotina ilimitada com lembretes", true),
        PlanFeature("Relatórios PDF e Insights Avançados", true),
        PlanFeature("Experiência 100% livre de anúncios", true),
        PlanFeature("Acesso a todas as novas funcionalidades", true)
    )
)

// --- A TELA PRINCIPAL ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PremiumScreen(navController: NavController) {
    val plans = listOf(freePlan, premiumPlan)
    val pagerState = rememberPagerState(pageCount = { plans.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Escolha seu Plano",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1A26))
            )
        },
        containerColor = Color(0xFF0D1A26)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 32.dp),
            ) { page ->
                PlanCard(
                    plan = plans[page],
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp, vertical = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp)) // Espaço no lugar dos "pontinhos"
        }
    }
}

// --- O CARD DE PLANO (Com novo estilo) ---
@Composable
private fun PlanCard(plan: PlanStyle, modifier: Modifier = Modifier) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, plan.borderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)), // Cor base escura
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // O Box aplica o Gradiente por cima da cor base
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(plan.gradient)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.Start
            ) {
                // Título e Preço
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.headlineMedium, // <-- MUDANÇA 4
                    fontWeight = FontWeight.Bold,
                    color = if (plan.isPremium) Color(0xFFFFC107) else Color.White
                )
                Text(
                    text = plan.subtitle,
                    style = MaterialTheme.typography.titleMedium, // <-- MUDANÇA 4
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.8f)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Color.White.copy(alpha = 0.2f)
                )

                // Lista de Vantagens
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    plan.features.forEach { feature ->
                        FeatureItem(
                            text = feature.text,
                            isAvailable = feature.isAvailable,
                            isPremium = plan.isPremium
                        )
                    }
                }

                // Botão/Ação
                if (plan.isPremium) {
                    Button(
                        onClick = { /* TODO: Google Play Billing */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Assine Agora", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                } else {
                    OutlinedButton(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        Text("Plano Atual", fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

// --- ITEM DA LISTA DE VANTAGENS ---
@Composable
private fun FeatureItem(text: String, isAvailable: Boolean, isPremium: Boolean) {
    val icon: ImageVector
    val iconColor: Color
    val textColor: Color

    if (isAvailable) {
        icon = Icons.Default.Check
        iconColor = if (isPremium) Color(0xFFFFC107) else MaterialTheme.colorScheme.primary
        textColor = Color.White
    } else {
        icon = Icons.Default.Close
        iconColor = Color.White.copy(alpha = 0.5f)
        textColor = Color.White.copy(alpha = 0.5f)
    }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )
    }
}