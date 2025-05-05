package com.github.jetbrains.rssreader.androidApp.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jetbrains.rssreader.androidApp.Cita
import com.github.jetbrains.rssreader.androidApp.HorarioDia
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarioSemanalHorizontalCliente(
    horario: Map<String, HorarioDia>,
    citas: List<Cita>,
    onCeldaLibreClick: (LocalDate, String) -> Unit
) {
    var semanaOffset by remember { mutableStateOf(0) }
    val hoy = LocalDate.now(ZoneId.systemDefault())
    val fechaInicioSemana = remember(semanaOffset) {
        hoy.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            .plusWeeks(semanaOffset.toLong())
    }
    val horaAltura = 48.dp
    val diasSemana = (0 until 7).map { fechaInicioSemana.plusDays(it.toLong()) }

    val scrollHorizontal = rememberScrollState()
    val scrollVertical = rememberScrollState()
    val ahora = remember { LocalTime.now() }

    val todasLasHoras = remember(horario) {
        val tramos = sortedSetOf<LocalTime>()
        horario.values.forEach { dia ->
            listOf(dia.aperturaManana to dia.cierreManana, dia.aperturaTarde to dia.cierreTarde).forEach { (inicio, fin) ->
                if (!inicio.isNullOrBlank() && !fin.isNullOrBlank()) {
                    var horaActual = LocalTime.parse(inicio)
                    val horaFin = LocalTime.parse(fin)
                    while (!horaActual.isAfter(horaFin)) {
                        tramos.add(horaActual)
                        horaActual = horaActual.plusMinutes(30)
                    }
                }
            }
        }
        val horas = tramos.map { it.toString().substring(0, 5) }
        Log.d("CALENDARIO", "📅 Horas generadas: $horas")
        horas
    }

    Log.d("CALENDARIO", "📅 Calendario renderizado con ${citas.size} citas")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263238))
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { if (semanaOffset > 0) semanaOffset-- },
                enabled = semanaOffset > 0
            ) { Text("<", color = Color.White) }

            val mesActual = fechaInicioSemana.month.getDisplayName(TextStyle.FULL, Locale("es"))
                .replaceFirstChar { it.uppercaseChar() }
            val anio = fechaInicioSemana.year
            Text(
                text = "$mesActual $anio",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = { semanaOffset++ }
            ) { Text(">", color = Color.White) }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(60.dp).height(48.dp))
            Row(modifier = Modifier.horizontalScroll(scrollHorizontal)) {
                diasSemana.forEach { dia ->
                    val diaNombre = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es"))
                        .replaceFirstChar { it.uppercaseChar() }
                    val fecha = dia.format(DateTimeFormatter.ofPattern("dd/MM"))
                    val esHoy = dia == hoy

                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(48.dp)
                            .background(if (esHoy) Color(0xFF4CAF50) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$diaNombre\n$fecha",
                            color = if (esHoy) Color.White else Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.verticalScroll(scrollVertical)) {
                todasLasHoras.forEach { hora ->
                    Box(
                        modifier = Modifier.width(60.dp).height(horaAltura),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(hora, color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .horizontalScroll(scrollHorizontal)
                    .verticalScroll(scrollVertical)
            ) {
                diasSemana.forEach { dia ->
                    val nombreDia = dia.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es"))
                        .replaceFirstChar { it.uppercaseChar() }
                    val infoDia = horario[nombreDia]

                    val franjasValidas = mutableSetOf<String>()
                    listOf(infoDia?.aperturaManana to infoDia?.cierreManana, infoDia?.aperturaTarde to infoDia?.cierreTarde).forEach { (inicio, fin) ->
                        if (!inicio.isNullOrBlank() && !fin.isNullOrBlank()) {
                            var horaActual = LocalTime.parse(inicio)
                            val horaFin = LocalTime.parse(fin)
                            while (!horaActual.isAfter(horaFin)) {
                                franjasValidas.add(horaActual.toString().substring(0, 5))
                                horaActual = horaActual.plusMinutes(30)
                            }
                        }
                    }

                    Column {
                        todasLasHoras.forEach { hora ->
                            val fechaDia = dia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            val cita = citas.find { it.fecha.trim() == fechaDia && it.hora.trim() == hora }

                            val colorFondo = if (franjasValidas.contains(hora)) Color(0xFFEEEEEE)
                            else Color.DarkGray.copy(alpha = 0.3f)

                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(horaAltura)
                                    .padding(2.dp)
                                    .background(colorFondo)
                                    .then(if (cita != null) Modifier.border(2.dp, Color.Red) else Modifier)
                                    .pointerInput(Unit) {
                                        detectTapGestures {
                                            if (cita == null && franjasValidas.contains(hora) && !dia.isBefore(hoy)) {
                                                onCeldaLibreClick(dia, hora)
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (cita != null) {
                                    Text("R", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Línea azul para la hora actual (solo si es hoy)
            if (diasSemana.contains(hoy)) {
                val indexDia = diasSemana.indexOf(hoy)
                val minutosDesdeInicio = ahora.hour * 60 + ahora.minute
                val minutosInicioDia = 8 * 60
                val minutosFinDia = 20 * 60
                val totalMinutos = minutosFinDia - minutosInicioDia
                val topOffsetPx = ((minutosDesdeInicio - minutosInicioDia).coerceIn(0, totalMinutos).toFloat() / totalMinutos) * (horaAltura.value * todasLasHoras.size)

                Canvas(
                    modifier = Modifier.fillMaxWidth().height((horaAltura * todasLasHoras.size))
                ) {
                    drawLine(
                        color = Color.Blue.copy(alpha = 0.8f),
                        start = Offset(x = 60.dp.toPx() + indexDia * 100.dp.toPx(), y = topOffsetPx),
                        end = Offset(x = 60.dp.toPx() + indexDia * 100.dp.toPx() + 100.dp.toPx(), y = topOffsetPx),
                        strokeWidth = 3f
                    )
                }
            }
        }
    }
}