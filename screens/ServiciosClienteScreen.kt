package com.github.jetbrains.rssreader.androidApp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController


@Composable
fun ServiciosClienteScreen(navController: NavHostController) {
    val fondo = Color(0xFF1C2D3C)

    ScaffoldCliente(navController = navController) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo) // ✅ Fondo sólido en lugar de imagen
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Nuestros Servicios",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                ServicioItem(nombre = "Corte de Pelo", duracion = "30 min", precio = "12€")
                ServicioItem(nombre = "Corte + Barba", duracion = "45 min", precio = "18€")
                ServicioItem(nombre = "Arreglo de Barba", duracion = "20 min", precio = "8€")
                ServicioItem(nombre = "Coloración", duracion = "60 min", precio = "20€")
            }

            //ChatBotCliente()
        }
    }
}

@Composable
fun ServicioItem(nombre: String, duracion: String, precio: String) {
    Card(
        backgroundColor = Color.White.copy(alpha = 0.1f),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = nombre, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Duración: $duracion", color = Color.White, fontSize = 14.sp)
            Text(text = "Precio: $precio", color = Color.White, fontSize = 14.sp)
        }
    }
}