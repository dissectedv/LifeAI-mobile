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
            HistoricoHeaderItem("Data", Modifier.weight(1f))
            HistoricoHeaderItem("% Gordura", Modifier.weight(1.1f), TextAlign.Center)
            HistoricoHeaderItem("% Músculo", Modifier.weight(1.1f), TextAlign.Center)
            HistoricoHeaderItem("% Água", Modifier.weight(1.1f), TextAlign.Center)
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
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HistoricoRowItem(formatarData(registro.dataConsulta), Modifier.weight(1f))

                        HistoricoChipItem(
                            value = registro.gorduraPercentual,
                            color = getCorGordura(registro.gorduraPercentual),
                            modifier = Modifier.weight(1.1f)
                        )

                        HistoricoChipItem(
                            value = registro.musculoPercentual,
                            color = getCorMusculo(registro.musculoPercentual),
                            modifier = Modifier.weight(1.1f)
                        )

                        HistoricoChipItem(
                            value = registro.aguaPercentual,
                            color = getCorAgua(registro.aguaPercentual),
                            modifier = Modifier.weight(1.1f)
                        )
                    }
                    HorizontalDivider(color = Color(0xFF0D1B2A))
                }
            }
        }
    }
}

private fun getStatusColor(status: AnaliseStatus): Color {
    return when (status) {
        AnaliseStatus.OTIMO -> Color(0xFF00C853)
        AnaliseStatus.BOM -> Color(0xFF4A90E2)
        AnaliseStatus.BAIXO -> Color(0xFFFDD835)
        AnaliseStatus.ALERTA -> Color(0xFFFF5252)
    }
}

private fun getCorGordura(gordura: Float, sexo: String = "Masculino"): Color {
    val status = if (sexo == "Masculino") {
        when {
            gordura < 8 -> AnaliseStatus.BAIXO
            gordura <= 20 -> AnaliseStatus.OTIMO
            gordura <= 25 -> AnaliseStatus.BOM
            else -> AnaliseStatus.ALERTA
        }
    } else {
        when {
            gordura < 15 -> AnaliseStatus.BAIXO
            gordura <= 25 -> AnaliseStatus.OTIMO
            gordura <= 32 -> AnaliseStatus.BOM
            else -> AnaliseStatus.ALERTA
        }
    }
    return getStatusColor(status)
}

private fun getCorMusculo(musculo: Float, sexo: String = "Masculino"): Color {
    val status = if (sexo == "Masculino") {
        when {
            musculo < 38 -> AnaliseStatus.BAIXO
            musculo <= 44 -> AnaliseStatus.BOM
            else -> AnaliseStatus.OTIMO
        }
    } else {
        when {
            musculo < 28 -> AnaliseStatus.BAIXO
            musculo <= 34 -> AnaliseStatus.BOM
            else -> AnaliseStatus.OTIMO
        }
    }
    return getStatusColor(status)
}

private fun getCorAgua(agua: Float): Color {
    val status = when {
        agua < 45 -> AnaliseStatus.BAIXO
        agua <= 65 -> AnaliseStatus.BOM
        else -> AnaliseStatus.OTIMO
    }
    return getStatusColor(status)
}

@Composable
private fun HistoricoChipItem(
    value: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (value <= 0) {
        HistoricoRowItem(
            text = "N/A",
            modifier = modifier,
            textAlign = TextAlign.Center
        )
        return
    }

    val text = String.format(Locale.getDefault(), "%.1f%%", value)

    Box(
        modifier = modifier.padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                textAlign = TextAlign.Center
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