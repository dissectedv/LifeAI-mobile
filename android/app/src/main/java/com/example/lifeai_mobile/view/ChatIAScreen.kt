package com.example.lifeai_mobile.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifeai_mobile.R
import com.example.lifeai_mobile.viewmodel.ChatMessage
import com.example.lifeai_mobile.viewmodel.ChatIAViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.ParagraphStyle
import java.net.URLDecoder // <-- 1. IMPORT NECESSÁRIO
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatIAScreen(
    viewModel: ChatIAViewModel,
    bottomBarPadding: PaddingValues,
    saudacaoInicial: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val navBarBottomPx = with(density) {
        bottomBarPadding.calculateBottomPadding().toPx()
    }
    val finalPaddingDp = with(density) {
        (if (imeBottomPx > 0) imeBottomPx.toFloat() else navBarBottomPx).toDp()
    }

    // --- CORREÇÃO BUG 3 (MENSAGEM COM `+`) ---
    LaunchedEffect(saudacaoInicial) {
        if (saudacaoInicial != null) {
            // Decodifica a mensagem (Ex: "Olá+mundo" vira "Olá mundo")
            val decodedGreeting = URLDecoder.decode(saudacaoInicial, StandardCharsets.UTF_8.name())
            viewModel.setInitialGreeting(decodedGreeting)
        }
    }
    // --- FIM DA CORREÇÃO ---

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_gemini_logo),
                        contentDescription = "Powered by Gemini",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "LifeAI Chat",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )

        Box(modifier = Modifier.weight(1f)) {
            // --- CORREÇÃO BUG 2 (TELA VAZIA) ---
            // Só mostra a tela vazia se NÃO veio saudação E a lista está vazia
            val mostrarTelaVazia = saudacaoInicial == null && uiState.messages.size <= 1

            if (mostrarTelaVazia) {
                EmptyChatView(
                    onSuggestionClick = { topic ->
                        viewModel.sendSuggestion(topic)
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    reverseLayout = true
                ) {
                    items(uiState.messages.reversed()) { msg ->
                        ChatBubble(message = msg)
                    }
                }
            }
            // --- FIM DA CORREÇÃO ---
        }

        UserInputBar(
            message = uiState.inputText,
            onMessageChange = viewModel::onInputTextChange,
            onSendClick = viewModel::sendMessage
        )

        Spacer(modifier = Modifier.height(finalPaddingDp))
    }
}

@Composable
fun EmptyChatView(onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Como posso ajudar?",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            val suggestions = listOf("Plano de Exercícios", "Receitas Saudáveis", "Dicas de Meditação")
            items(suggestions) { topic ->
                SuggestionChip(
                    onClick = { onSuggestionClick(topic) },
                    label = { Text(topic, color = Color.White, fontSize = 13.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color(0xFF1B263B)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2E8BC0))
                )
            }
        }
    }
}

@Composable
fun UserInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(color = Color.Transparent) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(Color(0xFF1B263B), RoundedCornerShape(25.dp))
        ) {
            TextField(
                value = message,
                onValueChange = onMessageChange,
                placeholder = { Text("Digite sua mensagem...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            IconButton(
                onClick = onSendClick,
                enabled = message.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = if (message.isNotBlank()) Color.White else Color.Gray
                )
            }
        }
    }
}

@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    val annotatedString = buildAnnotatedString {
        val lines = text.split('\n')
        lines.forEachIndexed { index, line ->
            var processedLine = line.trim()
            val isBoldTitle = processedLine.startsWith("**") && processedLine.endsWith("**")
            val isBulletPoint = processedLine.startsWith("* ")
            val numberRegex = """^(\d+)\.\s""".toRegex()
            val numberMatch = numberRegex.find(processedLine)
            val isNumberedList = numberMatch != null
            if (isBulletPoint || isNumberedList) {
                pushStyle(ParagraphStyle(textIndent = TextIndent(firstLine = 0.sp, restLine = 12.sp)))
                if (isBulletPoint) {
                    append("\u2022 ")
                    processedLine = processedLine.substring(2)
                } else if (numberMatch != null) {
                    append(numberMatch.value)
                    processedLine = processedLine.substring(numberMatch.value.length)
                }
            } else if (isBoldTitle) {
                processedLine = processedLine.removeSurrounding("**")
            }
            val parts = processedLine.split("**")
            parts.forEachIndexed { partIndex, part ->
                if (isBoldTitle || partIndex % 2 == 1) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(part)
                    }
                } else {
                    append(part)
                }
            }
            if (index < lines.size - 1) {
                append("\n")
            }
            if (isBulletPoint || isNumberedList) {
                pop()
            }
        }
    }
    Text(
        text = annotatedString,
        color = Color.White,
        fontSize = 15.sp,
        modifier = modifier
    )
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val bubbleColor = if (message.isUser) Color(0xFF2E8BC0) else Color(0xFF1B263B)
    val horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    val contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = horizontalArrangement
    ) {
        Box(
            modifier = Modifier
                .background(bubbleColor, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 280.dp),
            contentAlignment = contentAlignment
        ) {
            MarkdownText(text = message.text)
        }
    }
}