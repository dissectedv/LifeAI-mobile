package com.example.lifeai_mobile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifeai_mobile.model.ImcRegistro
import com.example.lifeai_mobile.viewmodel.HistoricoImcViewModel
import com.example.lifeai_mobile.viewmodel.ImcHistoryState
import java.util.Locale

@Composable
fun HistoricoImcComponent(
    modifier: Modifier = Modifier,
    viewModel: HistoricoImcViewModel
) {
    val state by viewModel.state.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var registroParaDeletar by remember { mutableStateOf<ImcRegistro?>(null) }

    Column(
        modifier = modifier
            .background(Color(0xFF1B263B), shape = RoundedCornerShape(12.dp))
            .padding(4.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E8BC0).copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HistoricoHeaderItem("Data", Modifier.weight(1f))
            HistoricoHeaderItem("Peso", Modifier.weight(1.2f), TextAlign.Center)
            HistoricoHeaderItem("Altura", Modifier.weight(1.2f), TextAlign.Center)
            HistoricoHeaderItem("IMC", Modifier.weight(1f), TextAlign.Center)
            Spacer(Modifier.weight(0.5f))
        }

        HorizontalDivider(color = Color(0xFF0D1B2A), thickness = 2.dp)

        when (val currentState = state) {
            is ImcHistoryState.Loading -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2E8BC0))
                }
            }
            is ImcHistoryState.Error -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(currentState.message, color = Color.Red, fontSize = 14.sp)
                }
            }
            is ImcHistoryState.Success -> {
                if (currentState.historico.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp), contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                                tint = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Nenhum registro encontrado.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(currentState.historico, key = { it.id }) { registro ->
                            val backgroundColor = if (registro.id % 2 == 0) Color(0xFF0D1B2A) else Color(0xFF132238)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(backgroundColor)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HistoricoRowItem(formatarData(registro.dataConsulta), Modifier.weight(1f))
                                HistoricoRowItem(
                                    String.format(Locale.getDefault(), "%.1f kg", registro.peso),
                                    Modifier.weight(1.2f),
                                    TextAlign.Center
                                )
                                HistoricoRowItem(
                                    String.format(Locale.getDefault(), "%.2f m", registro.altura),
                                    Modifier.weight(1.2f),
                                    TextAlign.Center
                                )

                                HistoricoImcChipItem(
                                    imcValue = registro.imcRes,
                                    modifier = Modifier.weight(1f)
                                )

                                RegistroOpcoesMenu(
                                    modifier = Modifier.weight(0.5f),
                                    onExcluirClick = {
                                        registroParaDeletar = registro
                                        showDialog = true
                                    }
                                )
                            }
                            HorizontalDivider(color = Color(0xFF0D1B2A))
                        }
                    }
                }
            }
        }
    }

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

@Composable
private fun getCorDoImc(imc: Double): Color {
    return when {
        imc < 18.5 -> Color(0xFF42A5F5)
        imc < 25 -> Color(0xFF66BB6A)
        imc < 30 -> Color(0xFFFFB300)
        else -> Color(0xFFE53935)
    }
}

@Composable
private fun HistoricoImcChipItem(
    imcValue: Double,
    modifier: Modifier = Modifier
) {
    val statusColor = getCorDoImc(imcValue)
    val imcText = String.format(Locale.getDefault(), "%.1f", imcValue)

    Box(
        modifier = modifier.padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = statusColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = imcText,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RegistroOpcoesMenu(
    modifier: Modifier = Modifier,
    onExcluirClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.CenterEnd
    ) {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Opções",
                tint = Color.White.copy(alpha = 0.7f)
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.background(Color(0xFF1B2A3D))
        ) {
            DropdownMenuItem(
                text = { Text("Excluir", color = Color.Red.copy(alpha = 0.9f)) },
                onClick = {
                    onExcluirClick()
                    menuExpanded = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = Color.Red.copy(alpha = 0.9f)
                    )
                }
            )
        }
    }
}

@Composable
private fun HistoricoHeaderItem(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 4.dp),
        fontWeight = FontWeight.Bold,
        color = Color(0xFF2E8BC0),
        textAlign = textAlign,
        fontSize = 14.sp
    )
}

@Composable
private fun HistoricoRowItem(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 4.dp),
        color = Color.White,
        textAlign = textAlign,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1
    )
}

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
        containerColor = Color(0xFF1B263B),
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
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
            ) {
                Text("Cancelar", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

@Composable
private fun formatarData(dataApi: String): String {
    return try {
        val partes = dataApi.split("-")
        "${partes[2]}/${partes[1]}"
    } catch (_: Exception) {
        dataApi
    }
}