package com.github.jetbrains.rssreader.androidApp.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.FirebaseService
import kotlinx.coroutines.launch

@Composable
fun MiCuentaAdminScreen(navController: NavHostController) {
    val user = FirebaseService.getCurrentUser()
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            FirebaseService.getUserData(uid,
                onSuccess = { data ->
                    nombre = data["nombre"] as? String ?: ""
                    apellidos = data["apellidos"] as? String ?: ""
                    email = data["email"] as? String ?: ""
                },
                onFailure = { Log.e("Firebase", "Error cargando datos del usuario: ${it.message}") }
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C2D3C))
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Mi cuenta", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email", color = Color.White) },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    disabledTextColor = Color.White,
                    disabledBorderColor = Color.LightGray
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Nueva contraseña", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña", color = Color.White) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Button(
                onClick = {
                    if (password != confirmPassword) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Las contraseñas no coinciden")
                        }
                        return@Button
                    }
                    FirebaseService.actualizarDatosUsuario(
                        nombre = nombre,
                        apellidos = apellidos,
                        onSuccess = {
                            if (password.isNotBlank()) {
                                FirebaseService.updatePassword(password,
                                    onSuccess = {
                                        scope.launch { snackbarHostState.showSnackbar("Datos actualizados correctamente") }
                                    },
                                    onFailure = {
                                        scope.launch { snackbarHostState.showSnackbar("Error al actualizar contraseña") }
                                    })
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Datos actualizados correctamente") }
                            }
                        },
                        onFailure = {
                            scope.launch { snackbarHostState.showSnackbar("Error al guardar cambios") }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF6680))
            ) {
                Text("Guardar cambios", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    navController.navigate("home_admin") {
                        popUpTo("mi_cuenta") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Volver al inicio", color = Color.White)
            }
        }
    }
}