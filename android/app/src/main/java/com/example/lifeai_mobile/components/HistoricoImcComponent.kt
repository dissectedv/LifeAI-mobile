package com.example.lifeai_mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifeai_mobile.model.ImcRegistro
import com.example.lifeai_mobile.viewmodel.HistoricoImcViewModel

// ### 1. O Componente Principal da Tabela ###
@Composable
fun HistoricoImcComponent(
    modifier: Modifier = Modifier, // Permite que a tela pai defina o tamanho
    viewModel: HistoricoImcViewModel // Recebe o ViewModel
) {
    // Busca os dados quando o componente é carregado
    LaunchedEffect(Unit) {
        viewModel.buscarHistorico()
    }

    val registros by viewModel.registrosImc.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()

    // Estado para controlar o Dialog de confirmação
    var showDialog by remember { mutableStateOf(false) }
    var registroParaDeletar by remember { mutableStateOf<ImcRegistro?>(null) }

    // Estado de rolagem horizontal (compartilhado entre cabeçalho e linhas)
    val horizontalScrollState = rememberScrollState()

    // Container principal do componente
    Column(
        modifier = modifier
            .background(Color(0xFF1B263B), shape = RoundedCornerShape(8.dp))
    ) {

        // --- Cabeçalho da Tabela (com rolagem horizontal) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HistoricoHeaderItem("Data", 100.dp)
            HistoricoHeaderItem("Peso", 80.dp, TextAlign.Center)
            HistoricoHeaderItem("Altura", 80.dp, TextAlign.Center)
            HistoricoHeaderItem("IMC", 80.dp, TextAlign.Center)
            HistoricoHeaderItem("Status", 120.dp)
            HistoricoHeaderItem("Ação", 60.dp, TextAlign.End)
        }

        Divider(color = Color(0xFF0D1B2A), thickness = 2.dp)

        // --- Corpo da Tabela (com rolagem vertical e horizontal) ---
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2E8BC0))
            }
        } else if (registros.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum registro encontrado.", color = Color.Gray)
            }
        } else {
            // Lista com rolagem vertical
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(registros, key = { it.id }) { registro ->
                    // Linha com rolagem horizontal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(horizontalScrollState) // Estado compartilhado
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Células de dados
                        HistoricoRowItem(formatarData(registro.dataConsulta), 100.dp)
                        HistoricoRowItem(String.format("%.1f kg", registro.peso), 80.dp, TextAlign.Center)
                        HistoricoRowItem(String.format("%.2f m", registro.altura), 80.dp, TextAlign.Center)
                        HistoricoRowItem(String.format("%.1f", registro.imcRes), 80.dp, TextAlign.Center)
                        HistoricoRowItem(registro.classificacao, 120.dp)

                        // Célula do Botão de Deletar
                        Box(modifier = Modifier.width(60.dp), contentAlignment = Alignment.CenterEnd) {
                            IconButton(
                                onClick = {
                                    registroParaDeletar = registro
                                    showDialog = true
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Deletar",
                                    tint = Color.Red.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    Divider(color = Color(0xFF0D1B2A))
                }
            }
        }
    }

    // --- Popup (AlertDialog) de Confirmação ---
    if (showDialog) {
        DeleteConfirmationDialog(
            registro = registroParaDeletar,
            isDeleting = isDeleting,
            onConfirm = {
                registroParaDeletar?.id?.let {
                    viewModel.deletarRegistro(it)
                }
                showDialog = false
                registroParaDeletar = null
            },
            onDismiss = {
                showDialog = false
                registroParaDeletar = null
            }
        )
    }
}

// ### 2. Itens do Cabeçalho (usa largura fixa) ###
@Composable
private fun HistoricoHeaderItem(text: String, width: androidx.compose.ui.unit.Dp, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(end = 8.dp),
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2E8BC0),
        textAlign = textAlign,
        fontSize = 14.sp
    )
}

// ### 3. Itens da Linha (usa largura fixa) ###
@Composable
private fun HistoricoRowItem(text: String, width: androidx.compose.ui.unit.Dp, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(end = 8.dp),
        color = Color.White,
        textAlign = textAlign,
        fontSize = 14.sp
    )
}

// ### 4. O Dialog de Confirmação ###
@Composable
private fun DeleteConfirmationDialog(
    registro: ImcRegistro?,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (registro == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir Registro", color = Color.White) },
        text = {
            Text(
                "Tem certeza que deseja excluir o registro de ${formatarData(registro.dataConsulta)}?",
                color = Color.LightGray
            )
        },
        containerColor = Color(0xFF1B263B), // Cor de fundo do Dialog
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Excluir")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.White)
            }
        }
    )
}

// ### 5. (Opcional) Função helper para formatar a data ###
@Composable
private fun formatarData(dataApi: String): String {
    // dataApi está no formato "YYYY-MM-DD"
    return try {
        val partes = dataApi.split("-")
        "${partes[2]}/${partes[1]}" // Converte para "DD/MM"
    } catch (e: Exception) {
        dataApi // Retorna a data original se der erro
    }
}