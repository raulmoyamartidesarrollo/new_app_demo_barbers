package com.github.jetbrains.rssreader.androidApp.screens

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.github.jetbrains.rssreader.androidApp.R
import com.google.firebase.auth.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val auth = remember { FirebaseAuth.getInstance() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C2D3C))
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (tipoUsuarioGlobal == "empresa")
                    "Inicia sesión para gestionar tu negocio"
                else
                    "Inicia sesión para poder reservar tu cita de forma fácil y cómoda",
                color = Color(0xFFB0D4E6),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("username", color = Color.DarkGray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFDCF1FF),
                    textColor = Color.Black,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("password", color = Color.DarkGray) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFDCF1FF),
                    textColor = Color.Black,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Text(
                text = "¿Has olvidado tu contraseña?",
                color = Color(0xFFFF6680),
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp, bottom = 16.dp)
                    .clickable { navController.navigate("forgot_password") }
            )

            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Por favor completa todos los campos."
                        showErrorDialog = true
                        return@Button
                    }

                    isLoading = true
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                val db = Firebase.firestore

                                if (userId != null) {
                                    db.collection("usuarios").document(userId).get()
                                        .addOnSuccessListener { userDocument ->
                                            val rol = userDocument.getString("rol") ?: "cliente"

                                            when (rol) {
                                                "cliente" -> {
                                                    // ✅ Ahora buscamos el idNegocio en clientes
                                                    db.collection("clientes").document(userId).get()
                                                        .addOnSuccessListener { clientDoc ->
                                                            val idNegocio = clientDoc.getString("idnegocio") ?: ""
                                                            if (idNegocio.isEmpty()) {
                                                                // ➡️ No tiene barbería favorita ➔ ir a seleccionar barbería
                                                                navController.navigate("inicio_usuario") {
                                                                    popUpTo(0) { inclusive = true }
                                                                    launchSingleTop = true
                                                                }
                                                            } else {
                                                                // ➡️ Ya tiene barbería ➔ ir a home_cliente
                                                                navController.navigate("home_cliente") {
                                                                    popUpTo(0) { inclusive = true }
                                                                    launchSingleTop = true
                                                                }
                                                            }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Log.e("LOGIN_FIRESTORE", "Error leyendo cliente: ${e.message}")
                                                            errorMessage = "Error verificando cliente."
                                                            showErrorDialog = true
                                                        }
                                                }
                                                "peluquero" -> {
                                                    navController.navigate("home_peluquero") {
                                                        popUpTo(0) { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                }
                                                "superpeluquero" -> {
                                                    navController.navigate("home_admin") {
                                                        popUpTo(0) { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                }
                                                else -> {
                                                    navController.navigate("start") {
                                                        popUpTo(0) { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("LOGIN_FIRESTORE", "Error leyendo usuario: ${e.message}")
                                            errorMessage = "Error verificando usuario."
                                            showErrorDialog = true
                                        }
                                } else {
                                    errorMessage = "No se pudo obtener el ID del usuario."
                                    showErrorDialog = true
                                }
                            } else {
                                val exception = task.exception
                                errorMessage = when (exception) {
                                    is FirebaseAuthInvalidUserException -> "Usuario no registrado en el sistema."
                                    is FirebaseAuthInvalidCredentialsException -> "Contraseña incorrecta."
                                    else -> {
                                        Log.e("LOGIN_ERROR", "Error al iniciar sesión: ${exception?.javaClass?.name} - ${exception?.message}")
                                        "Error al iniciar sesión. Intenta de nuevo."
                                    }
                                }
                                showErrorDialog = true
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFFFF6680),
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Login", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { /* TODO: Google Sign In */ },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/53/Google_%22G%22_Logo.svg/512px-Google_%22G%22_Logo.svg.png",
                        contentDescription = "Google icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar sesión con Google")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row {
                Text("¿No tienes cuenta? ", color = Color.LightGray)
                Text(
                    "Crea una ahora",
                    color = Color(0xFFFF6680),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate("register")
                    }
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
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("Aceptar")
                    }
                },
                title = { Text("Error de autenticación") },
                text = { Text(errorMessage) },
                backgroundColor = Color.White,
                contentColor = Color.Black
            )
        }
    }
}