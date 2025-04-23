package com.github.jetbrains.rssreader.androidApp.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.FirebaseService
import com.github.jetbrains.rssreader.androidApp.Peluquero
import kotlinx.coroutines.launch

@Composable
fun AdminGestionarTrabajadoresScreen(navController: NavHostController) {
    var negocioId by remember { mutableStateOf<String?>(null) }
    var peluqueros by remember { mutableStateOf<List<Peluquero>>(emptyList()) }

    val showDialog = remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var peluqueroEditando by remember { mutableStateOf<Peluquero?>(null) }

    val showConfirmDialog = remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()
    val showSnackbar = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(showSnackbar.value) {
        if (showSnackbar.value) {
            scaffoldState.snackbarHostState.showSnackbar("Perfecto, vamos a borrar")
            showSnackbar.value = false
        }
    }

    LaunchedEffect(Unit) {
        FirebaseService.getNegocioIdActual(
            onSuccess = { id -> negocioId = id },
            onFailure = { Log.e("Firebase", "Error al obtener negocioId: ${it.message}") }
        )
    }

    LaunchedEffect(negocioId) {
        negocioId?.let { id ->
            FirebaseService.getPeluquerosDelNegocio(id) {
                peluqueros = it
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize().background(Color(0xFF1C2D3C)),
        backgroundColor = Color(0xFF1C2D3C),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    nombre = ""
                    apellidos = ""
                    email = ""
                    password = ""
                    peluqueroEditando = null
                    showDialog.value = true
                },
                backgroundColor = Color(0xFF4CAF50)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Añadir trabajador", tint = Color.White)
            }
        },
        bottomBar = {
            BottomAppBar(
                backgroundColor = Color.DarkGray,
                contentPadding = PaddingValues(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate("home_admin") },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Volver al inicio")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Trabajadores del negocio",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp).align(Alignment.CenterHorizontally)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(peluqueros) { peluquero ->
                        Card(
                            backgroundColor = Color.White,
                            elevation = 4.dp,
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFF1C2D3C), modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = "${peluquero.nombre} ${peluquero.apellidos}", fontSize = 18.sp, color = Color.Black)
                                        Text(text = peluquero.email, fontSize = 14.sp, color = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    TextButton(onClick = {
                                        nombre = peluquero.nombre
                                        apellidos = peluquero.apellidos
                                        email = peluquero.email
                                        password = ""
                                        peluqueroEditando = peluquero
                                        showDialog.value = true
                                    }) {
                                        Text("Editar")
                                    }
                                    TextButton(onClick = {
                                        showConfirmDialog.value = true
                                    }) {
                                        Text("Eliminar", color = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showDialog.value) {
                Dialog(onDismissRequest = { showDialog.value = false }) {
                    val keyboardController = LocalSoftwareKeyboardController.current

                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    keyboardController?.hide()
                                })
                            }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp).wrapContentHeight().imePadding(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(if (peluqueroEditando == null) "Nuevo trabajador" else "Editar trabajador", fontSize = 18.sp, color = Color.Black)

                            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Contraseña") },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(icon, contentDescription = if (passwordVisible) "Ocultar" else "Mostrar")
                                    }
                                }
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { showDialog.value = false }) {
                                    Text("Cancelar", color = Color.Black)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = {
                                    if (nombre.isBlank() || apellidos.isBlank() || email.isBlank() || password.isBlank()) {
                                        scope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar("Por favor, completa todos los campos.")
                                        }
                                        return@TextButton
                                    }

                                    if (negocioId == null) {
                                        scope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar("Error: negocio no encontrado.")
                                        }
                                        return@TextButton
                                    }

                                    FirebaseService.crearPeluquero(
                                        negocioId = negocioId!!,
                                        nombre = nombre,
                                        apellidos = apellidos,
                                        email = email,
                                        password = password,
                                        onSuccess = {
                                            showDialog.value = false
                                            nombre = ""
                                            apellidos = ""
                                            email = ""
                                            password = ""
                                            peluqueroEditando = null
                                            FirebaseService.getPeluquerosDelNegocio(negocioId!!) {
                                                peluqueros = it
                                            }
                                            scope.launch {
                                                scaffoldState.snackbarHostState.showSnackbar("Trabajador creado correctamente")
                                            }
                                        },
                                        onFailure = { e ->
                                            Log.e("Firebase", "Error al guardar peluquero: ${e.message}")
                                            scope.launch {
                                                scaffoldState.snackbarHostState.showSnackbar("Error al crear trabajador: ${e.message}")
                                            }
                                        }
                                    )
                                }) {
                                    Text("Guardar", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            if (showConfirmDialog.value) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog.value = false },
                    title = { Text("¿Eliminar trabajador?") },
                    text = { Text("¿Seguro que quieres eliminar a este trabajador?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showConfirmDialog.value = false
                            showSnackbar.value = true
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog.value = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
