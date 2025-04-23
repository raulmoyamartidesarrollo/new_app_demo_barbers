package com.github.jetbrains.rssreader.androidApp.screens

import androidx.navigation.NavHostController
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.github.jetbrains.rssreader.androidApp.R
import com.github.jetbrains.rssreader.androidApp.FirebaseService

@Composable
fun RegisterScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nombreNegocio by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var showAlert by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val inputColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        textColor = Color.Black,
        placeholderColor = Color.DarkGray,
        backgroundColor = Color(0xFFDCF1FF)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C2D3C))
            .padding(WindowInsets.systemBars.asPaddingValues())
            .pointerInput(Unit) {
                detectTapGestures { focusManager.clearFocus() }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )

            Text("Crear Cuenta", fontSize = 22.sp, color = Color.White)

            if (tipoUsuarioGlobal == "empresa") {
                OutlinedTextField(
                    value = nombreNegocio,
                    onValueChange = { nombreNegocio = it },
                    placeholder = { Text("Nombre del negocio") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = inputColors
                )
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = { Text("Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = inputColors
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Correo electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = inputColors
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Contraseña") },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = inputColors
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Confirmar") },
                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                            Icon(
                                imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = inputColors
                )
            }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    alertMessage = null

                    if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || phone.isBlank()) {
                        alertMessage = "Por favor completa todos los campos."
                        showAlert = true
                        isSuccess = false
                        return@Button
                    }

                    if (password != confirmPassword) {
                        alertMessage = "Las contraseñas no coinciden."
                        showAlert = true
                        isSuccess = false
                        return@Button
                    }

                    if (tipoUsuarioGlobal == "empresa" && nombreNegocio.isBlank()) {
                        alertMessage = "El nombre del negocio es obligatorio."
                        showAlert = true
                        isSuccess = false
                        return@Button
                    }

                    isLoading = true

                    FirebaseService.registerWithEmailPassword(
                        email,
                        password,
                        onSuccess = { user ->
                            val uid = user.uid
                            if (tipoUsuarioGlobal == "empresa") {
                                FirebaseService.createNegocio(
                                    nombre = nombreNegocio,
                                    telefono = phone,
                                    onSuccess = { negocioId ->
                                        FirebaseService.createUserData(
                                            uid = uid,
                                            email = email,
                                            telefono = phone,
                                            rol = "superpeluquero",
                                            negocioId = negocioId,
                                            onSuccess = {
                                                isLoading = false
                                                alertMessage = "Registro completado con éxito."
                                                isSuccess = true
                                                showAlert = true
                                            },
                                            onFailure = {
                                                isLoading = false
                                                alertMessage = "Error al guardar usuario: ${it.message}"
                                                isSuccess = false
                                                showAlert = true
                                            }
                                        )
                                    },
                                    onFailure = {
                                        isLoading = false
                                        alertMessage = "Error al crear negocio: ${it.message}"
                                        isSuccess = false
                                        showAlert = true
                                    }
                                )
                            } else {
                                FirebaseService.createUserData(
                                    uid = uid,
                                    email = email,
                                    telefono = phone,
                                    rol = "cliente",
                                    onSuccess = {
                                        isLoading = false
                                        alertMessage = "Registro completado con éxito."
                                        isSuccess = true
                                        showAlert = true
                                    },
                                    onFailure = {
                                        isLoading = false
                                        alertMessage = "Error al guardar usuario: ${it.message}"
                                        isSuccess = false
                                        showAlert = true
                                    }
                                )
                            }
                        },
                        onFailure = {
                            isLoading = false
                            alertMessage = "Error al registrar usuario: ${it.message}"
                            isSuccess = false
                            showAlert = true
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFFF6680),
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Crear cuenta", fontWeight = FontWeight.Bold)
                }
            }

            if (showAlert && alertMessage != null) {
                AlertDialog(
                    onDismissRequest = { showAlert = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showAlert = false
                            if (isSuccess) {
                                navController.popBackStack()
                                navController.navigate("login")
                            }
                        }) {
                            Text("Aceptar")
                        }
                    },
                    title = { Text(if (isSuccess) "Registro exitoso" else "Error") },
                    text = { Text(alertMessage!!) },
                    backgroundColor = Color.White,
                    contentColor = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFFB0D4E6))) {
                        append("¿Seleccionaste mal tu rol? ")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFFFF6680))) {
                        append("Volver")
                    }
                },
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    navController.popBackStack()
                    navController.navigate("start")
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}