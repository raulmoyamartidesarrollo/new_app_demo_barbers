package com.github.jetbrains.rssreader.androidApp.screens

import android.Manifest
import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.firebase.auth.FirebaseAuth
import com.github.jetbrains.rssreader.androidApp.components.ChatBotCliente
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeClienteScreen(navController: NavHostController) {
    val nombreUsuario = "Miguel"
    val ultimaCita = mapOf(
        "fecha" to "5 de abril de 2025",
        "hora" to "17:30",
        "servicio" to "Corte + Barba",
        "estado" to "Pendiente"
    )
    val completadas = 4
    val auth = remember { FirebaseAuth.getInstance() }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val permissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)

    ScaffoldCliente(navController = navController) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.fondo_login),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .safeDrawingPadding(),
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hola, $nombreUsuario", fontSize = 24.sp, color = Color.White)
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color(0xFFF5F5F5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tu última cita", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fecha: ${ultimaCita["fecha"]}")
                        Text("Hora: ${ultimaCita["hora"]}")
                        Text("Servicio: ${ultimaCita["servicio"]}")
                        Text(
                            "Estado: ${ultimaCita["estado"]}",
                            color = if (ultimaCita["estado"] == "Pendiente") Color(0xFFD35400) else Color(0xFF27AE60)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { /* TODO: editar cita */ },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF00FF41),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Editar cita")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color(0xFF1C1C1C),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "COMPLETA Y GANA 1 CORTE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFFFFD700)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Column {
                            for (fila in 0 until 2) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    for (columna in 0 until 4) {
                                        val index = fila * 4 + columna
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .border(
                                                    width = 2.dp,
                                                    color = if (index < completadas) Color(0xFF00FF41) else Color.Gray,
                                                    shape = RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (index < completadas) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Completado",
                                                    tint = Color(0xFF00FF41),
                                                    modifier = Modifier.size(24.dp)
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
                        "Recuerda aceptar permisos para recibir notificaciones sobre tus citas o ver tu ubicación.",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))


                Button(
                    onClick = {
                        navController.navigate("inicio_usuario") {
                            popUpTo("home_cliente") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFE57373),
                        contentColor = Color.White
                    )
                ) {
                    Text("Seleccionar otra peluquería")
                }
            }

            ChatBotCliente()

            // 🔒 Confirmación de logout
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
                        }) {
                            Text("Sí")
                        }
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