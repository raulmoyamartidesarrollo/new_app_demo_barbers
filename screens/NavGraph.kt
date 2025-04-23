package com.github.jetbrains.rssreader.androidApp

import com.github.jetbrains.rssreader.androidApp.screens.InicioUsuarioScreen
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.github.jetbrains.rssreader.androidApp.screens.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavigation(navController: NavHostController) {
    val currentUser = FirebaseService.getCurrentUser()
    var startDestination by remember { mutableStateOf<String?>(null) }

    if (startDestination == null) {
        LaunchedEffect(Unit) {
            if (currentUser == null) {
                startDestination = "start"
            } else {
                FirebaseService.getUserRole(
                    uid = currentUser.uid,
                    onSuccess = { rol ->
                        if (rol == "cliente") {
                            // ⚠️ Leer desde la colección CLIENTES (no usuarios)
                            FirebaseFirestore.getInstance()
                                .collection("clientes")
                                .document(currentUser.uid)
                                .get()
                                .addOnSuccessListener { doc ->
                                    val idNegocio = doc.getString("idnegocio")
                                    startDestination = if (idNegocio.isNullOrBlank()) {
                                        "inicio_usuario"
                                    } else {
                                        "home_cliente"
                                    }
                                }
                                .addOnFailureListener {
                                    startDestination = "inicio_usuario"
                                }
                        } else {
                            startDestination = when (rol) {
                                "peluquero" -> "home_peluquero"
                                "superpeluquero" -> "home_admin"
                                else -> "start"
                            }
                        }
                    },
                    onFailure = {
                        startDestination = "start"
                    }
                )
            }
        }

        SplashLoading()
    } else {
        NavHost(navController = navController, startDestination = startDestination!!) {
            composable("start") { StartScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("register") { RegisterScreen(navController) }

            composable("home_cliente") { HomeClienteScreen(navController) }
            composable("home_admin") { HomeAdminScreen(navController) }
            composable("home_peluquero") { AdminPeluqueroScreen(navController) }

            composable("mi_cuenta") { MiCuentaScreen(navController) }
            composable("servicios_cliente") { ServiciosClienteScreen(navController) }
            composable("pedir_cita") { PedirCitaScreen(navController) }
            composable("forgot_password") { ForgotClientPasswordScreen(navController) }

            composable("configuracion_firebase") { ConfiguracionFirebaseScreen() }
            composable("mi_cuenta_Admin") { MiCuentaAdminScreen(navController) }
            composable("gestionar_negocio") { GestionarNegocioScreen(navController) }
            composable("pantalla_gestion_servicios") { AdminServiciosScreen(navController) }
            composable("admin_add_service") { AdminAddServiceScreen(navController) }
            composable("edit_service/{negocioId}/{serviceId}") { backStackEntry ->
                val negocioId = backStackEntry.arguments?.getString("negocioId") ?: ""
                val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
                EditServiceScreen(navController, negocioId, serviceId)
            }
            composable("pantalla_ver_citas") { AdminVerCitasScreen(navController) }
            composable("pantalla_gestion_trabajadores") { AdminGestionarTrabajadoresScreen(navController) }

            // Pantalla de selección de barbería favorita
            composable("inicio_usuario") { InicioUsuarioScreen(navController) }
        }
    }
}

@Composable
fun SplashLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFFFF6680))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Cargando...", color = Color.White)
        }
    }
}