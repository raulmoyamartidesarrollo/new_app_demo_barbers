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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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

@Composable
fun SeleccionBarberiaScreen(
    barberiasDisponibles: List<Barberia>,
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var favoritoId by remember { mutableStateOf<String?>(null) }
    var seccionActual by remember { mutableStateOf("servicios") }
    var serviciosActuales by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    val peluquerosPorBarberia = remember { mutableStateMapOf<String, List<Peluquero>>() }

    val listState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUser()?.uid?.let { uid ->
            FirebaseService.obtenerBarberiaFavorita(uid) { favoritoId = it }
        }
    }
    val barberia = barberiasDisponibles.getOrNull(listState.firstVisibleItemIndex)
    barberia?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C2A39))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Image(
                    painter = rememberAsyncImagePainter(barberia.galeria.firstOrNull()),
                    contentDescription = "Imagen principal",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                Image(
                    painter = rememberAsyncImagePainter(barberia.logoUrl),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(Alignment.BottomStart)
                        .offset(y = 40.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
            Text(barberia.nombre.uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("${barberia.direccion} • Abierto hasta ${barberia.horaCierre}", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BotonAccion("Servicios", Icons.Default.ContentCut, seccionActual == "servicios") {
                    seccionActual = "servicios"
                    FirebaseService.getServiciosNegocio(barberia.id, {
                        serviciosActuales = it
                    }, {})
                }
                BotonAccion("Staff", Icons.Default.Groups, seccionActual == "staff") {
                    seccionActual = "staff"
                    if (!peluquerosPorBarberia.containsKey(barberia.id)) {
                        FirebaseService.getPeluquerosDelNegocio(barberia.id) {
                            peluquerosPorBarberia[barberia.id] = it
                        }
                    }
                }
                BotonAccion("Favorito", Icons.Default.Favorite, favoritoId == barberia.id) {
                    FirebaseService.getCurrentUser()?.uid?.let { uid ->
                        if (favoritoId == barberia.id) {
                            FirebaseService.quitarBarberiaFavoritaCliente(uid, { favoritoId = null }, {})
                        } else {
                            FirebaseService.guardarBarberiaFavoritaCliente(uid, barberia.id, { favoritoId = barberia.id }, {})
                        }
                    }
                }
                BotonAccion("Mapa", Icons.Default.LocationOn, false) {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("geo:0,0?q=${Uri.encode(barberia.direccion)}")
                        setPackage("com.google.android.apps.maps")
                    }
                    context.startActivity(intent)
                }
                BotonAccion("Llamar", Icons.Default.Call, false) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${barberia.telefono}")
                    }
                    context.startActivity(intent)
                }
            }
            if (seccionActual == "servicios") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    serviciosActuales.forEach {
                        val nombre = it["nombre"]?.toString() ?: ""
                        val precio = it["precio"]?.toString() ?: "0"
                        val duracion = it["duracion"]?.toString() ?: "0"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContentCut, contentDescription = null, tint = Color.Black)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(nombre, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("⏱ $duracion min", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text("€ $precio", color = Color(0xFF00C853), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (seccionActual == "staff") {
                val peluqueros = peluquerosPorBarberia[barberia.id] ?: emptyList()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    peluqueros.forEach {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${it.nombre} ${it.apellidos}", fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Peluquero", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotonAccion(texto: String, icono: ImageVector, seleccionado: Boolean, onClick: () -> Unit) {
    val background = if (seleccionado) Color(0xFF1C2A39) else Color.White
    val color = if (seleccionado) Color.White else Color.Black

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(icono, contentDescription = texto, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(texto, color = color, fontSize = 10.sp)
    }
}