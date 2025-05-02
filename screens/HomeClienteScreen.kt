package com.github.jetbrains.rssreader.androidApp.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.*
import com.google.firebase.auth.FirebaseAuth
import com.github.jetbrains.rssreader.androidApp.components.ChatBotCliente
import com.github.jetbrains.rssreader.androidApp.FirebaseService
import com.github.jetbrains.rssreader.androidApp.Cita

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeClienteScreen(navController: NavHostController) {
    val auth = remember { FirebaseAuth.getInstance() }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var nombreUsuario by remember { mutableStateOf("") }
    var ultimaCita by remember {
        mutableStateOf(
            mapOf(
                "fecha" to "-",
                "hora" to "-",
                "servicio" to "-",
                "estado" to "-",
                "peluquero" to "-",
                "precio" to "-"
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
    val cardColor = Color.White
    val acento = Color(0xFFFF6680)
    val textoPrincipal = Color(0xFF1C1C1E)
    val textoSecundario = Color(0xFF8E8E93)
    val botonsecundario = Color(0xFFFF6680)

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

                Text(
                    text = "Hola, $nombreUsuario 👋",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textoPrincipal,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Tu próxima cita", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = textoSecundario)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = cardColor,
                    elevation = 6.dp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("📅 Fecha: ${ultimaCita["fecha"]}", color = textoPrincipal, fontSize = 16.sp)
                        Text("⏰ Hora: ${ultimaCita["hora"]}", color = textoPrincipal, fontSize = 16.sp)
                        Text("💇 Servicio: ${ultimaCita["servicio"]}", color = textoPrincipal, fontSize = 16.sp)
                        Text("🎯 Precio: ${ultimaCita["precio"]}", color = textoPrincipal, fontSize = 16.sp)
                        Text("👤 Peluquero: ${ultimaCita["peluquero"]}", color = textoPrincipal, fontSize = 16.sp)
                        Text(
                            "📝 Estado: ${ultimaCita["estado"]}",
                            color = if (ultimaCita["estado"] == "pendiente") acento else Color(0xFF4CD964),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* editar cita */ },
                            colors = ButtonDefaults.buttonColors(backgroundColor = acento),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Editar cita", color = Color.White)
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
                                            .border(
                                                2.dp,
                                                if (index < completadas) Color(0xFF00FF41) else Color.LightGray,
                                                RoundedCornerShape(12.dp)
                                            ),
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
        }
    }
}