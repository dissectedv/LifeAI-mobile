package com.example.lifeai_mobile.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.toDynamicShader
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

@Composable
fun ImcLineChart(
    values: List<Float>,
    labels: List<String>,
    chartColor: Color = MaterialTheme.colorScheme.primary,
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

    val modelProducer = remember(values) { ChartEntryModelProducer(entries) }

    val datasetLineSpec = arrayListOf(
        com.patrykandpatrick.vico.compose.chart.line.lineSpec(
            lineColor = chartColor,
            lineBackgroundShader = Brush.verticalGradient(
                colors = listOf(
                    chartColor.copy(alpha = 0.4f),
                    chartColor.copy(alpha = 0.0f)
                )
            ).toDynamicShader()
        )
    )

    val axisLabelColor = Color.White.copy(alpha = 0.6f)

    Chart(
        chart = lineChart(
            lines = datasetLineSpec,
            axisValuesOverrider = AxisValuesOverrider.adaptiveYValues(
                yFraction = 1.05f,
                round = false
            )
        ),
        chartModelProducer = modelProducer,
        modifier = modifier.padding(8.dp),
        startAxis = rememberStartAxis(
            label = textComponent(
                color = axisLabelColor,
                textSize = 10.sp
            ),
            valueFormatter = { value, _ ->
                String.format("%.1f", value)
            }
        ),
        bottomAxis = rememberBottomAxis(
            label = textComponent(
                color = axisLabelColor,
                textSize = 10.sp
            ),
            guideline = null,
            valueFormatter = { x, _ -> labels.getOrNull(x.toInt()) ?: "" }
        )
    )
}