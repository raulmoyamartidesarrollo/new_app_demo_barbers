package com.github.jetbrains.rssreader.androidApp.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import com.github.jetbrains.rssreader.androidApp.FirebaseService
import com.github.jetbrains.rssreader.androidApp.models.Barberia

@Composable
fun SeleccionBarberiaScreen(
    barberiasDisponibles: List<Barberia>,
    navController: NavController, // 🚀 Recibimos navController para navegar
    onSeleccionar: (Barberia) -> Unit = {}
) {
    var favoritoId by remember { mutableStateOf<String?>(null) }
    var mostrarAlerta by remember { mutableStateOf(true) }
    var iniciarAnimacion by remember { mutableStateOf(false) }
    var paginaActual by remember { mutableStateOf(0) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var barberiaSeleccionada by remember { mutableStateOf<Barberia?>(null) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val listState = rememberLazyListState()

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

    // Actualizar página actual según el scroll
    LaunchedEffect(listState.firstVisibleItemIndex) {
        paginaActual = listState.firstVisibleItemIndex
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C2A39))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxSize()
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
                                    texto = "Book",
                                    icono = Icons.Default.CalendarToday,
                                    seleccionado = true,
                                    onClick = { onSeleccionar(barberia) }
                                )

                                BotonAccion(
                                    texto = "Favorito",
                                    icono = if (favoritoId == barberia.id) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    seleccionado = false,
                                    onClick = {
                                        val clienteId = FirebaseService.getCurrentUser()?.uid
                                        clienteId?.let {
                                            favoritoId = barberia.id
                                            FirebaseService.guardarBarberiaFavoritaCliente(
                                                clienteId = it,
                                                idNegocio = barberia.id,
                                                onSuccess = {
                                                    barberiaSeleccionada = barberia
                                                    mostrarConfirmacion = true
                                                },
                                                onFailure = { error -> println("Error: ${error.message}") }
                                            )
                                        }
                                    }
                                )

                                BotonAccion(
                                    texto = "Localización",
                                    icono = Icons.Default.MoreHoriz,
                                    seleccionado = false,
                                    onClick = { /* acción adicional futura */ }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Peluqueros",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("L", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Leandro", fontSize = 14.sp, color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Descripción",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit...",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Dots animados
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                barberiasDisponibles.forEachIndexed { index, _ ->
                    val sizeDot by animateDpAsState(
                        targetValue = if (index == paginaActual) 12.dp else 8.dp,
                        animationSpec = tween(durationMillis = 300)
                    )
                    Box(
                        modifier = Modifier
                            .size(sizeDot)
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == paginaActual) Color.White else Color.Gray
                            )
                    )
                }
            }
        }

        // Modal inicial de selección
        if (mostrarAlerta) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = iniciarAnimacion,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(durationMillis = 600)
                    ) + fadeIn(animationSpec = tween(600)),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(0.5f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2A39))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "¡Atención!",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Debes seleccionar una barbería favorita para poder reservar citas.",
                                    color = Color.LightGray,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp
                                )
                            }

                            Button(
                                onClick = {
                                    mostrarAlerta = false
                                    iniciarAnimacion = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("Entendido", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Modal de Confirmación Favorito
        if (mostrarConfirmacion && barberiaSeleccionada != null) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmacion = false },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarConfirmacion = false
                            navController.navigate("home_cliente") {
                                popUpTo("inicio_usuario") { inclusive = true }
                            }
                        }
                    ) {
                        Text("OK")
                    }
                },
                title = { Text(text = "¡Barbería seleccionada!") },
                text = { Text("Has seleccionado: ${barberiaSeleccionada?.nombre}") },
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