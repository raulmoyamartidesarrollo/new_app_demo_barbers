package com.github.jetbrains.rssreader.androidApp.screens

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.R
import com.google.accompanist.permissions.*
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import com.github.jetbrains.rssreader.androidApp.components.ChatBotCliente

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MiCuentaScreen(navController: NavHostController) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var nombre by remember { mutableStateOf("Miguel") }
    var apellidos by remember { mutableStateOf("Pérez García") }
    var telefono by remember { mutableStateOf("666555444") }
    var passwordActual by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var passwordRepetida by remember { mutableStateOf("") }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val fondo = Color(0xFF1C2D3C)

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        if (it != null) profileBitmap = it
    }

    var showPermissionExplanation by remember { mutableStateOf(false) }

    val inputColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = Color(0xFF00FF41),
        unfocusedBorderColor = Color(0xFF00FF41),
        textColor = Color.White,
        placeholderColor = Color.LightGray,
        cursorColor = Color.White
    )

    ScaffoldCliente(navController = navController) { paddingValues ->
        // Fondo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo) // 👈 Fondo con color sólido
                .padding(paddingValues)
                .padding(WindowInsets.systemBars.asPaddingValues())
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(WindowInsets.systemBars.asPaddingValues()) // SafeArea
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        })
                    }
            ) {
                Text("Mi Cuenta", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .clickable {
                            if (!cameraPermissionState.status.isGranted) {
                                showPermissionExplanation = true
                            } else {
                                cameraLauncher.launch()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap!!.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.logo_old),
                            contentDescription = "Foto de perfil por defecto",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Cambiar foto",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.7f), shape = CircleShape)
                            .padding(6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Nombre y Apellidos", fontSize = 18.sp, color = Color.White)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        placeholder = { Text("Nombre", color = Color.LightGray) },
                        modifier = Modifier.weight(1f),
                        colors = inputColors
                    )
                    OutlinedTextField(
                        value = apellidos,
                        onValueChange = { apellidos = it },
                        placeholder = { Text("Apellidos", color = Color.LightGray) },
                        modifier = Modifier.weight(1f),
                        colors = inputColors
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Teléfono", fontSize = 18.sp, color = Color.White)
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    placeholder = { Text("Teléfono", color = Color.LightGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputColors
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("Cambiar contraseña", fontSize = 18.sp, color = Color.White)

                OutlinedTextField(
                    value = passwordActual,
                    onValueChange = { passwordActual = it },
                    placeholder = { Text("Contraseña actual", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputColors
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = passwordNueva,
                        onValueChange = { passwordNueva = it },
                        placeholder = { Text("Nueva", color = Color.LightGray) },
                        modifier = Modifier.weight(1f),
                        colors = inputColors
                    )
                    OutlinedTextField(
                        value = passwordRepetida,
                        onValueChange = { passwordRepetida = it },
                        placeholder = { Text("Repetir", color = Color.LightGray) },
                        modifier = Modifier.weight(1f),
                        colors = inputColors
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { /* Guardar en Firebase */ },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00FF41), contentColor = Color.Black),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar cambios")
                    }
                    Button(
                        onClick = {
                            navController.navigate("home_cliente") {
                                popUpTo("mi_cuenta") { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Volver a inicio")
                    }
                }
            }

            if (showPermissionExplanation) {
                AlertDialog(
                    onDismissRequest = { showPermissionExplanation = false },
                    title = { Text("Permiso necesario") },
                    text = { Text("Para cambiar tu foto de perfil necesitamos acceder a la cámara. ¿Deseas permitirlo?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showPermissionExplanation = false
                                cameraPermissionState.launchPermissionRequest()
                            }
                        ) {
                            Text("Permitir")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPermissionExplanation = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            ChatBotCliente()
        }
    }
}