package com.example.lifeai_mobile.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.lifeai_mobile.model.ComposicaoCorporalRegistro
import com.example.lifeai_mobile.model.ImcRegistro
import com.example.lifeai_mobile.model.PerfilResponse
import com.example.lifeai_mobile.ui.navigation.BottomNavItem
import com.example.lifeai_mobile.utils.HealthUiUtils
import com.example.lifeai_mobile.viewmodel.CompromissoState
import com.example.lifeai_mobile.viewmodel.GraficoUIState
import com.example.lifeai_mobile.viewmodel.ResumoState
import com.example.lifeai_mobile.viewmodel.ResumoViewModel
import java.util.*
import kotlin.math.abs

@Composable
fun HomeScreen(
    navController: NavHostController,
    mainNavController: NavController,
    resumoViewModel: ResumoViewModel,
    modifier: Modifier = Modifier
) {
    val state by resumoViewModel.state.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1A26))
            .statusBarsPadding()
    ) {
        Text(
            text = "LifeAI",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            when (val currentState = state) {
                is ResumoState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    ChatGreetingCard(perfil = null, navController = navController, isLoading = true)
                }

                is ResumoState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Erro ao carregar resumo: ${currentState.message}",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                is ResumoState.Success -> {
                    // --- CARD GRÁFICO HISTÓRICO ---
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(1000, delayMillis = 100)) +
                                slideInVertically(initialOffsetY = { 50 })
                    ) {
                        ImcHistoricoCard(
                            graficoState = currentState.graficoImcState,
                            ultimoImc = currentState.ultimoImc, // Passa o último registro para cor
                            onClick = {
                                mainNavController.navigate("imc_calculator")
                            }
                        )
                    }

                    // --- RESUMO DO IMC ATUAL + CHAT ---
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(1000, delayMillis = 200)) +
                                slideInVertically(initialOffsetY = { 50 })
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Passa o último registro de IMC
                            ResumoImcCard(ultimoImc = currentState.ultimoImc)

                            // Passa o perfil para pegar o nome
                            ChatGreetingCard(
                                perfil = currentState.perfil,
                                navController = navController,
                                isLoading = false
                            )
                        }
                    }

                    // --- COMPOSIÇÃO + COMPROMISSO ---
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(1000, delayMillis = 300)) +
                                slideInVertically(initialOffsetY = { 50 })
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ComposicaoCorporalCard(
                                modifier = Modifier.weight(1f),
                                perfil = currentState.perfil, // Passa perfil para saber o sexo
                                ultimoRegistro = currentState.ultimoRegistroComposicao,
                                onClick = {
                                    mainNavController.navigate("composicao_corporal_screen")
                                }
                            )
                            ProximoCompromissoCard(
                                modifier = Modifier.weight(1f),
                                state = currentState.compromissoState,
                                onClick = {
                                    navController.navigate("rotina_screen")
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatGreetingCard(
    perfil: PerfilResponse?,
    navController: NavHostController,
    isLoading: Boolean
) {
    val greeting = HealthUiUtils.getSaudacao()
    val userName = perfil?.nome
        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        ?: "Usuário"
    val greetingMessage = "$greeting, $userName!"
    val subMessage = "Como você está se sentindo hoje?"

    val gradient = Brush.horizontalGradient(
        listOf(Color(0xFF007BFF), Color(0xFF6C63FF))
    )

    Card(
        onClick = {
            if (!isLoading) {
                navController.navigate(BottomNavItem.ChatIA.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        enabled = !isLoading
    ) {
        Box(
            modifier = Modifier
                .background(gradient, shape = RoundedCornerShape(20.dp))
                .fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        Text(
                            text = "Carregando assistente...",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = greetingMessage,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subMessage,
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "IA",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun ResumoImcCard(ultimoImc: ImcRegistro?) {
    // Se não tiver registro ainda, assume 0
    val imc = ultimoImc?.imcRes?.toFloat() ?: 0f

    val imcProgress = HealthUiUtils.calculateImcProgress(imc)
    val progressActiveColor = HealthUiUtils.getImcColor(imc)
    // Se não tiver registro, mostra "Sem dados" ou similar
    val classificacaoTexto = ultimoImc?.classificacao ?: "Sem dados"
    val motivational = HealthUiUtils.getFraseMotivacional(imc)

    val progressInactiveColor = Color(0xFF2E4A5C)
    val gradientOverlay = Brush.verticalGradient(
        listOf(Color(0x334A90E2), Color.Transparent)
    )
    val textColor = Color.White
    val textMutedColor = Color.White.copy(alpha = 0.7f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientOverlay)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Seu IMC Atual",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = progressActiveColor.copy(alpha = 0.2f),
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(
                            text = classificacaoTexto,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = progressActiveColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    Text(
                        text = motivational,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textMutedColor
                    )
                }

                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(
                        progress = imcProgress,
                        progressColor = progressActiveColor,
                        baseColor = progressInactiveColor,
                        strokeWidth = 22f
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1f", imc),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "IMC",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textMutedColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    progress: Float,
    progressColor: Color,
    baseColor: Color,
    strokeWidth: Float = 28f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val startAngle = -90f
        val sweepAngleBase = 360f
        val sweepAngleProgress = 360f * progress
        drawArc(
            color = baseColor.copy(alpha = 0.3f),
            startAngle = startAngle,
            sweepAngle = sweepAngleBase,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(progressColor.copy(alpha = 0.8f), progressColor)
            ),
            startAngle = startAngle,
            sweepAngle = sweepAngleProgress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImcHistoricoCard(
    graficoState: GraficoUIState,
    ultimoImc: ImcRegistro?,
    onClick: () -> Unit
) {
    val gradientOverlay = Brush.verticalGradient(
        listOf(Color(0x334A90E2), Color.Transparent)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(400)),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientOverlay)
        ) {
            when (graficoState) {
                is GraficoUIState.Loading -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                is GraficoUIState.Error -> {
                    EmptyChartState(
                        message = "Registre seu IMC para ver seu progresso aqui."
                    )
                }
                is GraficoUIState.Success -> {
                    if (graficoState.valores.size < 3) {
                        EmptyChartState(
                            message = "Registre mais ${3 - graficoState.valores.size} vez(es) para ver o gráfico."
                        )
                    } else {
                        val variacao = graficoState.valores.last() - graficoState.valores.first()
                        val isMelhoria = variacao < 0
                        val corVariacao = if (isMelhoria) Color(0xFF00C853) else Color(0xFFFF5252)
                        val iconeVaria = if (isMelhoria) Icons.AutoMirrored.Filled.TrendingDown else Icons.AutoMirrored.Filled.TrendingUp

                        val imcAtual = ultimoImc?.imcRes?.toFloat() ?: 0f
                        val corStatusImc = when {
                            imcAtual < 18.5 -> Color(0xFF4A90E2)
                            imcAtual <= 24.9 -> Color(0xFF00C853)
                            imcAtual <= 29.9 -> Color(0xFFFDD835)
                            else -> Color(0xFFFF5252)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Evolução do IMC",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Surface(
                                    color = corVariacao.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, corVariacao.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = iconeVaria,
                                            contentDescription = null,
                                            tint = corVariacao,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = String.format(Locale.US, "%.1f", abs(variacao)),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = corVariacao
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            ImcLineChart(
                                values = graficoState.valores,
                                labels = graficoState.labels,
                                chartColor = corStatusImc,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChartState(
    title: String = "Evolução do IMC",
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ShowChart,
            contentDescription = "Gráfico",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposicaoCorporalCard(
    modifier: Modifier = Modifier,
    perfil: PerfilResponse?,
    ultimoRegistro: ComposicaoCorporalRegistro?,
    onClick: () -> Unit
) {
    val dadosGordura = remember(perfil, ultimoRegistro) {
        if (ultimoRegistro != null && perfil != null && ultimoRegistro.gorduraPercentual > 0) {
            val (analise, cor) = HealthUiUtils.getAnaliseGordura(ultimoRegistro.gorduraPercentual, perfil.sexo)
            Triple(ultimoRegistro.gorduraPercentual, analise, cor)
        } else {
            null
        }
    }

    val containerBase = Color(0xFF020617)
    val corDestaque = dadosGordura?.third ?: Color(0xFF38BDF8)
    val corBorda = if (dadosGordura != null) corDestaque.copy(alpha = 0.4f) else Color(0xFF1E293B)

    Card(
        modifier = modifier.height(170.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerBase),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, corBorda)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (dadosGordura != null)
                        Brush.verticalGradient(listOf(corDestaque.copy(alpha = 0.25f), containerBase))
                    else
                        Brush.verticalGradient(listOf(Color(0xFF1E293B), containerBase))
                )
        ) {
            if (dadosGordura != null) {
                val (percentual, analiseTexto, corTexto) = dadosGordura

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Gordura Corporal",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column {
                        Text(
                            text = String.format(Locale.US, "%.1f%%", percentual),
                            color = corTexto,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = analiseTexto.uppercase(Locale.getDefault()),
                            color = corTexto,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Composição Corporal",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Composição",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Clique para analisar",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProximoCompromissoCard(
    modifier: Modifier = Modifier,
    state: CompromissoState,
    onClick: () -> Unit
) {
    val baseGradientColor = if (state is CompromissoState.Proximo && state.isAtrasado) {
        Color(0xFFFF5252).copy(alpha = 0.15f)
    } else {
        Color(0x334A90E2)
    }

    val gradientOverlay = Brush.verticalGradient(
        listOf(baseGradientColor, Color.Transparent)
    )

    Card(
        modifier = modifier.height(170.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientOverlay)
                .padding(16.dp)
        ) {
            when (state) {
                is CompromissoState.Proximo -> {
                    val corDestaque = if (state.isAtrasado) Color(0xFFFF5252) else Color(0xFFFDD835)
                    val tituloSecao = if (state.isAtrasado) "Pendente" else "Próximo Foco"
                    val icone = if (state.isAtrasado) Icons.Default.Warning else Icons.Default.Alarm

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = tituloSecao,
                                color = if (state.isAtrasado) corDestaque else Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Icon(
                                imageVector = icone,
                                contentDescription = null,
                                tint = corDestaque,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = state.compromisso.titulo,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                color = corDestaque.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, corDestaque.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = state.compromisso.hora_inicio.take(5),
                                    color = corDestaque,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (state.isAtrasado) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Tarefa Pendente",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFFF5252),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val progresso = if (state.total > 0) state.concluidas.toFloat() / state.total else 0f

                                LinearProgressIndicator(
                                    progress = { progresso },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = Color(0xFF00C9A7),
                                    trackColor = Color.White.copy(alpha = 0.1f),
                                )
                                Text(
                                    text = "${state.concluidas}/${state.total} concluídas hoje",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                is CompromissoState.TodosConcluidos -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Concluído",
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Tudo Feito!",
                            color = Color(0xFF00C853),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.total} tarefas finalizadas",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                is CompromissoState.NenhumAgendado -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Planejar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Minha Rotina",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toque para planejar seu dia",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}