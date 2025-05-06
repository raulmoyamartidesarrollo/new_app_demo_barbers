package com.github.jetbrains.rssreader.androidApp.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.github.jetbrains.rssreader.androidApp.FirebaseService

@Composable
fun BottomBarCliente(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var telefonoNegocio by remember { mutableStateOf<String?>(null) }

    // Obtener el teléfono del negocio favorito del cliente
    LaunchedEffect(Unit) {
        telefonoNegocio = FirebaseService.getTelefonoNegocioDelCliente()
    }

    val bordeColor = Color(0xFFFF6680)
    val fondoMenu = Color.White
    val colorInactivo = Color(0xFF1C2D3C)
    val colorActivo = bordeColor

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("¿Llamar a la peluquería?") },
            text = {
                if (telefonoNegocio != null)
                    Text("¿Deseas llamar a la peluquería al número $telefonoNegocio?")
                else
                    Text("No se pudo obtener el número de teléfono.")
            },
            confirmButton = {
                if (telefonoNegocio != null) {
                    TextButton(onClick = {
                        showDialog = false
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$telefonoNegocio")
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Sí")
                    }
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
                selected = false,
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