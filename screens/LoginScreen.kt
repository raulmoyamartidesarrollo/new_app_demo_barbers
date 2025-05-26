package com.github.jetbrains.rssreader.androidApp.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.R
import com.github.jetbrains.rssreader.androidApp.components.GoogleAuthUIClient
import com.github.jetbrains.rssreader.androidApp.utils.guardarTokenEnFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    // Google Auth
    val googleAuthUIClient = remember { GoogleAuthUIClient(context) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val intent = result.data
            CoroutineScope(Dispatchers.Main).launch {
                if (intent != null) {
                    val signInResult = googleAuthUIClient.signInWithIntent(intent)
                    if (signInResult.isSuccess) {
                        val userId = Firebase.auth.currentUser?.uid
                        val db = Firebase.firestore

                        if (userId != null) {
                            db.collection("usuarios").document(userId).get()
                                .addOnSuccessListener { userDocument ->
                                    if (!userDocument.exists()) {
                                        errorMessage = "No hay ninguna cuenta registrada con ese correo."
                                        showErrorDialog = true
                                        navController.navigate("register")
                                        return@addOnSuccessListener
                                    }

                                    val rol = userDocument.getString("rol") ?: "cliente"
                                    Log.d("RAUL", "📥 Login exitoso. Rol detectado: $rol")

                                    guardarTokenEnFirestore(userId, rol)
                                    when (rol) {
                                        "cliente" -> {
                                            db.collection("clientes").document(userId).get()
                                                .addOnSuccessListener { clientDoc ->
                                                    val idNegocio = clientDoc.getString("idnegocio") ?: ""
                                                    if (idNegocio.isEmpty()) {
                                                        navController.navigate("inicio_usuario") {
                                                            popUpTo(0) { inclusive = true }
                                                            launchSingleTop = true
                                                        }
                                                    } else {
                                                        navController.navigate("home_cliente") {
                                                            popUpTo(0) { inclusive = true }
                                                            launchSingleTop = true
                                                        }
                                                    }
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
                                    }
                                }
                                .addOnFailureListener {
                                    errorMessage = "Error comprobando si el usuario está registrado."
                                    showErrorDialog = true
                                }
                        } else {
                            errorMessage = "No se pudo obtener el ID del usuario de Google."
                            showErrorDialog = true
                        }
                    } else {
                        errorMessage = "Error al iniciar sesión con Google."
                        showErrorDialog = true
                    }
                } else {
                    errorMessage = "No se recibió respuesta de Google."
                    showErrorDialog = true
                }
            }
        }
    }

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
                text = "Inicia sesión para continuar",
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
                                            Log.d("RAUL", "📥 Login exitoso. Rol detectado: $rol → guardando token ahora...")


                                            guardarTokenEnFirestore(userId, rol)

                                            when (rol) {
                                                "cliente" -> {
                                                    db.collection("clientes").document(userId).get()
                                                        .addOnSuccessListener { clientDoc ->
                                                            val idNegocio = clientDoc.getString("idnegocio") ?: ""
                                                            if (idNegocio.isEmpty()) {
                                                                navController.navigate("inicio_usuario") {
                                                                    popUpTo(0) { inclusive = true }
                                                                    launchSingleTop = true
                                                                }
                                                            } else {
                                                                navController.navigate("home_cliente") {
                                                                    popUpTo(0) { inclusive = true }
                                                                    launchSingleTop = true
                                                                }
                                                            }
                                                        }
                                                }
                                                "peluquero" -> {
                                                    navController.navigate("home_admin?modoPeluquero=true") {
                                                        popUpTo(0) { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                }
                                                "superpeluquero" -> {
                                                    navController.navigate("home_admin?modoPeluquero=false") {
                                                        popUpTo(0) { inclusive = true }
                                                        launchSingleTop = true
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage = "Error verificando usuario: ${e.message}"
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
                                    else -> "Error al iniciar sesión. Intenta de nuevo."
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

            /*Button(
                onClick = {
                    focusManager.clearFocus()
                    CoroutineScope(Dispatchers.Main).launch {
                        googleAuthUIClient.signOut()
                        val intent = googleAuthUIClient.signIn()
                        if (intent == null) {
                            errorMessage = "No se pudo iniciar sesión con Google. Intenta más tarde."
                            showErrorDialog = true
                        } else {
                            launcher.launch(intent)
                        }
                    }
                },
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
            } */

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
