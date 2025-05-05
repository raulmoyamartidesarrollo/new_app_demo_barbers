package com.github.jetbrains.rssreader.androidApp

import PedirCitaScreen
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.github.jetbrains.rssreader.androidApp.screens.AdminAddServiceScreen
import com.github.jetbrains.rssreader.androidApp.screens.AdminGestionarTrabajadoresScreen
import com.github.jetbrains.rssreader.androidApp.screens.AdminPeluqueroScreen
import com.github.jetbrains.rssreader.androidApp.screens.AdminServiciosScreen
import com.github.jetbrains.rssreader.androidApp.screens.AdminVerCitasScreen
import com.github.jetbrains.rssreader.androidApp.screens.ConfiguracionFirebaseScreen
import com.github.jetbrains.rssreader.androidApp.screens.EditServiceScreen
import com.github.jetbrains.rssreader.androidApp.screens.ForgotClientPasswordScreen
import com.github.jetbrains.rssreader.androidApp.screens.HomeAdminScreen
import com.github.jetbrains.rssreader.androidApp.screens.HomeClienteScreen
import com.github.jetbrains.rssreader.androidApp.screens.InicioUsuarioScreen
import com.github.jetbrains.rssreader.androidApp.screens.LoginScreen
import com.github.jetbrains.rssreader.androidApp.screens.MiCuentaScreen
import com.github.jetbrains.rssreader.androidApp.screens.RegisterScreen
import com.github.jetbrains.rssreader.androidApp.screens.ServiciosClienteScreen
import com.github.jetbrains.rssreader.androidApp.screens.StartScreen
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun AppNavigation(navController: NavHostController) {
    var checking by remember { mutableStateOf(true) }
    val currentUser = FirebaseService.getCurrentUser()

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            checking = false
            navController.navigate("start") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            try {
                val clienteDoc = FirebaseFirestore.getInstance()
                    .collection("clientes")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (clienteDoc.exists()) {
                    val rol = clienteDoc.getString("rol") ?: ""
                    val idNegocio = clienteDoc.getString("idnegocio") ?: ""

                    if (rol == "cliente") {
                        if (idNegocio.isEmpty()) {
                            // No tiene barbería aún
                            navController.navigate("inicio_usuario") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            // Ya tiene barbería asignada
                            navController.navigate("home_cliente") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        // Si no es cliente, rol inválido
                        navController.navigate("start") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    // No existe el documento en clientes (mal login o usuario sin datos)
                    navController.navigate("start") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }

            } catch (e: Exception) {
                Log.e("AppNavigation", "Error detectando usuario: ${e.message}")
                navController.navigate("start") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            } finally {
                checking = false
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (checking) "splash" else "start"
    ) {
        composable("splash") { SplashLoading() }
        composable("start") { StartScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }

        composable("home_cliente") { HomeClienteScreen(navController) }
        composable("home_admin") { HomeAdminScreen(navController) }
        composable("home_peluquero") { AdminPeluqueroScreen(navController) }
        composable("inicio_usuario") { InicioUsuarioScreen(navController) }

        composable("forgot_password") { ForgotClientPasswordScreen(navController) }
        composable("configuracion_firebase") { ConfiguracionFirebaseScreen() }
        composable("mi_cuenta") { MiCuentaScreen(navController) }
        composable("servicios_cliente") { ServiciosClienteScreen(navController) }
        composable("pedir_cita") { PedirCitaScreen(navController) }
        composable("pantalla_gestion_servicios") { AdminServiciosScreen(navController) }
        composable("admin_add_service") { AdminAddServiceScreen(navController) }
        composable("edit_service/{negocioId}/{serviceId}") { backStackEntry ->
            val negocioId = backStackEntry.arguments?.getString("negocioId") ?: ""
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            EditServiceScreen(navController, negocioId, serviceId)
        }
        composable("pantalla_ver_citas") { AdminVerCitasScreen(navController) }
        composable("pantalla_gestion_trabajadores") { AdminGestionarTrabajadoresScreen(navController) }
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