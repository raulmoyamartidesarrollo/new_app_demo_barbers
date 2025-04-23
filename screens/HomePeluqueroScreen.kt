package com.github.jetbrains.rssreader.androidApp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun AdminPeluqueroScreen(navController: NavHostController) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C2D3C)),
        backgroundColor = Color(0xFF1C2D3C),
        bottomBar = {
            BottomAppBar(
                backgroundColor = Color.DarkGray,
                contentPadding = PaddingValues(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate("home_admin") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Volver al inicio")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Pantalla Home del Peluquero", color = Color.White)
        }
    }
}