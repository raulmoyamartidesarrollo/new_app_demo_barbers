package com.github.jetbrains.rssreader.androidApp.screens

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.FirebaseService
import com.github.jetbrains.rssreader.androidApp.Peluquero
import com.github.jetbrains.rssreader.androidApp.Cita
import com.github.jetbrains.rssreader.androidApp.Cliente
import com.github.jetbrains.rssreader.androidApp.HorarioDia
import com.github.jetbrains.rssreader.androidApp.components.CalendarioSemanalHorizontal
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.padding

@Composable
fun DropdownMenuBox(
    opciones: List<String>,
    seleccionado: String,
    onSeleccionar: (String) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = seleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(onClick = {
                    onSeleccionar(opcion)
                    expanded = false
                }) {
                    Text(opcion)
                }
            }
        }
    }
}

@Composable
fun DropdownMenuBoxHorasDisponibles(
    horasDisponibles: List<String>,
    horaSeleccionada: String,
    onSeleccionar: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = horaSeleccionada,
            onValueChange = {},
            readOnly = true,
            label = { Text("Hora disponible") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            horasDisponibles.forEach { hora ->
                DropdownMenuItem(onClick = {
                    onSeleccionar(hora)
                    expanded = false
                }) {
                    Text(hora)
                }
            }
        }
    }
}


