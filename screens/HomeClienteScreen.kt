package com.github.jetbrains.rssreader.androidApp.screens

import DropdownMenuBoxHorasDisponiblesSimple
import DropdownMenuBoxSimple
import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.Cita
import com.github.jetbrains.rssreader.androidApp.FirebaseService
import com.github.jetbrains.rssreader.androidApp.HorarioDia
import com.github.jetbrains.rssreader.androidApp.Peluquero
import com.github.jetbrains.rssreader.androidApp.calcularHorasDisponiblesSimple
import com.github.jetbrains.rssreader.androidApp.components.ChatBotCliente
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeClienteScreen(navController: NavHostController) {
    val auth = remember { FirebaseAuth.getInstance() }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var nombreUsuario by remember { mutableStateOf("") }
    var citaCompleta by remember { mutableStateOf<Cita?>(null) }
    val completadas = 4
    val showBorrarDialog = remember { mutableStateOf(false) }

    val permissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)

    val fondo = Color(0xFF1C2D3C)
    val cardColor = Color.White
    val acento = Color(0xFFFF6680)
    val textoPrincipal = Color(0xFF1C1C1E)
    val textoSecundario = Color(0xFF8E8E93)
    val botonsecundario = Color(0xFFFF6680)

    val showEditarDialog = remember { mutableStateOf(false) }
    val peluqueros = remember { mutableStateOf<List<Peluquero>>(emptyList()) }
    val horario = remember { mutableStateOf<Map<String, HorarioDia>>(emptyMap()) }
    val servicios = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var negocioId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        FirebaseService.getUserName()?.let {
            nombreUsuario = it
        }
        FirebaseService.getUltimaCitaCliente { cita ->
            cita?.let {
                citaCompleta = it
            }
        }
        FirebaseService.getCurrentUser()?.uid?.let { uid ->
            FirebaseService.obtenerBarberiaFavorita(uid) { idNegocio ->
                if (idNegocio != null) {
                    negocioId = idNegocio
                    FirebaseService.getPeluquerosDelNegocio(idNegocio) { peluqueros.value = it }
                    FirebaseService.getHorarioNegocioCliente(idNegocio, { horario.value = it }, { })
                    FirebaseService.getServiciosNegocio(idNegocio, { servicios.value = it }, { })
                }
            }
        }
    }

    ScaffoldCliente(navController = navController) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .safeDrawingPadding()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hola, $nombreUsuario 👋",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textoSecundario
                    )
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = acento
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Tu próxima cita", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = textoSecundario)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = cardColor,
                    elevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("📅 Fecha: ${citaCompleta?.fecha ?: "-"}", color = textoPrincipal, fontSize = 16.sp)
                        Text("⏰ Hora: ${citaCompleta?.hora ?: "-"}", color = textoPrincipal, fontSize = 16.sp)
                        Text("💇 Servicio: ${citaCompleta?.servicio ?: "-"}", color = textoPrincipal, fontSize = 16.sp)
                        Text("🎯 Precio: ${citaCompleta?.precio ?: "-"} €", color = textoPrincipal, fontSize = 16.sp)
                        Text("👤 Peluquero: ${citaCompleta?.nombreCliente ?: "-"}", color = textoPrincipal, fontSize = 16.sp)
                        Text(
                            "📝 Estado: ${citaCompleta?.estado ?: "-"}",
                            color = if (citaCompleta?.estado == "pendiente") acento else Color(0xFF4CD964),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (citaCompleta?.estado == "pendiente") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { showEditarDialog.value = true },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = acento),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Editar", color = Color.White)
                                }
                                Button(
                                    onClick = { showBorrarDialog.value = true },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancelar", color = Color.White)
                                }
                            }
                        }else {
                            Button(
                                onClick = { navController.navigate("pedir_cita") },
                                colors = ButtonDefaults.buttonColors(backgroundColor = acento),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Pedir nueva cita", color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Fidelización", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = textoSecundario)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = cardColor,
                    elevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("COMPLETA Y GANA 1 CORTE", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textoPrincipal)
                        Spacer(modifier = Modifier.height(16.dp))
                        repeat(2) { fila ->
                            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                                repeat(4) { columna ->
                                    val index = fila * 4 + columna
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .border(2.dp, if (index < completadas) Color(0xFF00FF41) else Color.LightGray, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (index < completadas) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Hecho",
                                                tint = Color(0xFF00FF41),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                if (multiplePermissionsState.permissions.any { !it.status.isGranted }) {
                    Text(
                        "🔒 Recuerda aceptar permisos para recibir notificaciones o ver tu ubicación.",
                        color = acento,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        navController.navigate("inicio_usuario") {
                            popUpTo("home_cliente") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = botonsecundario),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Seleccionar otra peluquería", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

            ChatBotCliente()

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Cerrar sesión") },
                    text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
                    confirmButton = {
                        TextButton(onClick = {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo("home_cliente") { inclusive = true }
                            }
                            showLogoutDialog = false
                        }) { Text("Sí") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Cancelar")
                        }
                    },
                    backgroundColor = Color.White,
                    contentColor = textoPrincipal,
                    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
                )
            }
            if (showBorrarDialog.value && citaCompleta != null) {
                AlertDialog(
                    onDismissRequest = { showBorrarDialog.value = false },
                    title = { Text("Cancelar cita") },
                    text = { Text("¿Estás seguro de que quieres cancelar esta cita?") },
                    confirmButton = {
                        TextButton(onClick = {
                            FirebaseService.borrarReservaCliente(
                                negocioId = negocioId,
                                reservaId = citaCompleta!!.id,
                                onSuccess = {
                                    citaCompleta = null
                                    showBorrarDialog.value = false
                                },
                                onFailure = {
                                    Log.e("Reserva", "Error al cancelar cita: ${it.message}")
                                    showBorrarDialog.value = false
                                }
                            )
                        }) {
                            Text("Sí, cancelar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBorrarDialog.value = false }) {
                            Text("No")
                        }
                    },
                    backgroundColor = Color.White,
                    contentColor = Color.Black
                )
            }

            if (showEditarDialog.value && citaCompleta != null) {
                EditarReservaDialogCliente(
                    showDialog = showEditarDialog,
                    cita = citaCompleta!!,
                    peluqueros = peluqueros.value,
                    horario = horario.value,
                    servicios = servicios.value,
                    negocioId = negocioId,
                    onReservaEditada = {
                        showEditarDialog.value = false
                    }
                )
            }
        }
    }
}

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
    var peluqueroSeleccionado by remember { mutableStateOf(peluqueros.find { it.id == cita.idPeluquero }) }
    var servicioSeleccionadoId by remember { mutableStateOf(cita.idServicio) }
    var horaSeleccionada by remember { mutableStateOf(cita.hora) }
    var fechaSeleccionada by remember { mutableStateOf(LocalDate.parse(cita.fecha, formatter)) }
    val showCamposObligatorios = remember { mutableStateOf(false) }

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
                    ) + cita.hora

                    DropdownMenuBoxHorasDisponiblesSimple(
                        horasDisponibles = horasDisponibles.distinct(),
                        horaSeleccionada = horaSeleccionada
                    ) {
                        horaSeleccionada = it
                    }
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
