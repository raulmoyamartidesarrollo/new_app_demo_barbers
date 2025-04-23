package com.github.jetbrains.rssreader.androidApp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ConfiguracionFirebaseScreen() {
    val scope = rememberCoroutineScope()
    var mensaje by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    mensaje = "Creando estructura..."
                    try {
                        crearEstructuraInicialDesdeApp()
                        mensaje = "✅ Estructura de Firestore creada correctamente."
                    } catch (e: Exception) {
                        mensaje = "❌ Error: ${e.message}"
                        Log.e("FirebaseSetup", "Error al crear estructura", e)
                    }
                }
            }) {
                Text("Crear estructura Firebase")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (mensaje.isNotEmpty()) {
                Text(mensaje, style = MaterialTheme.typography.body1)
            }
        }
    }
}

suspend fun crearEstructuraInicialDesdeApp() {
    val db = Firebase.firestore
    val negocioRef = db.collection("negocios").document("negocio_demo")

    negocioRef.set(
        mapOf(
            "nombre" to "",
            "telefono" to "",
            "categoria" to "",
            "logoUrl" to null,
            "galeriaUrls" to listOf<String>(),
            "diasEspeciales" to listOf<String>()
        )
    ).await()

    negocioRef.collection("peluqueros").document("peluquero_demo").set(
        mapOf(
            "nombre" to "",
            "email" to "",
            "rol" to "peluquero",
            "fotoPerfil" to null,
            "diasNoDisponibles" to listOf<String>()
        )
    ).await()

    negocioRef.collection("servicios").document("servicio_demo").set(
        mapOf(
            "nombre" to "",
            "precio" to 0,
            "duracion" to 0
        )
    ).await()

    val horarios = listOf(
        "lunes", "martes", "miércoles", "jueves", "viernes"
    ).map {
        it to listOf(
            mapOf("inicio" to "10:00", "fin" to "14:00"),
            mapOf("inicio" to "16:00", "fin" to "20:00")
        )
    } + listOf(
        "sábado" to listOf(mapOf("inicio" to "10:00", "fin" to "14:00")),
        "domingo" to emptyList()
    )

    for ((dia, bloques) in horarios) {
        negocioRef.collection("horarios").document(dia).set(
            mapOf("dia" to dia, "horarios" to bloques)
        ).await()
    }

    negocioRef.collection("clientes").document("cliente_demo").set(
        mapOf("clienteId" to "cliente_demo")
    ).await()

    val clienteRef = db.collection("clientes").document("cliente_demo")
    clienteRef.set(
        mapOf(
            "nombre" to "",
            "email" to "",
            "telefono" to "",
            "fotoPerfil" to null,
            "negocioId" to "negocio_demo",
            "citasCompletadas" to 0
        )
    ).await()

    clienteRef.collection("citas").document("cita_demo").set(
        mapOf(
            "negocioId" to "negocio_demo",
            "peluqueroId" to "peluquero_demo",
            "servicioId" to "servicio_demo",
            "fecha" to "",
            "hora" to "",
            "estado" to "pendiente"
        )
    ).await()
}
