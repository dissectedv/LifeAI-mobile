package com.example.lifeai_mobile.view

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeai_mobile.model.ExerciseRepository
import com.example.lifeai_mobile.model.RetrofitInstance
import com.example.lifeai_mobile.repository.AuthRepository
import com.example.lifeai_mobile.utils.SessionManager
import com.example.lifeai_mobile.viewmodel.AtividadeFisicaViewModel
import com.example.lifeai_mobile.viewmodel.AtividadeFisicaViewModelFactory
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AtividadeFisicaScreen(
    navController: NavController,
    imc: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // --- INJEÇÃO DE DEPENDÊNCIA ---
    val sessionManager = remember { SessionManager(context) }
    val retrofitInstance = remember { RetrofitInstance(sessionManager) }
    val api = remember { retrofitInstance.api }
    val repository = remember { AuthRepository(api, sessionManager) }
    val factory = remember { AtividadeFisicaViewModelFactory(repository) }
    val viewModel: AtividadeFisicaViewModel = viewModel(factory = factory)

    // --- ESTADOS DO VIEWMODEL ---
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Novos estados do Resumo Diário
    val dailyCalories by viewModel.dailyCalories.collectAsState()
    val dailyTimeMinutes by viewModel.dailyTimeMinutes.collectAsState()

    val exerciseList = ExerciseRepository.getExercisesForImc(imc)
    val pagerState = rememberPagerState(pageCount = { exerciseList.size })

    // --- ESTADOS LOCAIS DE CONTROLE ---
    var isExercising by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) } // NOVO: Controle de Pausa
    var exerciseTimerSeconds by remember { mutableLongStateOf(0L) }
    var showFinishDialog by remember { mutableStateOf(false) }

    // --- LÓGICA DO CRONÔMETRO (COM PAUSA) ---
    LaunchedEffect(isExercising, isPaused) {
        if (isExercising && !isPaused) {
            val startTime = System.currentTimeMillis()
            var accumulatedTime = exerciseTimerSeconds

            while (isExercising && !isPaused) {
                // Usa delay simples para evitar drift complexo, reiniciando o loop
                delay(1000L)
                exerciseTimerSeconds++
            }
        }
    }

    // Feedback de Sucesso/Erro
    LaunchedEffect(saveSuccess, errorMessage) {
        if (saveSuccess) {
            Toast.makeText(context, "Treino salvo! Histórico atualizado. 🔥", Toast.LENGTH_LONG).show()
            viewModel.resetState()

            // Reseta a tela
            showFinishDialog = false
            isExercising = false
            isPaused = false
            exerciseTimerSeconds = 0
        }
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }

    val formattedTime = remember(exerciseTimerSeconds) {
        val minutes = exerciseTimerSeconds / 60
        val seconds = exerciseTimerSeconds % 60
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    Scaffold(
        containerColor = Color(0xFF0D1B2A),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isExercising) (if(isPaused) "Pausado" else "Em Treino") else "Atividade Física",
                        color = if(isPaused) Color.Yellow else Color(0xFF00C89C),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isExercising) {
                            showFinishDialog = true
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            // --- BARRA INFERIOR INTELIGENTE ---
            if (exerciseList.isNotEmpty()) {
                Surface(
                    color = Color(0xFF1B2A3D),
                    tonalElevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isExercising) {
                            // Botão Pausar/Retomar
                            Button(
                                onClick = { isPaused = !isPaused },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if(isPaused) Color(0xFF00C89C) else Color(0xFFFFA000)
                                ),
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    if(isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(if(isPaused) "Retomar" else "Pausar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }

                            // Botão Finalizar
                            Button(
                                onClick = { showFinishDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Square, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Finalizar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Botão Iniciar (Ocupa tudo)
                            Button(
                                onClick = {
                                    isExercising = true
                                    isPaused = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C89C)),
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Iniciar Exercício", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (exerciseList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum exercício encontrado.", color = Color.White)
                }
            } else {
                val currentExercise = exerciseList[pagerState.currentPage]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // --- NOVO: CARD DE RESUMO DO DIA ---
                    // Só mostra se não estiver treinando para limpar a tela
                    AnimatedVisibility(visible = !isExercising) {
                        ResumoDiarioCard(
                            kcal = dailyCalories,
                            minutes = dailyTimeMinutes
                        )
                    }

                    // Título do Exercício
                    Text(
                        text = currentExercise.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Área dinâmica: Carrossel ou Timer
                    AnimatedContentWrapper(isExercising = isExercising) { showTimer ->
                        if (showTimer) {
                            // --- UI DO TIMER (EM TREINO) ---
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(240.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B2A3D))
                                        .padding(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        progress = { 1f },
                                        modifier = Modifier.fillMaxSize(),
                                        color = if(isPaused) Color.Yellow else Color(0xFF00C89C),
                                        trackColor = Color.Transparent,
                                        strokeWidth = 8.dp
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        // Calorias em tempo real
                                        val currentBurned = remember(exerciseTimerSeconds) {
                                            ((exerciseTimerSeconds / 60.0) * currentExercise.caloriesBurnedPerMinute).toInt()
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                                            Text(
                                                text = "$currentBurned kcal",
                                                color = Color(0xFFFF9800),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }

                                        Spacer(Modifier.height(8.dp))

                                        Text(
                                            text = formattedTime,
                                            style = MaterialTheme.typography.displayMedium,
                                            color = if(isPaused) Color.Gray else Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if(isPaused) "PAUSADO" else "Tempo decorrido",
                                            color = if(isPaused) Color.Yellow else Color.Gray,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            // --- UI DO CARROSSEL ---
                            HorizontalPager(
                                state = pagerState,
                                contentPadding = PaddingValues(horizontal = 48.dp),
                                modifier = Modifier.height(250.dp),
                                pageSpacing = 16.dp
                            ) { page ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            val pageOffset = (
                                                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                                    ).coerceIn(-1f, 1f)
                                            val scale = 1f - (kotlin.math.abs(pageOffset) * 0.15f)
                                            scaleX = scale
                                            scaleY = scale
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = exerciseList[page].imageRes),
                                        contentDescription = exerciseList[page].name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1B2A3D),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentExercise.youtubeUrl))
                            context.startActivity(intent)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.OndemandVideo, contentDescription = null, tint = Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text("Ver tutorial no YouTube", color = Color.White)
                        }
                    }

                    if (!isExercising) {
                        InfoCard(title = "Ideal Para", icon = Icons.Default.Info) {
                            Text(currentExercise.idealPara, color = Color.White.copy(0.9f))
                        }
                        InfoCard(title = "Benefícios", icon = Icons.Default.StarOutline) {
                            Column {
                                currentExercise.beneficios.forEach {
                                    Text("• $it", color = Color.White.copy(0.9f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // --- DIALOG DE FINALIZAÇÃO ---
        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isLoading) showFinishDialog = false
                },
                containerColor = Color(0xFF1B2A3D),
                title = { Text("Finalizar Treino?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Você completou o exercício: ${exerciseList[pagerState.currentPage].name}",
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))

                        // Cálculo final para mostrar no Dialog
                        val currentBurned = ((exerciseTimerSeconds / 60.0) * exerciseList[pagerState.currentPage].caloriesBurnedPerMinute).toInt()

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Tempo:", color = Color.Gray)
                            Text(formattedTime, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Calorias:", color = Color.Gray)
                            Text("~$currentBurned kcal", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.finalizarExercicio(
                                exercise = exerciseList[pagerState.currentPage],
                                durationSeconds = exerciseTimerSeconds
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C89C)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Salvar Treino")
                        }
                    }
                },
                dismissButton = {
                    if (!isLoading) {
                        TextButton(onClick = { showFinishDialog = false }) {
                            Text("Continuar", color = Color.White)
                        }
                    }
                }
            )
        }
    }
}

// --- COMPOSABLE NOVO: RESUMO DO DIA ---
@Composable
fun ResumoDiarioCard(kcal: Int, minutes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coluna Kcal
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF9800))
                Text(
                    text = "$kcal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text("Kcal Hoje", fontSize = 12.sp, color = Color.Gray)
            }

            // Divisor Vertical
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            )

            // Coluna Minutos
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Schedule, null, tint = Color(0xFF00C89C))
                Text(
                    text = "$minutes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text("Min Hoje", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AnimatedContentWrapper(
    isExercising: Boolean,
    content: @Composable (Boolean) -> Unit
) {
    AnimatedVisibility(
        visible = isExercising,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        content(true)
    }
    AnimatedVisibility(
        visible = !isExercising,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        content(false)
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(8.dp))
                Icon(icon, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(20.dp))
            }
            content()
        }
    }
}