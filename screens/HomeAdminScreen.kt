package com.github.jetbrains.rssreader.androidApp.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Store
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.FirebaseService

@Composable
fun HomeAdminScreen(navController: NavHostController) {
    val showPermissionRequested = remember { mutableStateOf(false) }
    val showLogoutDialog = remember { mutableStateOf(false) }
    val user = FirebaseService.getCurrentUser()
    var userName by remember { mutableStateOf("") }

    // Solicitar permiso de notificaciones si aplica (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !showPermissionRequested.value) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { showPermissionRequested.value = true }
        )
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Cargar nombre del usuario
    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            FirebaseService.getUserName(uid,
                onSuccess = { name -> userName = name },
                onFailure = { userName = "Administrador" }
            )
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C2D3C))
            .padding(WindowInsets.safeDrawing.asPaddingValues()),
        backgroundColor = Color(0xFF1C2D3C),
        topBar = {
            TopAppBar(
                backgroundColor = Color(0xFF1C2D3C),
                contentColor = Color.White,
                elevation = 4.dp,
                title = { Text("Hola, ${userName.ifEmpty { "Admin" }}") },
                actions = {
                    IconButton(onClick = { showLogoutDialog.value = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = Color.White)
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CardAdminAcceso(
                icon = Icons.Default.Store,
                text = "Gestionar negocio"
            ) { navController.navigate("gestionar_negocio") }

            CardAdminAcceso(
                icon = Icons.Default.ContentCut,
                text = "Gestionar servicios"
            ) { navController.navigate("pantalla_gestion_servicios") }

            CardAdminAcceso(
                icon = Icons.Default.Event,
                text = "Ver citas"
            ) { navController.navigate("pantalla_ver_citas") }

            CardAdminAcceso(
                icon = Icons.Default.Groups,
                text = "Gestionar trabajadores"
            ) { navController.navigate("pantalla_gestion_trabajadores") }

            CardAdminAcceso(
                icon = Icons.Default.AccountCircle,
                text = "Mi cuenta"
            ) { navController.navigate("mi_cuenta_Admin") }
        }

        if (showLogoutDialog.value) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog.value = false },
                title = { Text("Cerrar sesión") },
                text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
                confirmButton = {
                    TextButton(onClick = {
                        FirebaseService.logout()
                        navController.popBackStack()
                        navController.navigate("start")
                        showLogoutDialog.value = false
                    }) {
                        Text("Cerrar sesión")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog.value = false }) {
                        Text("Cancelar")
                    }
                },
                backgroundColor = Color.White,
                contentColor = Color.Black
            )
        }

       /*Button(onClick = {
            navController.navigate("test_notif")
        }) {
            Text("Test Notificación Push")
        }*/
    }
}

@Composable
fun CardAdminAcceso(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        backgroundColor = Color.White,
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color(0xFF1C2D3C),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Text(
                text = text,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}