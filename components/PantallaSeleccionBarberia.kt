package com.github.jetbrains.rssreader.androidApp.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.coroutines.delay

@Composable
fun SeleccionBarberiaScreen(
    barberiasDisponibles: List<Barberia>,
    navController: NavController
) {
    var favoritoId by remember { mutableStateOf<String?>(null) }
    var mostrarAlerta by remember { mutableStateOf(true) }
    var iniciarAnimacion by remember { mutableStateOf(false) }
    var paginaActual by remember { mutableStateOf(0) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var barberiaSeleccionada by remember { mutableStateOf<Barberia?>(null) }
    var mostrarServicios by remember { mutableStateOf(false) }
    var serviciosActuales by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val peluquerosPorBarberia = remember { mutableStateMapOf<String, List<Peluquero>>() }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val clienteId = FirebaseService.getCurrentUser()?.uid
        clienteId?.let {
            FirebaseService.obtenerBarberiaFavorita(it) { idFavorita ->
                favoritoId = idFavorita
            }
        }
        delay(500)
        iniciarAnimacion = true
    }

    LaunchedEffect(listState.firstVisibleItemIndex) {
        paginaActual = listState.firstVisibleItemIndex
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1C2A39))) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                items(barberiasDisponibles.size) { index ->
                    val barberia = barberiasDisponibles[index]

                    Card(
                        modifier = Modifier
                            .width(screenWidth * 0.9f)
                            .height(500.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Color(0xFF1C2A39))
                                .padding(16.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(barberia.logoUrl),
                                contentDescription = "Logo barbería",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = barberia.nombre.uppercase(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${barberia.direccion} • Abierto hasta ${barberia.horaCierre}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                BotonAccion(
                                    texto = "Servicios",
                                    icono = Icons.Default.CalendarToday,
                                    seleccionado = true,
                                    onClick = {
                                        FirebaseService.getServiciosNegocio(
                                            negocioId = barberia.id,
                                            onSuccess = {
                                                serviciosActuales = it
                                                mostrarServicios = true
                                            },
                                            onFailure = {
                                                Log.e("FIREBASE_DEBUG", "Error al obtener servicios: ${it.message}")
                                            }
                                        )
                                    }
                                )

                                BotonAccion(
                                    texto = "Favorito",
                                    icono = if (favoritoId == barberia.id) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    seleccionado = false,
                                    onClick = {
                                        val clienteId = FirebaseService.getCurrentUser()?.uid
                                        clienteId?.let {
                                            if (favoritoId == barberia.id) {
                                                FirebaseService.quitarBarberiaFavoritaCliente(
                                                    clienteId = it,
                                                    onSuccess = { favoritoId = null },
                                                    onFailure = {}
                                                )
                                            } else {
                                                FirebaseService.guardarBarberiaFavoritaCliente(
                                                    clienteId = it,
                                                    idNegocio = barberia.id,
                                                    onSuccess = {
                                                        favoritoId = barberia.id
                                                    },
                                                    onFailure = {}
                                                )
                                            }
                                        }
                                    }
                                )
                                BotonAccion(
                                    texto = "Localización",
                                    icono = Icons.Default.LocationOn,
                                    seleccionado = false,
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("geo:0,0?q=${Uri.encode(barberia.direccion)}")
                                            setPackage("com.google.android.apps.maps")
                                        }
                                        context.startActivity(intent)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            LaunchedEffect(barberia.id) {
                                if (!peluquerosPorBarberia.containsKey(barberia.id)) {
                                    FirebaseService.getPeluquerosDelNegocio(barberia.id) { peluqueros ->
                                        peluquerosPorBarberia[barberia.id] = peluqueros
                                    }
                                }
                            }

                            Text("Peluqueros", fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))

                            val peluqueros = peluquerosPorBarberia[barberia.id] ?: emptyList()
                            Column {
                                peluqueros.forEach { peluquero ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(Color.LightGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = peluquero.nombre.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "${peluquero.nombre} ${peluquero.apellidos}",
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                                if (peluqueros.isEmpty()) {
                                    Text("No hay peluqueros disponibles.", fontSize = 12.sp, color = Color.LightGray)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Descripción", fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit...",
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                barberiasDisponibles.forEachIndexed { index, _ ->
                    val size by animateDpAsState(
                        targetValue = if (index == paginaActual) 12.dp else 8.dp,
                        animationSpec = tween(300)
                    )
                    Box(
                        modifier = Modifier
                            .size(size)
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(if (index == paginaActual) Color.White else Color.Gray)
                    )
                }
            }
        }

        if (mostrarServicios) {
            AlertDialog(
                onDismissRequest = { mostrarServicios = false },
                confirmButton = {
                    Button(onClick = { mostrarServicios = false }) {
                        Text("Cerrar")
                    }
                },
                title = { Text("Servicios disponibles") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        serviciosActuales.forEach { servicio ->
                            val nombre = servicio["nombre"]?.toString() ?: "Sin nombre"
                            val precio = servicio["precio"]?.toString() ?: "0€"
                            val duracion = servicio["duracion"]?.toString() ?: "0 min"

                            Column {
                                Text(
                                    text = nombre,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Duración: $duracion • Precio: $precio",
                                    fontSize = 13.sp,
                                    color = Color.LightGray
                                )
                                Divider(color = Color.Gray.copy(alpha = 0.3f))
                            }
                        }
                    }
                },
                containerColor = Color(0xFF1C2A39),
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }
    }
}

@Composable
fun BotonAccion(
    texto: String,
    icono: ImageVector,
    seleccionado: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (seleccionado) Color(0xFF0A84FF) else Color.White
    val contentColor = if (seleccionado) Color.White else Color.Black

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = texto,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(texto, color = contentColor, fontSize = 14.sp)
    }
}