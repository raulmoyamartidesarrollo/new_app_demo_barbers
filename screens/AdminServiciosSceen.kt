package com.github.jetbrains.rssreader.androidApp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.firebase.firestore.ktx.toObject

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AdminServiciosScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scaffoldState = rememberScaffoldState()

    var servicios by remember { mutableStateOf(listOf<Servicio>()) }
    var negocioId by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf<Servicio?>(null) }

    LaunchedEffect(true) {
        currentUser?.let { user ->
            db.collection("usuarios").document(user.uid).get().addOnSuccessListener { userDoc ->
                val negocio = userDoc.getString("negocioId") ?: return@addOnSuccessListener
                negocioId = negocio
                db.collection("negocios").document(negocioId).collection("servicios")
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null) {
                            servicios = snapshot.documents.mapNotNull { doc ->
                                val nombre = doc.getString("nombre") ?: return@mapNotNull null
                                val precio = (doc.getLong("precio") ?: 0).toInt()
                                val duracion = (doc.getLong("duracion") ?: 0).toInt()
                                val descripcion = doc.getString("descripcion") ?: ""
                                val id = doc.id

                                Servicio(id, nombre, precio, duracion, descripcion)
                            }
                        }
                    }
            }
        }
    }

    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C2D3C))
                .safeDrawingPadding()
                .padding(16.dp)
        ) {
            Text("Servicios", color = Color.White, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate("admin_add_service") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF6680))
            ) {
                Text("Añadir Servicio", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SOLO ESTA SECCIÓN ES SCROLLABLE
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn {
                    items(servicios) { servicio ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(4.dp, Color(0xFFFF6680), RoundedCornerShape(25.dp))
                                .padding(10.dp),
                            shape = RoundedCornerShape(25.dp),
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${servicio.nombre} - ${servicio.precio}€ - ${servicio.duracion}min",
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Divider(
                                    color = Color.LightGray,
                                    thickness = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "----- Descripción -----",
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = servicio.descripcion,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Button(
                                        onClick = {
                                            navController.navigate("edit_service/$negocioId/${servicio.id}")
                                        },
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Editar", color = Color.White)
                                    }
                                    Button(
                                        onClick = { showDeleteConfirm = servicio },
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE53935)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Eliminar", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Este botón queda fuera del scroll
            Button(
                onClick = {
                    navController.navigate("home_admin")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Volver a la Home", color = Color.White)
            }
        }
    }

    showDeleteConfirm?.let { servicio ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("¿Eliminar servicio?") },
            text = { Text("¿Estás seguro de que quieres eliminar '${servicio.nombre}'?") },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("negocios").document(negocioId)
                        .collection("servicios").document(servicio.id).delete()
                    showDeleteConfirm = null
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

data class Servicio(
    val id: String = "",
    val nombre: String = "",
    val precio: Int = 0,
    val duracion: Int = 0,
    val descripcion: String = ""
)
