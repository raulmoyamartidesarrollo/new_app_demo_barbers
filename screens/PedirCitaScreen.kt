// IMPORTS
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.Cita
import com.github.jetbrains.rssreader.androidApp.FirebaseService
import com.github.jetbrains.rssreader.androidApp.HorarioDia
import com.github.jetbrains.rssreader.androidApp.Peluquero
import com.github.jetbrains.rssreader.androidApp.components.CalendarioSemanalHorizontalCliente
import com.github.jetbrains.rssreader.androidApp.screens.ScaffoldCliente
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// UTILS
fun calcularHorasDisponiblesSimple(
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

// DROPDOWN SIMPLES
@Composable
fun DropdownMenuBoxSimple(
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
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(onClick = {
                    onSeleccionar(opcion)
                    expanded = false
                }) { Text(opcion) }
            }
        }
    }
}

@Composable
fun DropdownMenuBoxHorasDisponiblesSimple(
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
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            horasDisponibles.forEach { hora ->
                DropdownMenuItem(onClick = {
                    onSeleccionar(hora)
                    expanded = false
                }) { Text(hora) }
            }
        }
    }
}

// DIALOGO CREAR RESERVA
@Composable
fun CrearReservaDialogCliente(
    showDialog: MutableState<Boolean>,
    peluqueros: List<Peluquero>,
    horario: Map<String, HorarioDia>,
    servicios: List<Map<String, Any>>,
    negocioId: String,
    fechaPreseleccionada: LocalDate,
    horaPreseleccionada: String,
    onReservaCreada: () -> Unit
) {
    var peluqueroSeleccionado by remember { mutableStateOf<Peluquero?>(null) }
    var servicioSeleccionadoId by remember { mutableStateOf("") }
    var horaSeleccionada by remember { mutableStateOf(horaPreseleccionada) }
    var fechaSeleccionada by remember { mutableStateOf(fechaPreseleccionada) }
    var clienteId by remember { mutableStateOf("") }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val showCamposObligatorios = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUser()?.uid?.let { clienteId = it }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Pedir cita") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DropdownMenuBoxSimple(
                        opciones = listOf("Selecciona un peluquero") + peluqueros.map { "${it.nombre} ${it.apellidos}" },
                        seleccionado = peluqueroSeleccionado?.let { "${it.nombre} ${it.apellidos}" } ?: "Selecciona un peluquero",
                        onSeleccionar = { nombre ->
                            peluqueroSeleccionado = peluqueros.find { "${it.nombre} ${it.apellidos}" == nombre }
                        },
                        label = "Peluquero"
                    )
                    Spacer(Modifier.height(8.dp))

                    val opcionesServicios = servicios.map { it["id"].toString() to it["nombre"].toString() }
                    val nombreServicioSeleccionado = opcionesServicios.find { it.first == servicioSeleccionadoId }?.second ?: ""

                    DropdownMenuBoxSimple(
                        opciones = opcionesServicios.map { it.second },
                        seleccionado = nombreServicioSeleccionado,
                        onSeleccionar = { nombre ->
                            servicioSeleccionadoId = opcionesServicios.find { it.second == nombre }?.first ?: ""
                        },
                        label = "Selecciona servicio"
                    )

                    Spacer(Modifier.height(8.dp))

                    val nombreDia = fechaSeleccionada.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es"))
                        .replaceFirstChar { it.uppercaseChar() }

                    val horasDisponibles = calcularHorasDisponiblesSimple(
                        horario[nombreDia],
                        emptyList(),
                        fechaSeleccionada
                    )

                    DropdownMenuBoxHorasDisponiblesSimple(
                        horasDisponibles = horasDisponibles,
                        horaSeleccionada = horaSeleccionada
                    ) { horaSeleccionada = it }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (clienteId.isBlank() || servicioSeleccionadoId.isBlank() || horaSeleccionada.isBlank() || peluqueroSeleccionado == null || negocioId.isBlank()) {
                        showCamposObligatorios.value = true
                        return@TextButton
                    }
                    FirebaseService.crearReserva(
                        negocioId = negocioId,
                        idPeluquero = peluqueroSeleccionado!!.id,
                        idServicio = servicioSeleccionadoId,
                        idCliente = clienteId,
                        fecha = fechaSeleccionada.format(formatter),
                        hora = horaSeleccionada,
                        onSuccess = {
                            showDialog.value = false
                            onReservaCreada()
                        },
                        onFailure = { Log.e("Reserva", "Error al guardar: ${it.message}") }
                    )
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("Cancelar")
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
}

