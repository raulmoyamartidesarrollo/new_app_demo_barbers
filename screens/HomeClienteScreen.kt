package com.github.jetbrains.rssreader.androidApp.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import com.github.jetbrains.rssreader.androidApp.Cita
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.R
import com.google.accompanist.permissions.*
import com.google.firebase.auth.FirebaseAuth
import com.github.jetbrains.rssreader.androidApp.components.ChatBotCliente
import com.github.jetbrains.rssreader.androidApp.FirebaseService // Asegúrate de importar correctamente

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeClienteScreen(navController: NavHostController) {
    val auth = remember { FirebaseAuth.getInstance() }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Estado dinámico
    var nombreUsuario by remember { mutableStateOf("") }
    var ultimaCita by remember {
        mutableStateOf(
            mapOf(
                "fecha" to "-",
                "hora" to "-",
                "servicio" to "-",
                "estado" to "-"
            )
        )
    }

    val completadas = 4

    val permissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)

    val fondo = Color(0xFF1C2D3C)
    val cardColor = Color(0xFFFDFDFD)

    LaunchedEffect(Unit) {
        nombreUsuario = FirebaseService.getUserName() ?: "Usuario"
        FirebaseService.getUltimaCitaCliente { cita: Cita? ->
            cita?.let {
                ultimaCita = mapOf(
                    "fecha" to it.fecha,
                    "hora" to it.hora,
                    "servicio" to it.servicio,
                    "estado" to it.estado,
                    "peluquero" to it.nombreCliente,
                    "precio" to "${it.precio} €"
                )
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
                    Text("Hola, $nombreUsuario", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Tu última cita", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = cardColor,
                    elevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Fecha: ${ultimaCita["fecha"]}")
                        Text("Hora: ${ultimaCita["hora"]}")
                        Text("Servicio: ${ultimaCita["servicio"]}")
                        Text("Precio: ${ultimaCita["precio"]}")
                        Text("Peluquero: ${ultimaCita["peluquero"]}")
                        Text(
                            "Estado: ${ultimaCita["estado"]}",
                            color = if (ultimaCita["estado"] == "Pendiente") Color(0xFFD35400) else Color(0xFF27AE60)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { /* editar cita */ },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
                        ) {
                            Text("Editar cita", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Fidelización", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = cardColor,
                    elevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("COMPLETA Y GANA 1 CORTE", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF222222))
                        Spacer(modifier = Modifier.height(16.dp))
                        Column {
                            for (fila in 0 until 2) {
                                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                                    for (columna in 0 until 4) {
                                        val index = fila * 4 + columna
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.White)
                                                .border(
                                                    2.dp,
                                                    if (index < completadas) Color(0xFF00FF41) else Color.Gray,
                                                    RoundedCornerShape(10.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (index < completadas) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Hecho",
                                                    tint = Color(0xFF00FF41),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (multiplePermissionsState.permissions.any { !it.status.isGranted }) {
                    Text(
                        "🔒 Recuerda aceptar permisos para recibir notificaciones o ver tu ubicación.",
                        color = Color.Red,
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
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
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
                    contentColor = Color.Black,
                    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
                )
            }
        }
    }
}