package com.example.lifeai_mobile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifeai_mobile.model.ComposicaoCorporalRegistro
import com.example.lifeai_mobile.viewmodel.AnaliseStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoricoComposicaoComponent(
    modifier: Modifier = Modifier,
    registros: List<ComposicaoCorporalRegistro>
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1B263B), shape = RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E8BC0).copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HistoricoHeaderItem("Data", Modifier.weight(1.3f))
            HistoricoHeaderItem("Gordura", Modifier.weight(1f), TextAlign.Center)
            HistoricoHeaderItem("Músculo", Modifier.weight(1f), TextAlign.Center)
            HistoricoHeaderItem("Água", Modifier.weight(1f), TextAlign.Center)
        }

        HorizontalDivider(color = Color(0xFF0D1B2A), thickness = 2.dp)

        if (registros.isEmpty()) {
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
                items(registros, key = { it.id }) { registro ->
                    val backgroundColor = if (registro.id % 2 == 0) Color(0xFF0D1B2A) else Color(0xFF132238)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HistoricoRowItem(formatarData(registro.dataConsulta), Modifier.weight(1.3f))

                        HistoricoRowItem(
                            formatarPercentual(registro.gorduraPercentual),
                            Modifier.weight(1f),
                            TextAlign.Center
                        )

                        HistoricoRowItem(
                            formatarPercentual(registro.musculoPercentual),
                            Modifier.weight(1f),
                            TextAlign.Center
                        )

                        HistoricoRowItem(
                            formatarPercentual(registro.aguaPercentual),
                            Modifier.weight(1f),
                            TextAlign.Center
                        )
                    }
                    HorizontalDivider(color = Color(0xFF0D1B2A))
                }
            }
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

private fun formatarData(dataApi: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val parsedDate = parser.parse(dataApi)
        formatter.format(parsedDate ?: Date())
    } catch (_: Exception) {
        "N/A"
    }
}

private fun formatarPercentual(valor: Float): String {
    if (valor <= 0) return "N/A"
    return String.format(Locale.getDefault(), "%.1f%%", valor)
}