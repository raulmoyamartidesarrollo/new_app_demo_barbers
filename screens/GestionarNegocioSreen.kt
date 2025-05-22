
package com.github.jetbrains.rssreader.androidApp.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.github.jetbrains.rssreader.androidApp.FirebaseService
import com.github.jetbrains.rssreader.androidApp.HorarioDia
import com.github.jetbrains.rssreader.androidApp.R
import com.github.jetbrains.rssreader.androidApp.components.CustomTextField
import com.github.jetbrains.rssreader.androidApp.components.DiaHorarioPicker
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun GestionarNegocioScreen(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val diasIniciales = listOf("L", "M", "X", "J", "V", "S", "D")

    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }

    val horarios: Map<String, MutableState<HorarioDia>> = remember {
        diasSemana.associateWith { mutableStateOf(HorarioDia()) }
    }

    var logoUri by remember { mutableStateOf<Uri?>(null) }
    val galeriaUris = remember { mutableStateListOf<Uri>() }

    var showLogoDialog by remember { mutableStateOf(false) }
    var showGaleriaDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val logoGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { logoUri = it }
    }

    val galeriaGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        galeriaUris.addAll(uris)
    }

    val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempCameraUri?.let {
                if (showLogoDialog) logoUri = it
                if (showGaleriaDialog) galeriaUris.add(it)
            }
        }
    }

    fun crearArchivoImagen(context: Context): Uri {
        val imagenFile = File.createTempFile("IMG_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", imagenFile)
    }

    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedDiaIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        FirebaseService.getDatosNegocio(
            onSuccess = { datos ->
                nombre = datos["nombre"] as? String ?: ""
                direccion = datos["direccion"] as? String ?: ""
                telefono = datos["telefono"] as? String ?: ""
                codigoPostal = datos["codigoPostal"] as? String ?: ""
                val horarioMap = datos["horario"] as? Map<String, Map<String, String>> ?: emptyMap()
                diasSemana.forEach { dia ->
                    val diaData = horarioMap[dia]
                    horarios[dia]?.value = HorarioDia(
                        aperturaManana = diaData?.get("aperturaManana") ?: "",
                        cierreManana = diaData?.get("cierreManana") ?: "",
                        aperturaTarde = diaData?.get("aperturaTarde") ?: "",
                        cierreTarde = diaData?.get("cierreTarde") ?: ""
                    )
                }
            },
            onError = { Log.e("FIREBASE", "Error cargando datos negocio") }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C2D3C))
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.height(100.dp)
            )

            TabRow(selectedTabIndex, backgroundColor = Color(0xFF1C2D3C), contentColor = Color.White) {
                listOf("Datos", "Horarios", "Imágenes").forEachIndexed { i, text ->
                    Tab(selected = selectedTabIndex == i, onClick = { selectedTabIndex = i }) {
                        Text(text)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> {
                    Text("Editar datos del negocio", color = Color.White, style = MaterialTheme.typography.h6)
                    Spacer(Modifier.height(12.dp))
                    CustomTextField(nombre, { nombre = it }, "Nombre del negocio")
                    CustomTextField(direccion, { direccion = it }, "Dirección")
                    CustomTextField(telefono, { telefono = it }, "Teléfono")
                    CustomTextField(codigoPostal, { codigoPostal = it }, "Código Postal")
                }
                1 -> {
                    Text("Horario habitual", color = Color.White)
                    ScrollableTabRow(selectedTabIndex = selectedDiaIndex, backgroundColor = Color(0xFF263544)) {
                        diasIniciales.forEachIndexed { index, letra ->
                            Tab(selected = selectedDiaIndex == index, onClick = { selectedDiaIndex = index }) {
                                Text(letra, fontSize = 14.sp)
                            }
                        }
                    }
                    val diaSeleccionado = diasSemana[selectedDiaIndex]
                    DiaHorarioPicker(horarios[diaSeleccionado]!!.value) {
                        horarios[diaSeleccionado]!!.value = it
                    }
                }
                2 -> {
                    Text("Imágenes", color = Color.White, style = MaterialTheme.typography.h6)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { showLogoDialog = true }) { Text("Seleccionar logo") }
                    logoUri?.let {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showGaleriaDialog = true }) { Text("Añadir imágenes a galería") }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        galeriaUris.forEach { uri ->
                            Box(modifier = Modifier.padding(end = 8.dp)) {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = "Imagen",
                                    modifier = Modifier
                                        .height(100.dp)
                                        .width(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar",
                                    tint = Color.Red,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .clickable { galeriaUris.remove(uri) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        val logoUrl = logoUri?.let { FirebaseService.subirImagenNegocio(it, "logo.jpg") } ?: ""
                        val urlsGaleria = galeriaUris.mapIndexed { i, uri ->
                            FirebaseService.subirImagenNegocio(uri, "galeria_$i.jpg")
                        }

                        FirebaseService.guardarDatosNegocio(
                            nombre, direccion, telefono, codigoPostal,
                            horarios.mapValues { it.value.value },
                            logoUrl = logoUrl,
                            galeria = urlsGaleria,
                            onSuccess = {
                                scope.launch { snackbarHostState.showSnackbar("Datos guardados correctamente") }
                            },
                            onFailure = {
                                scope.launch { snackbarHostState.showSnackbar("Error al guardar los datos") }
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF6680))
            ) {
                Text("Guardar cambios", color = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    navController.navigate("home_admin") {
                        popUpTo("gestionar_negocio") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Volver a la Home", color = Color.White)
            }
        }
    }

    if (showLogoDialog) {
        AlertDialog(
            onDismissRequest = { showLogoDialog = false },
            title = { Text("Seleccionar logo") },
            text = { Text("¿Desde dónde deseas seleccionar el logo?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoDialog = false
                    val uri = crearArchivoImagen(context)
                    tempCameraUri = uri
                    takePhotoLauncher.launch(uri)
                }) { Text("Abrir cámara") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLogoDialog = false
                    logoGalleryLauncher.launch("image/*")
                }) { Text("Desde galería") }
            }
        )
    }

    if (showGaleriaDialog) {
        AlertDialog(
            onDismissRequest = { showGaleriaDialog = false },
            title = { Text("Seleccionar imágenes") },
            text = { Text("¿Desde dónde deseas seleccionar imágenes?") },
            confirmButton = {
                TextButton(onClick = {
                    showGaleriaDialog = false
                    val uri = crearArchivoImagen(context)
                    tempCameraUri = uri
                    takePhotoLauncher.launch(uri)
                }) { Text("Abrir cámara") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showGaleriaDialog = false
                    galeriaGalleryLauncher.launch("image/*")
                }) { Text("Desde galería") }
            }
        )
    }
}
