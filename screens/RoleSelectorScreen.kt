package com.github.jetbrains.rssreader.androidApp.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun RoleSelectorScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Simular inicio de sesión como...")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.navigate("home_admin") }) {
            Text("Administrador")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("home_peluquero") }) {
            Text("Peluquero")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("home_cliente") }) {
            Text("Cliente")
        }
    }
}