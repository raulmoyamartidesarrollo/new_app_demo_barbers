package com.github.jetbrains.rssreader.androidApp.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.github.jetbrains.rssreader.androidApp.FirebaseService
import com.github.jetbrains.rssreader.androidApp.Peluquero
import com.github.jetbrains.rssreader.androidApp.models.Barberia

@Composable
fun SeleccionBarberiaScreen(
    barberiasDisponibles: List<Barberia>,
    navController: NavController
) {
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val sliderState = rememberLazyListState()

    var indexActual by remember { mutableStateOf(0) }
    var opcionSeleccionada by remember { mutableStateOf("Servicios") }
    var favoritoId by remember { mutableStateOf<String?>(null) }
    var peluquerosPorBarberia = remember { mutableStateMapOf<String, List<Peluquero>>() }
    var serviciosPorBarberia = remember { mutableStateMapOf<String, List<Map<String, Any>>>() }

    LaunchedEffect(Unit) {
        val uid = FirebaseService.getCurrentUser()?.uid
        uid?.let {
            FirebaseService.obtenerBarberiaFavorita(it) { idFavorita ->
                favoritoId = idFavorita
            }
        }
    }

    LaunchedEffect(sliderState.firstVisibleItemIndex) {
        indexActual = sliderState.firstVisibleItemIndex
        opcionSeleccionada = "Servicios" // Cambiar tab a servicios
        val nuevaBarberia = barberiasDisponibles.getOrNull(indexActual)
        nuevaBarberia?.let {
            if (!serviciosPorBarberia.contains(it.id)) {
                FirebaseService.getServiciosNegocio(it.id, { servicios ->
                    serviciosPorBarberia[it.id] = servicios
                }, {})
            }
        }
    }

    val barberiaActual = barberiasDisponibles.getOrNull(indexActual)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1C2A39))) {
        LazyRow(
            state = sliderState,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            items(barberiasDisponibles.size) { index ->
                val barberia = barberiasDisponibles[index]
                Box(
                    modifier = Modifier
                        .width(screenWidth * 0.85f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = rememberAsyncImagePainter(barberia.galeria.firstOrNull()),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            painter = rememberAsyncImagePainter(barberia.logoUrl),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(barberia.nombre, color = Color.Black, fontWeight = FontWeight.Bold)
                        Text(barberia.direccion, color = Color.DarkGray, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Menú superior
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFFF6680)).padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val opciones = listOf("Servicios", "Peluqueros", "Mapa", "Llamar")
            opciones.forEach { opcion ->
                Text(
                    opcion,
                    color = if (opcionSeleccionada == opcion) Color.White else Color.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (opcionSeleccionada == opcion) Color(0xFF1C2A39) else Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable {
                            opcionSeleccionada = opcion

                            if (opcion == "Peluqueros") {
                                barberiaActual?.let {
                                    if (!peluquerosPorBarberia.contains(it.id)) {
                                        FirebaseService.getPeluquerosDelNegocio(it.id) { peluqueros ->
                                            peluquerosPorBarberia[it.id] = peluqueros
                                        }
                                    }
                                }
                            } else if (opcion == "Servicios") {
                                barberiaActual?.let {
                                    if (!serviciosPorBarberia.contains(it.id)) {
                                        FirebaseService.getServiciosNegocio(it.id, { servicios ->
                                            serviciosPorBarberia[it.id] = servicios
                                        }, {})
                                    }
                                }
                            } else if (opcion == "Mapa") {
                                barberiaActual?.let {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("geo:0,0?q=${Uri.encode(it.direccion)}")
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    context.startActivity(intent)
                                }
                            } else if (opcion == "Llamar") {
                                barberiaActual?.let {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${it.telefono}")
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Contenido dinámico
        when (opcionSeleccionada) {
            "Servicios" -> {
                val servicios = barberiaActual?.let { serviciosPorBarberia[it.id] } ?: emptyList()
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    servicios.forEach { servicio ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2C3E50))
                                .padding(12.dp)
                                .padding(bottom = 8.dp)
                        ) {
                            Column {
                                Text(servicio["nombre"].toString(), color = Color.White, fontWeight = FontWeight.Bold)
                                Text("\u23F0 ${servicio["duracion"]} min", color = Color.LightGray, fontSize = 12.sp)
                                Text("\u20AC ${servicio["precio"]}", color = Color(0xFF00C853), fontSize = 14.sp)
                            }


                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            "Peluqueros" -> {
                val peluqueros = barberiaActual?.let { peluquerosPorBarberia[it.id] } ?: emptyList()
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    peluqueros.forEach { peluquero ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2C3E50))
                                .padding(12.dp)
                                .padding(bottom = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(peluquero.nombre.firstOrNull()?.toString() ?: "?", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("${peluquero.nombre} ${peluquero.apellidos}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text(peluquero.email, color = Color.LightGray, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Favorito
        barberiaActual?.let { barberia ->
            val esFavorita = favoritoId == barberia.id
            Button(
                onClick = {
                    val clienteId = FirebaseService.getCurrentUser()?.uid ?: return@Button
                    if (esFavorita) {
                        FirebaseService.quitarBarberiaFavoritaCliente(clienteId, {
                            favoritoId = null
                        }, {})
                    } else {
                        FirebaseService.guardarBarberiaFavoritaCliente(clienteId, barberia.id, {
                            favoritoId = barberia.id
                        }, {})
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esFavorita) Color.Red else Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (esFavorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (esFavorita) "Quitar de favoritos" else "Marcar como favorito")
            }
        }
    }
}