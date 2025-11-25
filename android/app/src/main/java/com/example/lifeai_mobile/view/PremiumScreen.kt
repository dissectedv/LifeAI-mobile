package com.example.lifeai_mobile.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private data class PlanFeature(
    val text: String,
    val isAvailable: Boolean
)

private data class PlanStyle(
    val title: String,
    val subtitle: String,
    val buttonText: String,
    val isCurrent: Boolean,
    val isTarget: Boolean,
    val mainColor: Color,
    val features: List<PlanFeature>,
    val gradient: Brush
)

private val freePlan = PlanStyle(
    title = "Free",
    subtitle = "Comece sua jornada",
    buttonText = "Plano Atual",
    isCurrent = true,
    isTarget = false,
    mainColor = Color(0xFF90A4AE),
    gradient = Brush.verticalGradient(
        listOf(Color(0xFF90A4AE).copy(alpha = 0.15f), Color(0xFF12212F))
    ),
    features = listOf(
        PlanFeature("Chat IA (Limitado)", true),
        PlanFeature("Registro manual de dados", true),
        PlanFeature("Anúncios exibidos", true),
        PlanFeature("Planos de Dieta IA", false),
        PlanFeature("Suporte Prioritário", false)
    )
)

private val plusPlan = PlanStyle(
    title = "Plus",
    subtitle = "R$ 14,90/mês",
    buttonText = "Assinar Plus",
    isCurrent = false,
    isTarget = true,
    mainColor = Color(0xFF00C89C),
    gradient = Brush.verticalGradient(
        listOf(Color(0xFF00C89C).copy(alpha = 0.25f), Color(0xFF1B2A3D))
    ),
    features = listOf(
        PlanFeature("Chat IA Ilimitado", true),
        PlanFeature("Sem anúncios", true),
        PlanFeature("Dietas e Treinos por IA", true),
        PlanFeature("Relatórios PDF Mensais", true),
        PlanFeature("Acesso antecipado a recursos", false)
    )
)

private val ultraPlan = PlanStyle(
    title = "Ultra",
    subtitle = "R$ 49,90/mês",
    buttonText = "Virar VIP",
    isCurrent = false,
    isTarget = false,
    mainColor = Color(0xFFFFC107),
    gradient = Brush.verticalGradient(
        listOf(Color(0xFFFFC107).copy(alpha = 0.30f), Color(0xFF2D1B05))
    ),
    features = listOf(
        PlanFeature("Tudo do plano Plus", true),
        PlanFeature("Consultoria com Nutricionista", true),
        PlanFeature("Suporte Humano 24/7", true),
        PlanFeature("Badge Exclusiva no Perfil", true),
        PlanFeature("Acesso Beta a novas IAs", true)
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PremiumScreen(navController: NavController) {
    val plans = listOf(freePlan, plusPlan, ultraPlan)

    val pagerState = rememberPagerState(initialPage = 1, pageCount = { plans.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Nossos Planos", fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White)
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
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Invista na sua saúde",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Escolha a opção ideal para você",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 40.dp),
                pageSpacing = 16.dp
            ) { page ->
                val scale = if (pagerState.currentPage == page) 1f else 0.9f
                val alpha = if (pagerState.currentPage == page) 1f else 0.6f

                PlanCard(
                    plan = plans[page],
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .fillMaxHeight()
                        .padding(bottom = 24.dp)
                )
            }

            Row(
                Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(plans.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) Color(0xFF00C89C) else Color.Gray
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanCard(plan: PlanStyle, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                if (plan.isTarget) 3.dp else 1.dp,
                plan.mainColor,
                RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(plan.gradient)
        ) {
            if (plan.isTarget) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            plan.mainColor,
                            RoundedCornerShape(bottomStart = 16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "RECOMENDADO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = plan.mainColor
                )
                Text(
                    text = plan.subtitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 20.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    plan.features.forEach { feature ->
                        FeatureItem(
                            text = feature.text,
                            isAvailable = feature.isAvailable,
                            checkColor = plan.mainColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!plan.isCurrent) {
                        }
                    },
                    enabled = !plan.isCurrent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (plan.isCurrent) Color.Gray.copy(0.2f) else plan.mainColor,
                        disabledContainerColor = Color.Gray.copy(0.2f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = plan.buttonText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (plan.isTarget || plan.title == "Ultra") Color.Black else Color.White.copy(0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(text: String, isAvailable: Boolean, checkColor: Color) {
    val icon = if (isAvailable) Icons.Default.Check else Icons.Default.Close
    val tint = if (isAvailable) checkColor else Color.White.copy(alpha = 0.3f)
    val textAlpha = if (isAvailable) 1f else 0.5f

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Text(
            text = text,
            color = Color.White.copy(alpha = textAlpha),
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )
    }
}