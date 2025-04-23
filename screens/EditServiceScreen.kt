package com.github.jetbrains.rssreader.androidApp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.safeDrawingPadding

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun EditServiceScreen(navController: NavHostController, negocioId: String, serviceId: String) {
    val db = FirebaseFirestore.getInstance()
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var duracion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar los datos del servicio
    LaunchedEffect(Unit) {
        db.collection("negocios").document(negocioId)
            .collection("servicios").document(serviceId)
            .get()
            .addOnSuccessListener { doc ->
                nombre = doc.getString("nombre") ?: ""
                precio = (doc.getLong("precio") ?: 0).toString()
                duracion = (doc.getLong("duracion") ?: 0).toString()
                descripcion = doc.getString("descripcion") ?: ""
                isLoading = false
            }
    }

    Scaffold(scaffoldState = scaffoldState) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF6680))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1C2D3C))
                    .safeDrawingPadding()
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { focusManager.clearFocus() }
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Editar Servicio",
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = duracion,
                    onValueChange = { duracion = it },
                    label = { Text("Duración (min)", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { navController.navigate("pantalla_gestion_servicios") },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                    ) {
                        Text("Volver", color = Color.White)
                    }

                    Button(
                        onClick = {
                            val data = mapOf(
                                "nombre" to nombre,
                                "precio" to precio.toIntOrNull(),
                                "duracion" to duracion.toIntOrNull(),
                                "descripcion" to descripcion
                            )

                            db.collection("negocios").document(negocioId)
                                .collection("servicios").document(serviceId)
                                .update(data)
                                .addOnSuccessListener {
                                    scope.launch {
                                        scaffoldState.snackbarHostState
                                            .showSnackbar("Servicio actualizado correctamente")
                                    }
                                    nombre = ""
                                    precio = ""
                                    duracion = ""
                                    descripcion = ""
                                }
                                .addOnFailureListener {
                                    scope.launch {
                                        scaffoldState.snackbarHostState
                                            .showSnackbar("Error al actualizar el servicio")
                                    }
                                }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF6680))
                    ) {
                        Text("Guardar cambios", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun customTextFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    textColor = Color.White,
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.LightGray,
    cursorColor = Color.White
)