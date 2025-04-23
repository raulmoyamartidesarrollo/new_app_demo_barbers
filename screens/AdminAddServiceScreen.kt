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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AdminAddServiceScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    var negocioId by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var duracion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showEmptyFieldsDialog by remember { mutableStateOf(false) }
    var showInvalidValuesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        currentUser?.let { user ->
            db.collection("usuarios").document(user.uid).get().addOnSuccessListener { userDoc ->
                negocioId = userDoc.getString("negocioId") ?: ""
            }
        }
    }

    Scaffold(scaffoldState = scaffoldState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C2D3C))
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures { focusManager.clearFocus() }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Añadir Servicio", color = Color.White, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            OutlinedTextField(
                value = duracion,
                onValueChange = { duracion = it },
                label = { Text("Duración (min)", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Validaciones
                    if (nombre.isBlank() || precio.isBlank() || duracion.isBlank() || descripcion.isBlank()) {
                        showEmptyFieldsDialog = true
                        return@Button
                    }

                    val precioInt = precio.toIntOrNull()
                    val duracionInt = duracion.toIntOrNull()

                    if (precioInt == null || duracionInt == null || precioInt <= 0 || duracionInt <= 0) {
                        showInvalidValuesDialog = true
                        return@Button
                    }

                    // Guardar en Firestore
                    val servicioNuevo = hashMapOf(
                        "nombre" to nombre,
                        "precio" to precioInt,
                        "duracion" to duracionInt,
                        "descripcion" to descripcion
                    )

                    db.collection("negocios").document(negocioId)
                        .collection("servicios")
                        .add(servicioNuevo)
                        .addOnSuccessListener {
                            nombre = ""
                            precio = ""
                            duracion = ""
                            descripcion = ""
                            showSuccessDialog = true
                        }
                        .addOnFailureListener {
                            showErrorDialog = true
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF6680))
            ) {
                Text("Crear Servicio", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { navController.navigate("home_admin") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Volver a la Home", color = Color.White)
            }
        }
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("¡Éxito!") },
            text = { Text("El servicio se ha creado correctamente.") },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Diálogo de error al guardar
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text("No se pudo crear el servicio. Inténtalo de nuevo.") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Diálogo de campos vacíos
    if (showEmptyFieldsDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyFieldsDialog = false },
            title = { Text("Campos vacíos") },
            text = { Text("Por favor, completa todos los campos antes de guardar.") },
            confirmButton = {
                TextButton(onClick = { showEmptyFieldsDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Diálogo de valores inválidos
    if (showInvalidValuesDialog) {
        AlertDialog(
            onDismissRequest = { showInvalidValuesDialog = false },
            title = { Text("Valores inválidos") },
            text = { Text("El precio y la duración deben ser números positivos mayores que cero.") },
            confirmButton = {
                TextButton(onClick = { showInvalidValuesDialog = false }) {
                    Text("Corregir")
                }
            }
        )
    }
}

@Composable
fun textFieldColors() = TextFieldDefaults.outlinedTextFieldColors(
    textColor = Color.White,
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.LightGray,
    cursorColor = Color.White
)