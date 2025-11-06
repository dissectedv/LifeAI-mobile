package com.example.lifeai_mobile.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifeai_mobile.model.DietaResponse
import com.example.lifeai_mobile.model.PlanoDiario
import com.example.lifeai_mobile.model.Refeicao
import com.example.lifeai_mobile.viewmodel.DietaState
import com.example.lifeai_mobile.viewmodel.DietaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DietaScreen(
    navController: NavController,
    viewModel: DietaViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Plano de Dieta IA", fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    if (state is DietaState.Success) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opções", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color(0xFF1B2A3D))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Gerar Novo Plano", color = Color.White) },
                                onClick = {
                                    showMenu = false
                                    viewModel.apagarPlanoEGerarNovo()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1A26))
            )
        },
        containerColor = Color(0xFF0D1A26)
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val currentState = state) {
                is DietaState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4A90E2))
                    }
                }

                is DietaState.Generating -> {
                    GeneratingState()
                }

                is DietaState.Empty -> {
                    EmptyState(onGenerateClick = { viewModel.gerarPlanoDeDieta() })
                }

                is DietaState.Error -> {
                    ErrorState(
                        message = currentState.message,
                        onRetryClick = { viewModel.gerarPlanoDeDieta() }
                    )
                }

                is DietaState.Success -> {
                    PlanoDietaTabs(currentState.dieta)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onGenerateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color(0xFF4A90E2),
            modifier = Modifier.size(60.dp)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "Seu plano de dieta personalizado",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "Crie um plano de dieta semanal gerado por IA com base no seu perfil e objetivos.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onGenerateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9A7))
        ) {
            Text("Gerar meu Plano", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GeneratingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF4A90E2),
            modifier = Modifier.size(60.dp)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "Analisando seu perfil...",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Text(
            "Estamos gerando seu plano de dieta com IA...",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Text(
            "(Isso pode levar até 60 segundos)",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color.Red.copy(alpha = 0.8f),
            modifier = Modifier.size(50.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Erro ao Gerar Plano",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
        ) {
            Text("Tentar Novamente", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlanoDietaTabs(dieta: DietaResponse) {
    val dias = dieta.planoDiario
    val pagerState = rememberPagerState(pageCount = { dias.size })
    val coroutineScope = rememberCoroutineScope()

    Column {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF4A90E2),
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 3.dp,
                    color = Color(0xFF4A90E2)
                )
            }
        ) {
            dias.forEachIndexed { index, planoDiario ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = {
                        Text(
                            text = planoDiario.dia.take(3),
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = Color(0xFF4A90E2),
                    unselectedContentColor = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val planoDoDia = dias[pageIndex]
            DiaPlanoScreen(planoDoDia)
        }
    }
}

@Composable
fun DiaPlanoScreen(plano: PlanoDiario) {
    val gradientOverlay = Brush.verticalGradient(
        listOf(Color(0x334A90E2), Color.Transparent)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item {
            ResumoDiaCard(plano, gradientOverlay)
            Spacer(Modifier.height(24.dp))
        }

        items(plano.refeicoes) { refeicao ->
            RefeicaoCard(refeicao, gradientOverlay)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ResumoDiaCard(plano: PlanoDiario, gradientOverlay: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientOverlay)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    "Resumo de ${plano.dia}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Calorias Totais",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${plano.resumoKcal} kcal",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MacroItem("Proteínas", plano.macros.proteina, Color(0xFF4A90E2))
                    MacroItem("Carboidratos", plano.macros.carbo, Color(0xFF66BB6A))
                    MacroItem("Gorduras", plano.macros.gordura, Color(0xFFE53935))
                }
            }
        }
    }
}

@Composable
fun MacroItem(nome: String, valor: Int, cor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$valor g",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            nome,
            style = MaterialTheme.typography.bodySmall,
            color = cor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RefeicaoCard(refeicao: Refeicao, gradientOverlay: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientOverlay)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    refeicao.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A90E2)
                )

                OpcaoSubLista(
                    titulo = "Opções Acessíveis",
                    corTitulo = Color(0xFF66BB6A),
                    opcoes = refeicao.opcoesAcessiveis
                )

                OpcaoSubLista(
                    titulo = "Opções Ideais",
                    corTitulo = Color(0xFFFFC107),
                    opcoes = refeicao.opcoesIdeais
                )
            }
        }
    }
}

@Composable
private fun OpcaoSubLista(titulo: String, corTitulo: Color, opcoes: List<String>) {
    if (opcoes.isNotEmpty() && opcoes.firstOrNull()?.isNotBlank() == true) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = corTitulo,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        opcoes.forEach { opcao ->
            Row(
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    "•",
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(end = 8.dp),
                    fontSize = 16.sp
                )
                Text(
                    opcao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}