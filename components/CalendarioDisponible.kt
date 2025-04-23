package com.github.jetbrains.rssreader.androidApp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import java.time.*
import java.time.format.TextStyle
import java.util.*

@Composable
fun CalendarioDisponible(
    selected: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by rememberSaveable { mutableStateOf(selected ?: today) }

    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()
    val daysInMonth = (1..lastDayOfMonth.dayOfMonth).map { currentMonth.atDay(it) }

    val dayHeaders = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY,
    ).map {
        it.getDisplayName(TextStyle.SHORT, Locale("es", "ES"))
    }

    val offset = (firstDayOfMonth.dayOfWeek.value + 6) % 7
    val calendarDays = List(offset) { null } + daysInMonth

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Controles de mes
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val canGoBack = currentMonth > YearMonth.from(today)
            Text("←", modifier = Modifier.clickable(enabled = canGoBack) {
                if (canGoBack) currentMonth = currentMonth.minusMonths(1)
            }, color = if (canGoBack) Color.White else Color.Gray, fontSize = 20.sp)

            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))} ${currentMonth.year}",
                color = Color.White,
                fontSize = 18.sp
            )

            Text("→", modifier = Modifier.clickable {
                currentMonth = currentMonth.plusMonths(1)
            }, color = Color.White, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            dayHeaders.forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 280.dp)
        ) {
            items(calendarDays) { date ->
                if (date == null) {
                    Box(modifier = Modifier.size(40.dp))
                } else {
                    val isPast = date.isBefore(today)
                    val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
                    val isToday = date == today
                    val isSelected = selectedDate == date

                    val borderColor = when {
                        isSelected -> Color.White
                        isToday -> Color.Blue
                        isSunday || isPast -> Color.Transparent
                        else -> Color.Green
                    }

                    val bgColor = when {
                        isSunday -> Color.Red
                        isPast -> Color.DarkGray
                        else -> Color.Transparent
                    }

                    val textColor = when {
                        isSunday || isPast -> Color.LightGray
                        else -> Color.White
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                            .background(bgColor)
                            .border(2.dp, borderColor)
                            .clickable(enabled = !isSunday && !isPast) {
                                selectedDate = date
                                onDateSelected(date)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = date.dayOfMonth.toString(), color = textColor, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Leyenda
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Leyenda:", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(Color.Red))
                Spacer(modifier = Modifier.width(8.dp))
                Text("No disponible (Domingo)", color = Color.White, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).border(2.dp, Color.Blue))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hoy", color = Color.White, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).border(2.dp, Color.Green))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Disponible", color = Color.White, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).border(2.dp, Color(0xFFFFA500)))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pocos huecos disponibles", color = Color.White, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).border(2.dp, Color.White))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Día seleccionado", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}