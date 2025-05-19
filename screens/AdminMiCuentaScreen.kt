package com.github.jetbrains.rssreader.androidApp.screens

//noinspection UsingMaterialAndMaterial3Libraries
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.FirebaseService
import kotlinx.coroutines.launch

@Composable
fun MiCuentaAdminScreen(navController: NavHostController, modoPeluquero: Boolean = false){
    val user = FirebaseService.getCurrentUser()
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current


    var rol by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        user?.uid?.let { uid ->
            FirebaseService.getUserRole(
                uid,
                onSuccess = { fetchedRol -> rol = fetchedRol },
                onFailure = { rol = null }
            )
        }
    }

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
                    navController.navigate("home_admin?modoPeluquero=$modoPeluquero") {
                        popUpTo("admin_mi_cuenta_Screen") { inclusive = true }
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