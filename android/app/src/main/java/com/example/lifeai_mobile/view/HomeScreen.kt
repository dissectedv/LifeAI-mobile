package com.example.lifeai_mobile.view

import androidx.compose.animation.animateContentSize
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.lifeai_mobile.model.ImcBaseProfile
import com.example.lifeai_mobile.ui.navigation.BottomNavItem
import com.example.lifeai_mobile.viewmodel.CompromissoState
import com.example.lifeai_mobile.viewmodel.ResumoState
import com.example.lifeai_mobile.viewmodel.ResumoViewModel
import java.util.*
import com.example.lifeai_mobile.viewmodel.GraficoUIState
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp

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
                    ChatGreetingCard(profile = null, navController = navController, isLoading = true)
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
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(1000, delayMillis = 100)) +
                                slideInVertically(initialOffsetY = { 50 })
                    ) {
                        ImcHistoricoCard(
                            graficoState = currentState.graficoImcState,
                            profile = currentState.profile,
                            onClick = {
                                mainNavController.navigate("imc_calculator")
                            }
                        )
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(1000, delayMillis = 200)) +
                                slideInVertically(initialOffsetY = { 50 })
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            ResumoImcCard(profile = currentState.profile)
                            ChatGreetingCard(
                                profile = currentState.profile,
                                navController = navController,
                                isLoading = false
                            )
                        }
                    }

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
                                profile = currentState.profile,
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

private fun getGreeting(): String {
    val calendar = Calendar.getInstance()
    return when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatGreetingCard(
    profile: ImcBaseProfile?,
    navController: NavHostController,
    isLoading: Boolean
) {
    val greeting = getGreeting()
    val userName = profile?.nome
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
private fun ResumoImcCard(profile: ImcBaseProfile) {
    val imc = profile.imcResultado.toFloat()
    val imcProgress = calculateIdealRangeProgress(imc)

    val progressActiveColor = when {
        imc < 18.5f -> Color(0xFF4A90E2)
        imc <= 24.9f -> Color(0xFF00C853)
        imc <= 29.9f -> Color(0xFFFDD835)
        else -> Color(0xFFFF5252)
    }

    val progressInactiveColor = Color(0xFF2E4A5C)
    val gradientOverlay = Brush.verticalGradient(
        listOf(Color(0x334A90E2), Color.Transparent)
    )
    val textColor = Color.White
    val textMutedColor = Color.White.copy(alpha = 0.7f)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
                            text = profile.classificacao?.uppercase(Locale.getDefault()) ?: "N/A",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = progressActiveColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    Text(
                        text = motivationalPhrase(imc),
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

private fun motivationalPhrase(imc: Float): String {
    return when {
        imc < 18.5f -> "Vamos fortalecer sua rotina e alcançar o equilíbrio!"
        imc <= 24.9f -> "Excelente! Continue cuidando da sua saúde."
        imc <= 29.9f -> "Você está quase lá! Continue progredindo."
        else -> "Vamos te ajudar a começar essa mudança!"
    }
}

private fun calculateIdealRangeProgress(imc: Float): Float {
    val minNormalImc = 18.5f
    val maxNormalImc = 24.9f

    if (imc < minNormalImc) {
        return 0.15f
    }

    val range = maxNormalImc - minNormalImc
    val progressInBuffer = imc - minNormalImc
    return (progressInBuffer / range).coerceIn(0f, 1f)
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
    profile: ImcBaseProfile?,
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
                        val primeira = graficoState.valores.first()
                        val ultima = graficoState.valores.last()
                        val diff = ultima - primeira

                        val isSubindo = diff > 0
                        val corIndicador = if (isSubindo) Color(0xFFFF5252) else Color(0xFF00C853)
                        val icone = if (isSubindo) Icons.Default.TrendingUp else Icons.Default.TrendingDown
                        val textoDiff = String.format(Locale.US, "%.1f", kotlin.math.abs(diff))

                        val corStatusImc = when {
                            (profile?.imcResultado ?: 0.0) < 18.5 -> Color(0xFF4A90E2)
                            (profile?.imcResultado ?: 0.0) <= 24.9 -> Color(0xFF00C853)
                            (profile?.imcResultado ?: 0.0) <= 29.9 -> Color(0xFFFDD835)
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
                                    color = corIndicador.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, corIndicador.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = icone,
                                            contentDescription = null,
                                            tint = corIndicador,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = textoDiff,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = corIndicador
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

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

@Composable
private fun getAnaliseGordura(gordura: Float, sexo: String): Pair<String, Color> {
    if (gordura <= 0) return "N/A" to Color.White.copy(alpha = 0.7f)

    return if (sexo == "Masculino") {
        when {
            gordura < 8 -> "Muito Baixo" to Color(0xFFFDD835)
            gordura <= 20 -> "Ideal" to Color(0xFF00C853)
            gordura <= 25 -> "Saudável" to Color(0xFF4A90E2)
            else -> "Elevado" to Color(0xFFFF5252)
        }
    } else {
        when {
            gordura < 15 -> "Muito Baixo" to Color(0xFFFDD835)
            gordura <= 25 -> "Ideal" to Color(0xFF00C853)
            gordura <= 32 -> "Saudável" to Color(0xFF4A90E2)
            else -> "Elevado" to Color(0xFFFF5252)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposicaoCorporalCard(
    modifier: Modifier = Modifier,
    profile: ImcBaseProfile?,
    ultimoRegistro: ComposicaoCorporalRegistro?,
    onClick: () -> Unit
) {
    val hasData = ultimoRegistro != null && profile != null && ultimoRegistro.gorduraPercentual > 0

    val (analise, corDestaque) = if (hasData) {
        getAnaliseGordura(ultimoRegistro.gorduraPercentual, profile.sexo)
    } else {
        null to Color(0xFF38BDF8)
    }

    val containerBase = Color(0xFF020617)
    val corBorda = if (hasData) corDestaque.copy(alpha = 0.4f) else Color(0xFF1E293B)

    Card(
        modifier = modifier
            .height(170.dp),
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
                    if (hasData)
                        Brush.verticalGradient(listOf(corDestaque.copy(alpha = 0.25f), containerBase))
                    else
                        Brush.verticalGradient(listOf(Color(0xFF1E293B), containerBase))
                )
        ) {
            if (hasData) {
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
                            text = String.format(Locale.US, "%.1f%%", ultimoRegistro.gorduraPercentual),
                            color = corDestaque,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = analise!!.uppercase(Locale.getDefault()),
                            color = corDestaque,
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
    val gradientOverlay = Brush.verticalGradient(
        listOf(Color(0x334A90E2), Color.Transparent)
    )

    Card(
        modifier = modifier
            .height(170.dp),
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
                    // --- ESTADO 1: TAREFA PENDENTE ---
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Topo: Título da seção e Ícone
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Próximo Foco",
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = Color(0xFFFDD835),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Meio: Título da Tarefa e Horário
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

                            // Etiqueta de Horário (Pílula)
                            Surface(
                                color = Color(0xFFFDD835).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFFDD835).copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = state.compromisso.hora_inicio.take(5),
                                    color = Color(0xFFFDD835),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Rodapé: Barra de Progresso
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val progresso = if (state.total > 0) state.concluidas.toFloat() / state.total else 0f

                            LinearProgressIndicator(
                                progress = { progresso },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF00C9A7),
                                trackColor = Color.White.copy(alpha = 0.1f),
                            )
                            Text(
                                text = "${state.concluidas}/${state.total} concluídas hoje",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
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
                    // --- ESTADO 3: VAZIO ---
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Planejar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
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
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}