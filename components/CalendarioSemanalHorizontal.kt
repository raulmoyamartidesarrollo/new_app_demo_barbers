package com.github.jetbrains.rssreader.androidApp.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
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
import java.util.*

@Composable
fun CalendarioSemanalHorizontal(
    citas: List<Cita>,
    horario: Map<String, HorarioDia>,
    diasVisibles: Int = 7,
    onCeldaLibreClick: (LocalDate, String) -> Unit,
    onEditarClick: (Cita) -> Unit,
    onEliminarClick: (Cita) -> Unit
) {
    var semanaOffset by remember { mutableStateOf(0) }
    val hoy = LocalDate.now(ZoneId.systemDefault())
    val fechaInicioSemana = remember(semanaOffset) {
        hoy.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            .plusWeeks(semanaOffset.toLong())
    }
    val horaAltura = 48.dp
    val diasSemana = (0 until diasVisibles)
        .map { fechaInicioSemana.plusDays(it.toLong()) }
        .filter { !it.isBefore(hoy) }

    val scrollHorizontal = rememberScrollState()
    val scrollVertical = rememberScrollState()
    val ahora = remember { LocalTime.now() }
    val seleccionCita = remember { mutableStateOf<Cita?>(null) }

    val todasLasHoras = remember(horario) {
        val tramos = sortedSetOf<LocalTime>()
        horario.values.forEach { dia ->
            listOf(dia.aperturaManana to dia.cierreManana, dia.aperturaTarde to dia.cierreTarde).forEach { (inicio, fin) ->
                if (inicio.isNotEmpty() && fin.isNotEmpty()) {
                    var horaActual = LocalTime.parse(inicio)
                    val horaFin = LocalTime.parse(fin)
                    while (!horaActual.isAfter(horaFin)) {
                        tramos.add(horaActual)
                        horaActual = horaActual.plusMinutes(30)
                    }
                }
            }
        }
        tramos.map { it.toString().substring(0, 5) }
    }

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
            Button(
                onClick = { if (semanaOffset > 0) semanaOffset-- },
                modifier = Modifier.padding(start = 16.dp),
                enabled = semanaOffset > 0
            ) { Text("<") }

            val mesActual = fechaInicioSemana.month.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercaseChar() }
            val anio = fechaInicioSemana.year
            Text(
                text = "$mesActual $anio",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { semanaOffset++ },
                modifier = Modifier.padding(end = 16.dp)
            ) { Text(">") }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(60.dp).height(48.dp))
            Row(modifier = Modifier.horizontalScroll(scrollHorizontal)) {
                diasSemana.forEach { dia ->
                    val diaNombre = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es")).replaceFirstChar { it.uppercaseChar() }
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
                            textAlign = TextAlign.Center,
                            maxLines = 2
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

            Box {
                Row(
                    modifier = Modifier.horizontalScroll(scrollHorizontal).verticalScroll(scrollVertical)
                ) {
                    diasSemana.forEach { dia ->
                        val nombreDia = dia.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercaseChar() }
                        val infoDia = horario[nombreDia]

                        val franjasValidas = mutableSetOf<String>()
                        listOf(infoDia?.aperturaManana to infoDia?.cierreManana, infoDia?.aperturaTarde to infoDia?.cierreTarde).forEach { (inicio, fin) ->
                            if (!inicio.isNullOrEmpty() && !fin.isNullOrEmpty()) {
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
                                val cita = citas.find { it.fecha.trim() == fechaDia.trim() && it.hora.trim() == hora.trim() }

                                val colorFondo = when {
                                    franjasValidas.contains(hora) -> Color(0xFFEEEEEE)
                                    else -> Color.DarkGray.copy(alpha = 0.3f)
                                }

                                val bordeRojo = cita != null
                                val indexDia = diasSemana.indexOf(dia)
                                val indexHora = todasLasHoras.indexOf(hora)

                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(horaAltura)
                                        .padding(2.dp)
                                        .background(colorFondo)
                                        .then(if (bordeRojo) Modifier.border(2.dp, Color.Red) else Modifier)
                                        .pointerInput(cita) {
                                            detectTapGestures(
                                                onTap = {
                                                    if (cita == null && franjasValidas.contains(hora) && !dia.isBefore(hoy)) {
                                                        onCeldaLibreClick(dia, hora)
                                                    }
                                                },
                                                onLongPress = {
                                                    if (cita != null) {
                                                        seleccionCita.value = cita
                                                    }
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cita != null) {
                                        Text(
                                            text = "R",
                                            fontSize = 12.sp,
                                            color = Color.Red,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (cita != null && seleccionCita.value == cita) {
                                        Box(
                                            modifier = Modifier
                                                .absoluteOffset(y = (-horaAltura))
                                                .fillMaxWidth()
                                                .background(Color.White)
                                                .border(1.dp, Color.Gray)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(4.dp),
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(onClick = {
                                                    onEditarClick(cita)
                                                    seleccionCita.value = null
                                                }) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(
                                                            painter = painterResource(id = android.R.drawable.ic_menu_edit),
                                                            contentDescription = "Editar",
                                                            tint = Color(0xFF3F51B5)
                                                        )
                                                        Text("Editar", fontSize = 10.sp, color = Color.Black)
                                                    }
                                                }
                                                IconButton(onClick = {
                                                    onEliminarClick(cita)
                                                    seleccionCita.value = null
                                                }) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(
                                                            painter = painterResource(id = android.R.drawable.ic_menu_delete),
                                                            contentDescription = "Eliminar",
                                                            tint = Color(0xFFF44336)
                                                        )
                                                        Text("Eliminar", fontSize = 10.sp, color = Color.Black)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (diasSemana.contains(hoy)) {
                    val indexDia = diasSemana.indexOf(hoy)
                    val minutosDesdeInicio = ahora.hour * 60 + ahora.minute
                    val minutosInicioDia = 10 * 60
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
}
