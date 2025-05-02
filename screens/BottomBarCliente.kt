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
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBarCliente(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val bordeColor = Color(0xFFFF6680)
    val fondoMenu = Color.White
    val colorInactivo = Color(0xFF1C2D3C)
    val colorActivo = bordeColor

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

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
            .background(fondoMenu)
            .padding(bottom = 16.dp)
            .zIndex(1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(bordeColor)
                .align(Alignment.TopCenter)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconWithLabel(
                icon = Icons.Default.Home,
                label = "Home",
                selected = currentRoute == "home_cliente",
                colorActivo = colorActivo,
                colorInactivo = colorInactivo
            ) {
                navController.navigate("home_cliente") {
                    popUpTo("home_cliente") { inclusive = true }
                    launchSingleTop = true
                }
            }

            IconWithLabel(
                icon = Icons.Default.List,
                label = "Servicios",
                selected = currentRoute == "servicios_cliente",
                colorActivo = colorActivo,
                colorInactivo = colorInactivo
            ) {
                navController.navigate("servicios_cliente")
            }

            Spacer(modifier = Modifier.width(56.dp))

            IconWithLabel(
                icon = Icons.Default.Person,
                label = "Cuenta",
                selected = currentRoute == "mi_cuenta",
                colorActivo = colorActivo,
                colorInactivo = colorInactivo
            ) {
                navController.navigate("mi_cuenta") {
                    popUpTo("mi_cuenta") { inclusive = true }
                    launchSingleTop = true
                }
            }

            IconWithLabel(
                icon = Icons.Default.Call,
                label = "Llamar",
                selected = false, // Nunca se marca como activa
                colorActivo = colorActivo,
                colorInactivo = colorInactivo
            ) {
                showDialog = true
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-36).dp)
                .size(70.dp)
                .background(fondoMenu, CircleShape)
                .zIndex(2f)
        ) {
            FloatingActionButton(
                onClick = {
                    navController.navigate("pedir_cita")
                },
                backgroundColor = fondoMenu,
                contentColor = colorInactivo,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .border(
                        width = 5.dp,
                        color = bordeColor,
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
    selected: Boolean,
    colorActivo: Color,
    colorInactivo: Color,
    onClick: (() -> Unit)? = null
) {
    val color = if (selected) colorActivo else colorInactivo

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 8.dp)
            .let {
                if (onClick != null) it.clickable { onClick() } else it
            }
    ) {
        Icon(icon, contentDescription = label, tint = color)
        Text(label, fontSize = 12.sp, color = color)
    }
}