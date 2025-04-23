package com.github.jetbrains.rssreader.androidApp.components
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight

import coil.compose.rememberAsyncImagePainter
import com.github.jetbrains.rssreader.androidApp.models.Barberia


@Composable
fun SeleccionBarberiaScreen(
    barberiasDisponibles: List<com.github.jetbrains.rssreader.androidApp.models.Barberia>,
    onSeleccionar: (Barberia) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Selecciona tu barbería favorita",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(barberiasDisponibles) { barberia ->
                Card(
                    modifier = Modifier
                        .width(250.dp)
                        .clickable { onSeleccionar(barberia) },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.Black)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(barberia.logoUrl),
                            contentDescription = "Logo barbería",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = barberia.nombre,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = barberia.direccion,
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = "Abierto hasta ${barberia.horaCierre}",
                            fontSize = 12.sp,
                            color = Color.Green
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Recibirás notificaciones y harás tus reservas en la barbería que selecciones.",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}