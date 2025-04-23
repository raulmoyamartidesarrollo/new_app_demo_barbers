package com.github.jetbrains.rssreader.androidApp.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController

@Composable
fun BottomBarCliente(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("¿Llamar a la peluquería?") },
            text = { Text("¿Deseas llamar a la peluquería Miguel Ángel al 964 010 203?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:964010203")
                    }
                    context.startActivity(intent)
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(bottom = 16.dp)
            .zIndex(1f)
    ) {
        // Borde superior verde
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color(0xFF00FF41))
                .align(Alignment.TopCenter)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 🔹 HOME
            IconWithLabel(icon = Icons.Default.Home, label = "Home") {
                navController.navigate("home_cliente") {
                    popUpTo("home_cliente") { inclusive = true }
                    launchSingleTop = true
                }
            }

            // 🔹 SERVICIOS
            IconWithLabel(icon = Icons.Default.List, label = "Servicios") {
                navController.navigate("servicios_cliente") // Asegúrate de que esta ruta existe
            }

            Spacer(modifier = Modifier.width(56.dp)) // espacio para botón central

            // 🔹 CUENTA
            IconWithLabel(icon = Icons.Default.Person, label = "Cuenta") {
                navController.navigate("mi_cuenta") {
                    popUpTo("mi_cuenta") { inclusive = true }
                    launchSingleTop = true
                }
            }

            // 🔹 LLAMAR
            IconWithLabel(icon = Icons.Default.Call, label = "Llamar") {
                showDialog = true
            }
        }

        // 🔹 BOTÓN FLOTANTE CENTRAL: PEDIR CITA
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-36).dp)
                .size(70.dp)
                .background(Color.Black, CircleShape)
                .zIndex(2f)
        ) {
            FloatingActionButton(
                onClick = {
                    navController.navigate("pedir_cita") // Asegúrate de tener esta ruta en tu NavHost
                },
                backgroundColor = Color.Black,
                contentColor = Color(0xFF00FF41),
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .border(
                        width = 5.dp,
                        color = Color(0xFF00FF41),
                        shape = CircleShape
                    )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Pedir cita")
            }
        }
    }
}

@Composable
fun IconWithLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 8.dp)
            .let {
                if (onClick != null) it.clickable { onClick() } else it
            }
    ) {
        Icon(icon, contentDescription = label, tint = Color(0xFF00FF41))
        Text(label, fontSize = 12.sp, color = Color.White)
    }
}