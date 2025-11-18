package com.example.lifeai_mobile.view

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@Composable
fun ImcLineChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nenhum dado disponível", color = Color.White.copy(alpha = 0.6f))
        }
        return
    }

    val entries = values.mapIndexed { index, value ->
        entryOf(index.toFloat(), value)
    }

    val modelProducer = ChartEntryModelProducer(entries)

    Chart(
        chart = lineChart(
            // se quiser customizar a linha: use lineSpec na versão 1.14 via parâmetro lines
            // aqui deixamos a configuração padrão (usa tema)
        ),
        chartModelProducer = modelProducer,
        modifier = modifier.padding(8.dp),
        startAxis = rememberStartAxis(), // eixo Y
        bottomAxis = rememberBottomAxis(
            valueFormatter = { x, _ -> labels.getOrNull(x.toInt()) ?: "" }
        ) // eixo X com rótulos das datas
    )
}
