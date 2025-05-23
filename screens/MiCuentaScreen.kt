package com.github.jetbrains.rssreader.androidApp.screens

import android.Manifest
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalPermissionsApi::class)
@Composable

fun MiCuentaScreen(navController: NavHostController) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var fotoPerfilUrl by remember { mutableStateOf("") }

    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var passwordActual by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var passwordRepetida by remember { mutableStateOf("") }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPermissionExplanation by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val fondo = Color(0xFF1C2D3C)
    val acento = Color(0xFFFF6680)
    val verde = Color(0xFF00FF41)
    val texto = Color.White

    val inputColors = TextFieldDefaults.outlinedTextFieldColors(
        backgroundColor = Color(0xFFDCF1FF),
        textColor = Color.Black,
        placeholderColor = Color.Black,
        cursorColor = Color.Black,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent
    )

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        if (it != null) profileBitmap = it
    }

    // Cargar datos del usuario al iniciar
    LaunchedEffect(Unit) {

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userRef = FirebaseFirestore.getInstance().collection("clientes").document(it.uid)
            userRef.get().addOnSuccessListener { doc ->
                nombre = doc.getString("nombre") ?: ""
                apellidos = doc.getString("apellidos") ?: ""
                telefono = doc.getString("telefono") ?: ""
                fotoPerfilUrl = doc.getString("fotoPerfil") ?: ""
            }
        }
    }

    ScaffoldCliente(navController = navController) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(paddingValues)
                .padding(WindowInsets.systemBars.asPaddingValues())
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
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Mi Cuenta", fontSize = 24.sp, color = texto, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                    ) {
                        when {
                            profileBitmap != null -> {
                                Image(
                                    bitmap = profileBitmap!!.asImageBitmap(),
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            fotoPerfilUrl.isNotBlank() -> {
                                // Requiere Coil
                                androidx.compose.foundation.Image(
                                    painter = coil.compose.rememberAsyncImagePainter(fotoPerfilUrl),
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_old),
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .zIndex(2f)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.8f))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {
                                if (!cameraPermissionState.status.isGranted) showPermissionExplanation = true else cameraLauncher.launch()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Cambiar foto", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Nombre y Apellidos", fontSize = 18.sp, color = texto, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, placeholder = { Text("Nombre") }, modifier = Modifier.weight(1f), colors = inputColors)
                    OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, placeholder = { Text("Apellidos") }, modifier = Modifier.weight(1f), colors = inputColors)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Teléfono", fontSize = 18.sp, color = texto, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, placeholder = { Text("Teléfono") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), colors = inputColors)

                Spacer(modifier = Modifier.height(24.dp))
                Text("Cambiar contraseña", fontSize = 18.sp, color = texto, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = passwordActual, onValueChange = { passwordActual = it }, placeholder = { Text("Contraseña actual") }, modifier = Modifier.fillMaxWidth(), colors = inputColors)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(value = passwordNueva, onValueChange = { passwordNueva = it }, placeholder = { Text("Nueva") }, modifier = Modifier.weight(1f), colors = inputColors)
                    OutlinedTextField(value = passwordRepetida, onValueChange = { passwordRepetida = it }, placeholder = { Text("Repetir") }, modifier = Modifier.weight(1f), colors = inputColors)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = {
                        val user = FirebaseAuth.getInstance().currentUser ?: return@Button
                        val updates = mutableMapOf<String, Any>()
                        val firestore = FirebaseFirestore.getInstance()
                        val userRef = firestore.collection("clientes").document(user.uid)
                        if (nombre.isNotBlank()) updates["nombre"] = nombre
                        if (apellidos.isNotBlank()) updates["apellidos"] = apellidos
                        if (telefono.isNotBlank()) updates["telefono"] = telefono
                        if (updates.isNotEmpty()) {
                            userRef.update(updates).addOnSuccessListener { showSuccessDialog = true }.addOnFailureListener { Log.e("MiCuenta", "Error: ${it.message}") }
                        }
                        if (passwordNueva.isNotBlank() && passwordNueva == passwordRepetida) {
                            user.updatePassword(passwordNueva).addOnSuccessListener { showSuccessDialog = true }.addOnFailureListener { Log.e("MiCuenta", "Error: ${it.message}") }
                        }
                        profileBitmap?.let { bitmap ->
                            val baos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val data = baos.toByteArray()
                            val ref = FirebaseStorage.getInstance().reference.child("clientes/${user.uid}/fotoPerfil.jpg")
                            ref.putBytes(data).addOnSuccessListener {
                                ref.downloadUrl.addOnSuccessListener { uri ->
                                    userRef.update("fotoPerfil", uri.toString())
                                        .addOnSuccessListener {
                                            // También actualizar en la colección 'usuarios'
                                            FirebaseFirestore.getInstance().collection("clientes").document(user.uid)
                                                .update("fotoPerfil", uri.toString())
                                                .addOnSuccessListener {
                                                    showSuccessDialog = true
                                                    Log.d("MiCuenta", "Foto de perfil actualizada en ambas colecciones")
                                                }
                                                .addOnFailureListener {
                                                    Log.e("MiCuenta", "Error al actualizar en usuarios: ${it.message}")
                                                }
                                        }
                                        .addOnFailureListener {
                                            Log.e("MiCuenta", "Error al actualizar fotoPerfil en clientes: ${it.message}")
                                        }
                                }
                            }.addOnFailureListener { Log.e("MiCuenta", "Error imagen: ${it.message}") }
                        }
                    }, colors = ButtonDefaults.buttonColors(backgroundColor = verde), modifier = Modifier.weight(1f)) {
                        Text("Guardar cambios", color = Color.Black)
                    }
                    Button(onClick = {
                        navController.navigate("home_cliente") {
                            popUpTo("mi_cuenta") { inclusive = true }
                            launchSingleTop = true
                        }
                    }, colors = ButtonDefaults.buttonColors(backgroundColor = acento), modifier = Modifier.weight(1f)) {
                        Text("Volver a inicio", color = Color.White)
                    }
                }
            }

            if (showPermissionExplanation) {
                AlertDialog(onDismissRequest = { showPermissionExplanation = false }, title = { Text("Permiso necesario") }, text = { Text("Para cambiar tu foto de perfil necesitamos acceder a la cámara.") }, confirmButton = {
                    TextButton(onClick = {
                        showPermissionExplanation = false
                        cameraPermissionState.launchPermissionRequest()
                    }) {
                        Text("Permitir")
                    }
                }, dismissButton = {
                    TextButton(onClick = { showPermissionExplanation = false }) {
                        Text("Cancelar")
                    }
                })
            }

            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
                    title = { Text("🎉 ¡Cambios guardados!") },
                    text = { Text("Tu información se ha actualizado correctamente.") },
                    confirmButton = {
                        TextButton(onClick = { showSuccessDialog = false }) {
                            Text("Aceptar")
                        }
                    },
                    backgroundColor = Color.White,
                    contentColor = Color.Black
                )
            }

            //ChatBotCliente()
        }
    }
}
