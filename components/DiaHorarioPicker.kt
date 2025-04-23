package com.github.jetbrains.rssreader.androidApp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.jetbrains.rssreader.androidApp.HorarioDia

@Composable
fun DiaHorarioPicker(horario: HorarioDia, onHorarioChange: (HorarioDia) -> Unit) {
    val horas = remember {
        buildList {
            for (h in 7..21) {
                add("%02d:00".format(h))
                add("%02d:30".format(h))
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownTimePickerTextField("Mañana desde", horario.aperturaManana, horas, { onHorarioChange(horario.copy(aperturaManana = it)) }, Modifier.weight(1f))
            DropdownTimePickerTextField("Hasta", horario.cierreManana, horas, { onHorarioChange(horario.copy(cierreManana = it)) }, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownTimePickerTextField("Tarde desde", horario.aperturaTarde, horas, { onHorarioChange(horario.copy(aperturaTarde = it)) }, Modifier.weight(1f))
            DropdownTimePickerTextField("Hasta", horario.cierreTarde, horas, { onHorarioChange(horario.copy(cierreTarde = it)) }, Modifier.weight(1f))
        }
    }
}