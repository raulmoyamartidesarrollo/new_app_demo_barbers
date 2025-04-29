package com.github.jetbrains.rssreader.androidApp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.FirebaseService
import com.github.jetbrains.rssreader.androidApp.models.Barberia
import com.github.jetbrains.rssreader.androidApp.components.SeleccionBarberiaScreen
import com.github.jetbrains.rssreader.androidApp.components.BotonAccion
@Composable
fun InicioUsuarioScreen(
    navController: NavHostController
) {
    var barberiasDisponibles by remember { mutableStateOf<List<Barberia>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseService.getTodasLasBarberias(
            onSuccess = {
                barberiasDisponibles = it
                cargando = false
            },
            onFailure = {
                Log.e("Firebase", "Error cargando barberías: ${it.message}")
                cargando = false
            }
        )
    }

    if (cargando) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        SeleccionBarberiaScreen(
            barberiasDisponibles = barberiasDisponibles,
            navController = navController, // <-- ⚡ Añades esta línea
            onSeleccionar = { barberiaSeleccionada ->
                val clienteId = FirebaseService.getCurrentUser()?.uid
                if (clienteId != null) {
                    FirebaseService.guardarBarberiaFavoritaCliente(
                        clienteId = clienteId,
                        idNegocio = barberiaSeleccionada.id,
                        onSuccess = {
                            navController.navigate("home_cliente") {
                                popUpTo("inicio_usuario") { inclusive = true }
                            }
                        },
                        onFailure = { error ->
                            Log.e("Firebase", "Error al guardar barbería favorita: ${error.message}")
                        }
                    )
                }
            }
        )
    }
}