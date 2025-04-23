package com.github.jetbrains.rssreader.androidApp.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.R
import com.github.jetbrains.rssreader.androidApp.components.CalendarioDisponible
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PedirCitaScreen(navController: NavHostController) {
    val servicios = listOf("Corte", "Barba", "Corte + Barba", "Coloración")
    val peluqueros = listOf("Juan", "Laura", "Carlos")

    var servicioSeleccionado by remember { mutableStateOf("") }
    var peluqueroSeleccionado by remember { mutableStateOf("") }
    var fechaSeleccionada by remember { mutableStateOf("") }
    var horaSeleccionada by remember { mutableStateOf("") }
    var fechaLocalSeleccionada by remember { mutableStateOf<LocalDate?>(null) }

    var showCalendar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val inputColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = Color(0xFF00FF41),
        unfocusedBorderColor = Color(0xFF00FF41),
        textColor = Color.White,
        placeholderColor = Color.LightGray,
        cursorColor = Color.White
    )

    ScaffoldCliente(navController = navController) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues()) // ✅ SafeArea
        ) {
            Image(
                painter = painterResource(id = R.drawable.fondo_login),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                Text("Pedir Cita", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Text("Selecciona un servicio:", color = Color.White)
                FlowRow(
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    servicios.forEach { servicio ->
                        AssistChip(
                            onClick = { servicioSeleccionado = servicio },
                            label = {
                                Text(servicio, color = if (servicioSeleccionado == servicio) Color.Black else Color.White)
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (servicioSeleccionado == servicio) Color(0xFF00FF41) else Color.Transparent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Selecciona un peluquero:", color = Color.White)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    peluqueros.forEach { peluquero ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(100.dp)
                                .clickable { peluqueroSeleccionado = peluquero }
                        ) {
                            Card(
                                backgroundColor = if (peluqueroSeleccionado == peluquero) Color(0xFF00FF41) else Color.LightGray,
                                shape = MaterialTheme.shapes.medium,
                                elevation = 4.dp
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.foto_peluquero),
                                    contentDescription = "Foto de $peluquero",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .padding(4.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(peluquero, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Selecciona una fecha:", color = Color.White)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCalendar = true }
                ) {
                    OutlinedTextField(
                        value = fechaSeleccionada,
                        onValueChange = {},
                        placeholder = { Text("Haz clic para elegir fecha", color = Color.LightGray) },
                        readOnly = true,
                        enabled = false,
                        colors = inputColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (fechaLocalSeleccionada != null) {
                    Text("Selecciona una hora:", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    val tabLabels = listOf("Mañana", "Tarde")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        tabLabels.forEachIndexed { index, label ->
                            val isSelected = selectedTabIndex == index
                            Column(
                                modifier = Modifier
                                    .clickable { selectedTabIndex = index }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .graphicsLayer {
                                        alpha = if (isSelected) 1f else 0.5f
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = label,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .height(3.dp)
                                            .width(40.dp)
                                            .background(Color(0xFF00FF41))
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val dayOfWeek = fechaLocalSeleccionada!!.dayOfWeek
                    val horarios = when {
                        dayOfWeek == DayOfWeek.SATURDAY -> listOf(
                            listOf("10:00", "10:20", "10:40", "11:00", "11:20", "11:40", "12:00", "12:20", "12:40", "13:00", "13:20", "13:40"),
                            emptyList()
                        )
                        else -> listOf(
                            listOf("10:00", "10:20", "10:40", "11:00", "11:20", "11:40", "12:00", "12:20", "12:40", "13:00", "13:20", "13:40"),
                            listOf("16:00", "16:20", "16:40", "17:00", "17:20", "17:40", "18:00", "18:20", "18:40", "19:00", "19:20", "19:40")
                        )
                    }

                    FlowRow(
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        horarios[selectedTabIndex].forEach { hora ->
                            AssistChip(
                                onClick = { horaSeleccionada = hora },
                                label = {
                                    Text(hora, color = if (horaSeleccionada == hora) Color.Black else Color.White)
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (horaSeleccionada == hora) Color(0xFF00FF41) else Color.Transparent
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (
                            servicioSeleccionado.isNotEmpty() &&
                            peluqueroSeleccionado.isNotEmpty() &&
                            fechaSeleccionada.isNotEmpty() &&
                            horaSeleccionada.isNotEmpty()
                        ) {
                            showConfirmDialog = true
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Por favor, completa todos los campos correctamente.")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF00FF41),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Confirmar cita")
                }

                Spacer(modifier = Modifier.height(96.dp))
            }

            if (showCalendar) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { showCalendar = false }
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color(0xFF1E1E1E))
                            .padding(16.dp)
                    ) {
                        CalendarioDisponible { fecha ->
                            fechaSeleccionada = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            fechaLocalSeleccionada = fecha
                            horaSeleccionada = ""
                            selectedTabIndex = 0
                            showCalendar = false
                        }
                    }
                }
            }

            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text("Confirmar cita") },
                    text = {
                        Text("¿Confirmas tu cita para $servicioSeleccionado con $peluqueroSeleccionado el día $fechaSeleccionada a las $horaSeleccionada?")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showConfirmDialog = false
                            showLoading = true
                            coroutineScope.launch {
                                delay(2000)
                                showLoading = false
                                showSuccessDialog = true
                            }
                        }) {
                            Text("Sí")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }

            if (showLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00FF41))
                }
            }

            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
                    title = { Text("Cita guardada") },
                    text = { Text("Tu cita ha sido guardada correctamente.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showSuccessDialog = false
                            navController.navigate("home_cliente")
                        }) {
                            Text("Aceptar")
                        }
                    }
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
            )
        }
    }
}