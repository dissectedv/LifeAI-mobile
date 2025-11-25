package com.example.lifeai_mobile.view

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.StarOutline
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
import com.example.lifeai_mobile.model.ExerciseSessionResponse
import com.example.lifeai_mobile.model.RetrofitInstance
import com.example.lifeai_mobile.repository.AuthRepository
import com.example.lifeai_mobile.utils.SessionManager
import com.example.lifeai_mobile.viewmodel.AtividadeFisicaViewModel
import com.example.lifeai_mobile.viewmodel.AtividadeFisicaViewModelFactory
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AtividadeFisicaScreen(
    navController: NavController,
    imc: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val sessionManager = remember { SessionManager(context) }
    val retrofitInstance = remember { RetrofitInstance(sessionManager) }
    val api = remember { retrofitInstance.api }
    val repository = remember { AuthRepository(api, sessionManager) }
    val factory = remember { AtividadeFisicaViewModelFactory(repository) }
    val viewModel: AtividadeFisicaViewModel = viewModel(factory = factory)

    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val dailyCalories by viewModel.dailyCalories.collectAsState()
    val dailyTimeMinutes by viewModel.dailyTimeMinutes.collectAsState()
    val todaysExercises by viewModel.todaysExercises.collectAsState()
    val dailyCalorieGoal = viewModel.dailyCalorieGoal
    val dailyTimeGoal = viewModel.dailyTimeGoal
    val exerciseList = ExerciseRepository.getExercisesForImc(imc)
    val pagerState = rememberPagerState(pageCount = { exerciseList.size })

    var isExercising by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var exerciseTimerSeconds by remember { mutableLongStateOf(0L) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var sessionGoalMinutes by remember { mutableIntStateOf(15) }

    var currentTipIndex by remember { mutableIntStateOf(0) }
    val tips = remember {
        listOf(
            "Mantenha a respiração constante",
            "Contraia o abdômen",
            "Hidrate-se entre as séries",
            "Foco no movimento",
            "Você está indo muito bem!",
            "Sinta o músculo trabalhar"
        )
    }

    LaunchedEffect(isExercising, isPaused) {
        if (isExercising && !isPaused) {
            while (isExercising && !isPaused) {
                delay(1000L)
                exerciseTimerSeconds++
                if (exerciseTimerSeconds % 30 == 0L) {
                    currentTipIndex = (currentTipIndex + 1) % tips.size
                }
            }
        }
    }

    LaunchedEffect(saveSuccess, errorMessage) {
        if (saveSuccess) {
            Toast.makeText(context, "Treino salvo!", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            showFinishDialog = false
            isExercising = false
            isPaused = false
            exerciseTimerSeconds = 0
            showHistorySheet = true
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
                        text = if (isExercising) (if (isPaused) "Pausado" else "Em Treino") else "Atividade Física",
                        color = if (isPaused) Color.Yellow else Color(0xFF00C89C),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isExercising) showFinishDialog = true else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isExercising) {
                            Button(
                                onClick = { isPaused = !isPaused },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPaused) Color(0xFF00C89C) else Color(0xFFFFA000)
                                ),
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (isPaused) "Retomar" else "Pausar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { showFinishDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Square, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Finalizar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    isExercising = true
                                    isPaused = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C89C)),
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Iniciar Exercício", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = modifier.fillMaxSize().padding(innerPadding)) {
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
                    AnimatedVisibility(visible = !isExercising) {
                        ResumoDiarioCard(
                            currentKcal = dailyCalories,
                            goalKcal = dailyCalorieGoal,
                            currentMinutes = dailyTimeMinutes,
                            goalMinutes = dailyTimeGoal,
                            onClick = { showHistorySheet = true }
                        )
                    }

                    Text(
                        text = currentExercise.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    AnimatedContentWrapper(isExercising = isExercising) { showTimer ->
                        if (showTimer) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val progress = remember(exerciseTimerSeconds, sessionGoalMinutes) {
                                    if (sessionGoalMinutes == 0) 1f else (exerciseTimerSeconds.toFloat() / (sessionGoalMinutes * 60)).coerceIn(0f, 1f)
                                }
                                val isOverTime = remember(exerciseTimerSeconds, sessionGoalMinutes) {
                                    sessionGoalMinutes > 0 && exerciseTimerSeconds >= (sessionGoalMinutes * 60)
                                }

                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(280.dp).padding(10.dp)) {
                                    CircularProgressIndicator(
                                        progress = { 1f },
                                        modifier = Modifier.fillMaxSize(),
                                        color = Color.White.copy(0.05f),
                                        trackColor = Color.Transparent,
                                        strokeWidth = 12.dp
                                    )
                                    CircularProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier.fillMaxSize(),
                                        color = if (isOverTime) Color(0xFFFFD700) else if (isPaused) Color.Yellow else Color(0xFF00C89C),
                                        trackColor = Color.Transparent,
                                        strokeWidth = 12.dp
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        val currentBurned = remember(exerciseTimerSeconds) {
                                            ((exerciseTimerSeconds / 60.0) * currentExercise.caloriesBurnedPerMinute).toInt()
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                                            Text("$currentBurned", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                        }
                                        Text(
                                            formattedTime,
                                            style = MaterialTheme.typography.displayLarge,
                                            color = if (isPaused) Color.Gray else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 56.sp
                                        )
                                        Text(
                                            text = if (isPaused) "PAUSADO"
                                            else if (isOverTime) "Meta Batida!"
                                            else if (sessionGoalMinutes == 0) "Modo Livre"
                                            else "Meta: ${sessionGoalMinutes} min",
                                            color = if (isPaused) Color.Yellow else if (isOverTime) Color(0xFFFFD700) else Color.Gray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Spacer(Modifier.height(32.dp))
                                SmartCoachPill(tip = tips[currentTipIndex])
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
                                                val scale = 1f - (kotlin.math.abs(pageOffset) * 0.15f)
                                                scaleX = scale
                                                scaleY = scale
                                            },
                                        shape = RoundedCornerShape(20.dp),
                                        elevation = CardDefaults.cardElevation(8.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = exerciseList[page].imageRes),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }

                                Spacer(Modifier.height(24.dp))
                                Text("Definir Duração", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                SegmentedGoalSelector(
                                    selectedOption = sessionGoalMinutes,
                                    onOptionSelected = { sessionGoalMinutes = it }
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1B2A3D),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(currentExercise.youtubeUrl)))
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.OndemandVideo, null, tint = Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text("Ver tutorial no YouTube", color = Color.White)
                        }
                    }

                    if (!isExercising) {
                        InfoCard("Ideal Para", Icons.Default.Info) {
                            Text(currentExercise.idealPara, color = Color.White.copy(0.9f))
                        }
                        InfoCard("Benefícios", Icons.Default.StarOutline) {
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

        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = { if (!isLoading) showFinishDialog = false },
                containerColor = Color(0xFF1B2A3D),
                title = { Text("Finalizar Treino?", color = Color.White) },
                text = {
                    Column {
                        Text("Você completou: ${exerciseList[pagerState.currentPage].name}", color = Color.White)
                        val currentBurned = ((exerciseTimerSeconds / 60.0) * exerciseList[pagerState.currentPage].caloriesBurnedPerMinute).toInt()
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Tempo", color = Color.Gray)
                            Text(formattedTime, color = Color.White)
                        }
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Kcal", color = Color.Gray)
                            Text("~$currentBurned", color = Color(0xFFFF9800))
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.finalizarExercicio(exerciseList[pagerState.currentPage], exerciseTimerSeconds)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C89C)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                        else Text("Salvar")
                    }
                },
                dismissButton = {
                    if (!isLoading) TextButton(onClick = { showFinishDialog = false }) {
                        Text("Voltar", color = Color.White)
                    }
                }
            )
        }

        if (showHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF1B2A3D)
            ) {
                Column(Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                    Text(
                        "Histórico de Hoje",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    if (todaysExercises.isEmpty()) {
                        Text("Nenhum exercício hoje. Vamos começar?", color = Color.Gray)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(todaysExercises) { ExerciseHistoryItem(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartCoachPill(tip: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.08f),
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Spa, null, tint = Color(0xFF00C89C), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            AnimatedContent(
                targetState = tip,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(600))
                },
                label = "CoachText"
            ) { targetTip ->
                Text(targetTip, color = Color.White.copy(0.9f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SegmentedGoalSelector(selectedOption: Int, onOptionSelected: (Int) -> Unit) {
    val options = listOf(15, 30, 45, 0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(0xFF1B2A3D), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            val isSelected = selectedOption == option
            val label = if (option == 0) "Livre" else "${option}m"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color(0xFF00C89C) else Color.Transparent)
                    .clickable { onOptionSelected(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ResumoDiarioCard(
    currentKcal: Int,
    goalKcal: Int,
    currentMinutes: Int,
    goalMinutes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$currentKcal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("/$goalKcal kcal", fontSize = 10.sp, color = Color.Gray)
                LinearProgressIndicator(
                    progress = { (currentKcal.toFloat() / goalKcal).coerceIn(0f, 1f) },
                    Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Color(0xFFFF9800),
                    trackColor = Color.White.copy(0.1f)
                )
            }
            Box(Modifier.height(30.dp).width(1.dp).background(Color.White.copy(0.2f)))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$currentMinutes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("/$goalMinutes min", fontSize = 10.sp, color = Color.Gray)
                LinearProgressIndicator(
                    progress = { (currentMinutes.toFloat() / goalMinutes).coerceIn(0f, 1f) },
                    Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = Color(0xFF00C89C),
                    trackColor = Color.White.copy(0.1f)
                )
            }
        }
    }
}

@Composable
fun ExerciseHistoryItem(item: ExerciseSessionResponse) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.White.copy(0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).background(Color(0xFF0D1B2A), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.FitnessCenter, null, tint = Color(0xFF00C89C), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.exerciseName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${item.durationSeconds / 60} min • ${item.caloriesBurned} kcal", color = Color.Gray, fontSize = 12.sp)
        }
        Text(formatHour(item.createdAt), color = Color.White.copy(0.6f), fontSize = 12.sp)
    }
}

fun formatHour(isoString: String): String {
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = isoFormat.parse(isoString.substringBefore(".").replace("Z", ""))
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date ?: return "--:--")
    } catch (e: Exception) {
        "--:--"
    }
}

@Composable
fun AnimatedContentWrapper(isExercising: Boolean, content: @Composable (Boolean) -> Unit) {
    AnimatedVisibility(
        visible = isExercising,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) { content(true) }
    AnimatedVisibility(
        visible = !isExercising,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) { content(false) }
}

@Composable
private fun InfoCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(8.dp))
                Icon(icon, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(20.dp))
            }
            content()
        }
    }
}