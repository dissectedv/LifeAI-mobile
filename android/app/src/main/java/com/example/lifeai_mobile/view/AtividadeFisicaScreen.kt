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
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PlayArrow
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

    val sessionManager = remember { SessionManager(context) }
    val retrofitInstance = remember { RetrofitInstance(sessionManager) }
    val api = remember { retrofitInstance.api }
    val repository = remember { AuthRepository(api, sessionManager) }
    val factory = remember { AtividadeFisicaViewModelFactory(repository) }
    val viewModel: AtividadeFisicaViewModel = viewModel(factory = factory)

    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val exerciseList = ExerciseRepository.getExercisesForImc(imc)
    val pagerState = rememberPagerState(pageCount = { exerciseList.size })

    var isExercising by remember { mutableStateOf(false) }
    var exerciseTimerSeconds by remember { mutableLongStateOf(0L) }
    var showFinishDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isExercising) {
        if (isExercising) {
            val startTime = System.currentTimeMillis() - (exerciseTimerSeconds * 1000)
            while (isExercising) {
                exerciseTimerSeconds = (System.currentTimeMillis() - startTime) / 1000
                delay(1000L)
            }
        }
    }

    LaunchedEffect(saveSuccess, errorMessage) {
        if (saveSuccess) {
            Toast.makeText(context, "Treino salvo com sucesso! 💪", Toast.LENGTH_LONG).show()
            viewModel.resetState()

            showFinishDialog = false
            isExercising = false
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
                        if (isExercising) "Em Treino" else "Atividade Física",
                        color = Color(0xFF00C89C),
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isExercising) {
                            Button(
                                onClick = { showFinishDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Square, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Finalizar Treino", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { isExercising = true },
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
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(220.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1B2A3D))
                                        .padding(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        progress = { 1f },
                                        modifier = Modifier.fillMaxSize(),
                                        color = Color(0xFF00C89C),
                                        trackColor = Color.Transparent,
                                        strokeWidth = 8.dp
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = Color(0xFF00C89C),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            text = formattedTime,
                                            style = MaterialTheme.typography.displayMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Tempo decorrido",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        } else {
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

        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isLoading) showFinishDialog = false
                },
                containerColor = Color(0xFF1B2A3D),
                title = { Text("Parabéns!", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "Você completou o exercício: ${exerciseList[pagerState.currentPage].name}",
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tempo total: $formattedTime",
                            color = Color(0xFF00C89C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
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