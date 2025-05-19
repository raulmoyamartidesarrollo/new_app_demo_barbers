package com.github.jetbrains.rssreader.androidApp.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
    val scope = rememberCoroutineScope()


    var showEliminarDialog by remember { mutableStateOf(false) }
    var peluqueroAEliminar by remember { mutableStateOf<Peluquero?>(null) }

    var showOpcionesDialog by remember { mutableStateOf(false) }
    var modoEliminarCitas by remember { mutableStateOf<Boolean?>(null) }

    var showSelectNuevoPeluquero by remember { mutableStateOf(false) }
    var nuevoPeluqueroId by remember { mutableStateOf<String?>(null) }

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
            BottomAppBar(backgroundColor = Color.DarkGray) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.navigationBars.asPaddingValues()) // 🔐 Esto protege contra la barra del sistema
                ) {
                    Button(
                        onClick = { navController.navigate("home_admin") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Volver al inicio")
                    }
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

                LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)) {
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
                                        peluqueroAEliminar = peluquero
                                        showEliminarDialog = true
                                    }) {
                                        Text("Eliminar", color = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showEliminarDialog) {
                AlertDialog(
                    onDismissRequest = { showEliminarDialog = false },
                    title = { Text("Eliminar trabajador") },
                    text = { Text("¿Estás seguro de que quieres eliminar a este trabajador?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showEliminarDialog = false
                            showOpcionesDialog = true
                        }) { Text("Sí") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEliminarDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (showOpcionesDialog) {
                AlertDialog(
                    onDismissRequest = { showOpcionesDialog = false },
                    title = { Text("¿Qué hacer con sus citas?") },
                    text = { Text("Este trabajador tiene citas asignadas. ¿Quieres eliminarlas o reasignarlas a otro trabajador?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showOpcionesDialog = false
                            modoEliminarCitas = true
                            scope.launch {
                                FirebaseService.eliminarPeluqueroYReservas(
                                    negocioId = negocioId!!,
                                    peluqueroId = peluqueroAEliminar!!.id,
                                    onSuccess = {
                                        peluqueroAEliminar = null
                                        FirebaseService.getPeluquerosDelNegocio(negocioId!!) { peluqueros = it }
                                        scope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar("Trabajador y sus citas eliminadas.")
                                        }
                                    },
                                    onFailure = {
                                        scope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar("Error al eliminar trabajador: ${it.message}")
                                        }
                                    }
                                )
                            }
                        }) { Text("Eliminar citas") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showOpcionesDialog = false
                            showSelectNuevoPeluquero = true
                        }) { Text("Reasignar citas") }
                    }
                )
            }

            if (showSelectNuevoPeluquero) {
                Dialog(onDismissRequest = { showSelectNuevoPeluquero = false }) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Selecciona nuevo trabajador", fontSize = 18.sp, color = Color.Black)

                            peluqueros.filter { it.id != peluqueroAEliminar?.id }.forEach { peluquero ->
                                Button(
                                    onClick = {
                                        nuevoPeluqueroId = peluquero.id
                                        showSelectNuevoPeluquero = false

                                        scope.launch {
                                            FirebaseService.reasignarCitasYEliminarPeluquero(
                                                negocioId = negocioId!!,
                                                peluqueroIdAntiguo = peluqueroAEliminar!!.id,
                                                nuevoPeluqueroId = nuevoPeluqueroId!!,
                                                onSuccess = {
                                                    peluqueroAEliminar = null
                                                    FirebaseService.getPeluquerosDelNegocio(negocioId!!) { peluqueros = it }

                                                    // ✅ Esta parte estaba mal antes por no usar scope.launch
                                                    scope.launch {
                                                        scaffoldState.snackbarHostState.showSnackbar("Citas reasignadas y trabajador eliminado.")
                                                    }
                                                },
                                                onFailure = {
                                                    scope.launch {
                                                        scaffoldState.snackbarHostState.showSnackbar("Error: ${it.message}")
                                                    }
                                                }
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text("${peluquero.nombre} ${peluquero.apellidos}")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { showSelectNuevoPeluquero = false }) {
                                Text("Cancelar", color = Color.Gray)
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
                                    if (nombre.isBlank() || email.isBlank()) {
                                        scope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar("Nombre y email no pueden estar vacíos.")
                                        }
                                        return@TextButton
                                    }

                                    if (negocioId == null) {
                                        scope.launch {
                                            scaffoldState.snackbarHostState.showSnackbar("Error: negocio no encontrado.")
                                        }
                                        return@TextButton
                                    }

                                    if (peluqueroEditando == null) {
                                        // CREAR NUEVO TRABAJADOR
                                        if (password.isBlank()) {
                                            scope.launch {
                                                scaffoldState.snackbarHostState.showSnackbar("Contraseña obligatoria para nuevo trabajador.")
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
                                                FirebaseService.getPeluquerosDelNegocio(negocioId!!) { peluqueros = it }
                                                scope.launch {
                                                    scaffoldState.snackbarHostState.showSnackbar("Trabajador creado correctamente")
                                                }
                                            },
                                            onFailure = {
                                                scope.launch {
                                                    scaffoldState.snackbarHostState.showSnackbar("Error al crear trabajador: ${it.message}")
                                                }
                                            }
                                        )
                                    } else {
                                        // EDITAR TRABAJADOR EXISTENTE
                                        FirebaseService.actualizarPeluquero(
                                            negocioId = negocioId!!,
                                            peluqueroId = peluqueroEditando!!.id,
                                            nombre = nombre,
                                            email = email,
                                            nuevaPassword = if (password.isNotBlank()) password else null,
                                            onSuccess = {
                                                showDialog.value = false
                                                FirebaseService.getPeluquerosDelNegocio(negocioId!!) { peluqueros = it }
                                                scope.launch {
                                                    scaffoldState.snackbarHostState.showSnackbar("Trabajador actualizado correctamente")
                                                }
                                            },
                                            onFailure = {
                                                scope.launch {
                                                    scaffoldState.snackbarHostState.showSnackbar("Error al actualizar trabajador: ${it.message}")
                                                }
                                            }
                                        )
                                    }
                                }) {
                                    Text("Guardar", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