@Composable
fun AdminVerCitasScreen(navController: NavHostController) {
    val context = LocalContext.current
    var negocioId by remember { mutableStateOf<String?>(null) }
    var peluqueros by remember { mutableStateOf<List<Peluquero>>(emptyList()) }
    var citas by remember { mutableStateOf<List<Cita>>(emptyList()) }
    var peluqueroSeleccionado by remember { mutableStateOf<Peluquero?>(null) }
    var horario by remember { mutableStateOf<Map<String, HorarioDia>>(emptyMap()) }

    val showCrearReservaDialog = remember { mutableStateOf(false) }
    val showCamposObligatorios = remember { mutableStateOf(false) }
    val showReservaConfirmada = remember { mutableStateOf(false) }
    val showConfirmDelete = remember { mutableStateOf(false) }
    val showConfirmEdit = remember { mutableStateOf(false) }

    var citaSeleccionada by remember { mutableStateOf<Cita?>(null) }
    var clienteSeleccionadoId by remember { mutableStateOf("") }
    var servicioSeleccionadoId by remember { mutableStateOf("") }
    var fechaSeleccionada by remember { mutableStateOf(LocalDate.now()) }
    var horaSeleccionada by remember { mutableStateOf("") }
    val showDatePicker = remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var servicios by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    fun recargarCitas() {
        if (negocioId != null && peluqueroSeleccionado != null) {
            FirebaseService.getReservasPorPeluquero(
                negocioId = negocioId!!,
                peluqueroId = peluqueroSeleccionado!!.id,
                onResult = { citas = it },
                onError = { Log.e("Firebase", "Error al actualizar reservas: ${it.message}") }
            )
        }
    }

    LaunchedEffect(Unit) {
        FirebaseService.getNegocioIdActual(
            onSuccess = { id -> negocioId = id },
            onFailure = { Log.e("Firebase", "Error al obtener negocioId: ${it.message}") }
        )
    }

    LaunchedEffect(negocioId) {
        negocioId?.let { id ->
            FirebaseService.getPeluquerosDelNegocio(id) {
                peluqueros = it
                peluqueroSeleccionado = if (it.size == 1) it.firstOrNull() else null
            }
            FirebaseService.getHorarioNegocio(
                onSuccess = { horario = it },
                onFailure = { Log.e("Firebase", "Error al obtener horario: ${it.message}") }
            )
            FirebaseService.getClientesPorNegocio(id) { clientes = it }
            FirebaseService.getServiciosNegocio(id,
                onSuccess = { servicios = it },
                onFailure = { Log.e("Firebase", "Error al obtener servicios: ${it.message}") })
        }
    }

    LaunchedEffect(peluqueroSeleccionado?.id) {
        recargarCitas()
    }

    if (showDatePicker.value) {
        val actual = fechaSeleccionada
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                if (selectedDate.dayOfWeek != java.time.DayOfWeek.SUNDAY) {
                    fechaSeleccionada = selectedDate
                }
                showDatePicker.value = false
            },
            actual.year, actual.monthValue - 1, actual.dayOfMonth
        ).apply {
            datePicker.minDate = Calendar.getInstance().timeInMillis
        }.show()
    }
    if (showReservaConfirmada.value) {
        AlertDialog(
            onDismissRequest = { showReservaConfirmada.value = false },
            title = { Text("Reserva guardada") },
            text = { Text("La cita se ha guardado correctamente.") },
            confirmButton = {
                TextButton(onClick = { showReservaConfirmada.value = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showCamposObligatorios.value) {
        AlertDialog(
            onDismissRequest = { showCamposObligatorios.value = false },
            title = { Text("Campos obligatorios") },
            text = { Text("Completa todos los campos antes de guardar la cita.") },
            confirmButton = {
                TextButton(onClick = { showCamposObligatorios.value = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showConfirmDelete.value && citaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete.value = false },
            title = { Text("¿Eliminar cita?") },
            text = { Text("¿Estás seguro de que deseas eliminar esta cita?") },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseService.eliminarReserva(
                        negocioId = negocioId!!,
                        reservaId = citaSeleccionada!!.id,
                        onSuccess = {
                            recargarCitas()
                            showConfirmDelete.value = false
                            citaSeleccionada = null
                        },
                        onFailure = {
                            Log.e("Reserva", "Error al eliminar la reserva: ${it.message}")
                        }
                    )
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showConfirmEdit.value && citaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { showConfirmEdit.value = false },
            title = { Text("¿Editar cita?") },
            text = { Text("Se abrirá el formulario con los datos actuales para editar.") },
            confirmButton = {
                TextButton(onClick = {
                    clienteSeleccionadoId = citaSeleccionada!!.idCliente
                    servicioSeleccionadoId = citaSeleccionada!!.idServicio
                    horaSeleccionada = citaSeleccionada!!.hora
                    fechaSeleccionada = LocalDate.parse(citaSeleccionada!!.fecha, formatter)
                    peluqueroSeleccionado = peluqueros.find { it.id == citaSeleccionada!!.idPeluquero }

                    showCrearReservaDialog.value = true
                    showConfirmEdit.value = false
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmEdit.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C2D3C)),
        backgroundColor = Color(0xFF1C2D3C),
        floatingActionButton = {
            Box(modifier = Modifier.padding(end = 16.dp, bottom = 32.dp)) {
                FloatingActionButton(
                    onClick = {
                        citaSeleccionada = null // ← importante
                        clienteSeleccionadoId = ""
                        servicioSeleccionadoId = ""
                        horaSeleccionada = ""
                        fechaSeleccionada = LocalDate.now()
                        peluqueroSeleccionado = null
                        showCrearReservaDialog.value = true
                    },
                    backgroundColor = Color(0xFF4CAF50)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir cita", tint = Color.White)
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                backgroundColor = Color.Transparent,
                contentPadding = WindowInsets.safeDrawing.asPaddingValues(),
                elevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("home_admin") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver al inicio")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top).asPaddingValues())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text("Citas por peluquero", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                items(peluqueros) { peluquero ->
                    Card(
                        backgroundColor = if (peluquero.id == peluqueroSeleccionado?.id) Color(0xFF4CAF50) else Color.White,
                        elevation = 6.dp,
                        modifier = Modifier
                            .clickable { peluqueroSeleccionado = peluquero }
                            .width(200.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = if (peluquero.id == peluqueroSeleccionado?.id) Color.White else Color.Black
                            )
                            Text(
                                peluquero.nombre,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (peluquero.id == peluqueroSeleccionado?.id) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            if (peluqueroSeleccionado != null) {
                Text("Calendario semanal", color = Color.White, fontWeight = FontWeight.Bold)
                CalendarioSemanalHorizontal(
                    citas = citas,
                    horario = horario,
                    onCeldaLibreClick = { fecha, hora ->
                        citaSeleccionada = null
                        fechaSeleccionada = fecha
                        horaSeleccionada = hora
                        showCrearReservaDialog.value = true
                    },
                    onEditarClick = {
                        citaSeleccionada = it
                        showConfirmEdit.value = true
                    },
                    onEliminarClick = {
                        citaSeleccionada = it
                        showConfirmDelete.value = true
                    }
                )
            }
        }
    }
    if (showCrearReservaDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showCrearReservaDialog.value = false
                citaSeleccionada = null
            },
            title = { Text(if (citaSeleccionada == null) "Crear nueva cita" else "Editar cita") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DropdownMenuBox(
                        opciones = listOf("Selecciona un peluquero") + peluqueros.map { "${it.nombre} ${it.apellidos}" },
                        seleccionado = peluqueroSeleccionado?.let { "${it.nombre} ${it.apellidos}" } ?: "Selecciona un peluquero",
                        onSeleccionar = { nombre ->
                            peluqueroSeleccionado = peluqueros.find { "${it.nombre} ${it.apellidos}" == nombre }
                        },
                        label = "Peluquero"
                    )
                    Spacer(Modifier.height(8.dp))

                    val opcionesClientes = clientes.map { it.id to "${it.nombre} ${it.apellidos}" }
                    val nombreClienteSeleccionado = opcionesClientes.find { it.first == clienteSeleccionadoId }?.second ?: ""

                    DropdownMenuBox(
                        opciones = opcionesClientes.map { it.second },
                        seleccionado = nombreClienteSeleccionado,
                        onSeleccionar = { nombreMostrado ->
                            clienteSeleccionadoId = opcionesClientes.find { it.second == nombreMostrado }?.first ?: ""
                        },
                        label = "Selecciona cliente"
                    )

                    Spacer(Modifier.height(8.dp))

                    val opcionesServicios = servicios.map { it["id"].toString() to it["nombre"].toString() }
                    val nombreServicioSeleccionado = opcionesServicios.find { it.first == servicioSeleccionadoId }?.second ?: ""

                    DropdownMenuBox(
                        opciones = opcionesServicios.map { it.second },
                        seleccionado = nombreServicioSeleccionado,
                        onSeleccionar = { nombre ->
                            servicioSeleccionadoId = opcionesServicios.find { it.second == nombre }?.first ?: ""
                        },
                        label = "Selecciona servicio"
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fechaSeleccionada.format(formatter),
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker.value = true },
                        label = { Text("Selecciona Fecha") }
                    )
                    Spacer(Modifier.height(8.dp))

                    val nombreDia = fechaSeleccionada.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es"))
                        .replaceFirstChar { it.uppercaseChar() }

                    val horasDisponibles = calcularHorasDisponibles(
                        horario[nombreDia],
                        citas.filterNot { it.id == citaSeleccionada?.id },
                        fechaSeleccionada
                    )

                    DropdownMenuBoxHorasDisponibles(
                        horasDisponibles = horasDisponibles,
                        horaSeleccionada = horaSeleccionada
                    ) {
                        horaSeleccionada = it
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (clienteSeleccionadoId.isBlank() || servicioSeleccionadoId.isBlank() || horaSeleccionada.isBlank() || peluqueroSeleccionado == null || negocioId == null) {
                        showCamposObligatorios.value = true
                        return@TextButton
                    }

                    val datosReserva = mapOf(
                        "idPeluquero" to peluqueroSeleccionado!!.id,
                        "idServicio" to servicioSeleccionadoId,
                        "idCliente" to clienteSeleccionadoId,
                        "fecha" to fechaSeleccionada.format(formatter),
                        "hora" to horaSeleccionada
                    )

                    if (citaSeleccionada == null) {
                        FirebaseService.crearReserva(
                            negocioId = negocioId!!,
                            idPeluquero = peluqueroSeleccionado!!.id,
                            idServicio = servicioSeleccionadoId,
                            idCliente = clienteSeleccionadoId,
                            fecha = fechaSeleccionada.format(formatter),
                            hora = horaSeleccionada,
                            onSuccess = {
                                recargarCitas()
                                showCrearReservaDialog.value = false
                                showReservaConfirmada.value = true
                                clienteSeleccionadoId = ""
                                servicioSeleccionadoId = ""
                                horaSeleccionada = ""
                                fechaSeleccionada = LocalDate.now()
                            },
                            onFailure = { Log.e("Reserva", "Error al guardar: ${it.message}") }
                        )
                    } else {
                        FirebaseService.actualizarReserva(
                            negocioId = negocioId!!,
                            reservaId = citaSeleccionada!!.id,
                            datosActualizados = datosReserva,
                            onSuccess = {
                                recargarCitas()
                                showCrearReservaDialog.value = false
                                showReservaConfirmada.value = true
                                clienteSeleccionadoId = ""
                                servicioSeleccionadoId = ""
                                horaSeleccionada = ""
                                fechaSeleccionada = LocalDate.now()
                                citaSeleccionada = null
                            },
                            onFailure = { Log.e("Reserva", "Error al actualizar: ${it.message}") }
                        )
                    }
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCrearReservaDialog.value = false
                    citaSeleccionada = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Función para calcular las horas disponibles
fun calcularHorasDisponibles(
    horarioDia: HorarioDia?,
    citas: List<Cita>,
    fechaSeleccionada: LocalDate
): List<String> {
    if (horarioDia == null) return emptyList()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val tramos = mutableListOf<String>()

    listOf(
        horarioDia.aperturaManana to horarioDia.cierreManana,
        horarioDia.aperturaTarde to horarioDia.cierreTarde
    ).forEach { (inicio, fin) ->
        if (!inicio.isNullOrEmpty() && !fin.isNullOrEmpty()) {
            var horaActual = LocalTime.parse(inicio)
            val horaFin = LocalTime.parse(fin)
            while (!horaActual.isAfter(horaFin)) {
                tramos.add(horaActual.toString().substring(0, 5))
                horaActual = horaActual.plusMinutes(30)
            }
        }
    }

    val fechaStr = fechaSeleccionada.format(formatter)
    val horasOcupadas = citas.filter { it.fecha == fechaStr }.map { it.hora.trim() }
    return tramos.filterNot { horasOcupadas.contains(it) }
}