// PANTALLA PRINCIPAL
@Composable
fun PedirCitaScreen(navController: NavHostController) {
    val showEditarDialog = remember { mutableStateOf(false) }
    val negocioId = remember { mutableStateOf("") }
    val peluqueros = remember { mutableStateOf<List<Peluquero>>(emptyList()) }
    val peluqueroSeleccionado = remember { mutableStateOf<Peluquero?>(null) }
    val horario = remember { mutableStateOf<Map<String, HorarioDia>>(emptyMap()) }
    val servicios = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val showCrearDialog = remember { mutableStateOf(false) }
    val showDialogBorrar = remember { mutableStateOf(false) }
    val citaSeleccionada = remember { mutableStateOf<Cita?>(null) }
    val fechaSeleccionada = remember { mutableStateOf(LocalDate.now()) }
    val horaSeleccionada = remember { mutableStateOf("") }
    val citasPeluquero = remember { mutableStateOf<List<Cita>>(emptyList()) }
    val clienteId = remember { mutableStateOf("") }
    val showReservaConfirmada = remember { mutableStateOf(false) }




    @Composable
    fun EditarReservaDialogCliente(
        showDialog: MutableState<Boolean>,
        cita: Cita,
        peluqueros: List<Peluquero>,
        horario: Map<String, HorarioDia>,
        servicios: List<Map<String, Any>>,
        negocioId: String,
        onReservaEditada: () -> Unit
    ) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        var peluqueroSeleccionado by remember { mutableStateOf<Peluquero?>(null) }
        var servicioSeleccionadoId by remember { mutableStateOf("") }
        var horaSeleccionada by remember { mutableStateOf("") }
        var fechaSeleccionada by remember { mutableStateOf(LocalDate.now()) }
        val showCamposObligatorios = remember { mutableStateOf(false) }

        // ✅ Esto fuerza la precarga al cambiar la cita
        LaunchedEffect(cita) {
            peluqueroSeleccionado = peluqueros.find { it.id == cita.idPeluquero }
            servicioSeleccionadoId = cita.idServicio
            horaSeleccionada = cita.hora
            fechaSeleccionada = LocalDate.parse(cita.fecha, formatter)
        }

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text("Editar cita") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        DropdownMenuBoxSimple(
                            opciones = listOf("Selecciona un peluquero") + peluqueros.map { "${it.nombre} ${it.apellidos}" },
                            seleccionado = peluqueroSeleccionado?.let { "${it.nombre} ${it.apellidos}" } ?: "Selecciona un peluquero",
                            onSeleccionar = { nombre ->
                                peluqueroSeleccionado = peluqueros.find { "${it.nombre} ${it.apellidos}" == nombre }
                            },
                            label = "Peluquero"
                        )
                        Spacer(Modifier.height(8.dp))

                        val opcionesServicios = servicios.map { it["id"].toString() to it["nombre"].toString() }
                        val nombreServicioSeleccionado = opcionesServicios.find { it.first == servicioSeleccionadoId }?.second ?: ""

                        DropdownMenuBoxSimple(
                            opciones = opcionesServicios.map { it.second },
                            seleccionado = nombreServicioSeleccionado,
                            onSeleccionar = { nombre ->
                                servicioSeleccionadoId = opcionesServicios.find { it.second == nombre }?.first ?: ""
                            },
                            label = "Selecciona servicio"
                        )

                        Spacer(Modifier.height(8.dp))

                        val nombreDia = fechaSeleccionada.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es"))
                            .replaceFirstChar { it.uppercaseChar() }

                        val horasDisponibles = calcularHorasDisponiblesSimple(
                            horario[nombreDia],
                            emptyList(),
                            fechaSeleccionada
                        ) + cita.hora // incluye la actual por si ya está ocupada

                        DropdownMenuBoxHorasDisponiblesSimple(
                            horasDisponibles = horasDisponibles.distinct(),
                            horaSeleccionada = horaSeleccionada
                        ) { horaSeleccionada = it }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (peluqueroSeleccionado == null || servicioSeleccionadoId.isBlank() || horaSeleccionada.isBlank() || negocioId.isBlank()) {
                            showCamposObligatorios.value = true
                            return@TextButton
                        }
                        FirebaseService.actualizarReservaCliente(
                            negocioId = negocioId,
                            reservaId = cita.id,
                            nuevaFecha = fechaSeleccionada.format(formatter),
                            nuevaHora = horaSeleccionada,
                            nuevoPeluqueroId = peluqueroSeleccionado!!.id,
                            nuevoServicioId = servicioSeleccionadoId,
                            onSuccess = {
                                showDialog.value = false
                                onReservaEditada()
                            },
                            onFailure = {
                                Log.e("Reserva", "Error al editar reserva: ${it.message}")
                            }
                        )
                    }) {
                        Text("Guardar cambios")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showCamposObligatorios.value) {
            AlertDialog(
                onDismissRequest = { showCamposObligatorios.value = false },
                title = { Text("Campos obligatorios") },
                text = { Text("Completa todos los campos para guardar los cambios.") },
                confirmButton = {
                    TextButton(onClick = { showCamposObligatorios.value = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUser()?.uid?.let { clienteId.value = it }
    }

    LaunchedEffect(Unit) {
        FirebaseService.getNegocioFavoritoCliente(
            onSuccess = { negocioId.value = it },
            onFailure = { Log.e("Firebase", "Error al obtener negocioId: ${it.message}") }
        )
    }

    LaunchedEffect(negocioId.value) {
        if (negocioId.value.isNotBlank()) {
            FirebaseService.getPeluquerosDelNegocio(negocioId.value) {
                peluqueros.value = it
                if (it.size == 1) peluqueroSeleccionado.value = it.firstOrNull()
            }
            FirebaseService.getHorarioNegocioCliente(
                negocioId = negocioId.value,
                onSuccess = { horario.value = it },
                onFailure = { Log.e("Firebase", "Error horario cliente: ${it.message}") }
            )
            FirebaseService.getServiciosNegocio(
                negocioId.value,
                onSuccess = { servicios.value = it },
                onFailure = { Log.e("Firebase", "Error servicios: ${it.message}") }
            )
        }
    }

    LaunchedEffect(key1 = peluqueroSeleccionado.value?.id, key2 = negocioId.value) {
        val id = peluqueroSeleccionado.value?.id
        if (!id.isNullOrEmpty() && negocioId.value.isNotEmpty()) {
            FirebaseService.getCitasPorPeluquero(
                negocioId = negocioId.value,
                peluqueroId = id,
                onSuccess = { citasPeluquero.value = it },
                onFailure = { citasPeluquero.value = emptyList() }
            )
        }
    }

    ScaffoldCliente(navController = navController) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C2D3C))
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text("Selecciona peluquero", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)

            LazyRow(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                items(peluqueros.value) { peluquero ->
                    Card(
                        backgroundColor = if (peluquero.id == peluqueroSeleccionado.value?.id) Color(0xFF4CAF50) else Color.White,
                        elevation = 6.dp,
                        modifier = Modifier
                            .clickable { peluqueroSeleccionado.value = peluquero }
                            .width(200.dp)
                            .padding(end = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(32.dp))
                            Text(
                                peluquero.nombre,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (peluquero.id == peluqueroSeleccionado.value?.id) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            peluqueroSeleccionado.value?.let {
                Text("Calendario semanal", color = Color.White, fontWeight = FontWeight.Bold)
                CalendarioSemanalHorizontalCliente(
                    horario = horario.value,
                    citas = citasPeluquero.value,
                    clienteId = clienteId.value,
                    onCeldaLibreClick = { fecha, hora ->
                        fechaSeleccionada.value = fecha
                        horaSeleccionada.value = hora
                        showCrearDialog.value = true
                    },
                    onEditarClick = { cita ->
                        citaSeleccionada.value = cita
                        showEditarDialog.value = true
                    },
                    onEliminarClick = { cita ->
                        citaSeleccionada.value = cita
                        showDialogBorrar.value = true
                    }
                )
            }
        }
    }

    if (showCrearDialog.value) {
        CrearReservaDialogCliente(
            showDialog = showCrearDialog,
            peluqueros = peluqueros.value,
            horario = horario.value,
            servicios = servicios.value,
            negocioId = negocioId.value,
            fechaPreseleccionada = fechaSeleccionada.value,
            horaPreseleccionada = horaSeleccionada.value,
            onReservaCreada = {
                val id = peluqueroSeleccionado.value?.id
                if (!id.isNullOrEmpty() && negocioId.value.isNotEmpty()) {
                    FirebaseService.getCitasPorPeluquero(
                        negocioId = negocioId.value,
                        peluqueroId = id,
                        onSuccess = {
                            citasPeluquero.value = it
                            showReservaConfirmada.value = true
                        },
                        onFailure = { Log.e("Firebase", "Error al actualizar citas tras crear") }
                    )
                }
            }
        )
    }
    if (showDialogBorrar.value && citaSeleccionada.value != null) {
        AlertDialog(
            onDismissRequest = { showDialogBorrar.value = false },
            title = { Text("¿Eliminar cita?") },
            text = { Text("¿Estás seguro de que quieres cancelar esta cita?") },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseService.borrarReservaCliente(
                        negocioId = negocioId.value,
                        reservaId = citaSeleccionada.value!!.id,
                        onSuccess = {
                            showDialogBorrar.value = false
                            val id = peluqueroSeleccionado.value?.id
                            if (!id.isNullOrEmpty()) {
                                FirebaseService.getCitasPorPeluquero(
                                    negocioId = negocioId.value,
                                    peluqueroId = id,
                                    onSuccess = { citasPeluquero.value = it },
                                    onFailure = { Log.e("Firebase", "Error al actualizar citas tras borrar") }
                                )
                            }
                        },
                        onFailure = { e ->
                            Log.e("Firebase", "Error al borrar cita: ${e.message}")
                            showDialogBorrar.value = false
                        }
                    )
                }) {
                    Text("Sí, borrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialogBorrar.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (showReservaConfirmada.value) {
        AlertDialog(
            onDismissRequest = { showReservaConfirmada.value = false },
            title = { Text("Cita guardada") },
            text = { Text("Tu cita ha sido guardada correctamente.") },
            confirmButton = {
                TextButton(onClick = {
                    showReservaConfirmada.value = false
                    // 🔄 Refrescar calendario por si acaso
                    val id = peluqueroSeleccionado.value?.id
                    if (!id.isNullOrEmpty() && negocioId.value.isNotEmpty()) {
                        FirebaseService.getCitasPorPeluquero(
                            negocioId = negocioId.value,
                            peluqueroId = id,
                            onSuccess = { citasPeluquero.value = it },
                            onFailure = { Log.e("Firebase", "Error al refrescar citas") }
                        )
                    }
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    if (showEditarDialog.value && citaSeleccionada.value != null) {
        EditarReservaDialogCliente(
            showDialog = showEditarDialog,
            cita = citaSeleccionada.value!!,
            peluqueros = peluqueros.value,
            horario = horario.value,
            servicios = servicios.value,
            negocioId = negocioId.value,
            onReservaEditada = {
                val id = peluqueroSeleccionado.value?.id
                if (!id.isNullOrEmpty()) {
                    FirebaseService.getCitasPorPeluquero(
                        negocioId = negocioId.value,
                        peluqueroId = id,
                        onSuccess = { citasPeluquero.value = it },
                        onFailure = { Log.e("Firebase", "Error al refrescar citas editadas") }
                    )
                }
            }
        )
    }
}