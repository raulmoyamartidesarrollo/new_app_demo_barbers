package com.github.jetbrains.rssreader.androidApp.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.github.jetbrains.rssreader.androidApp.FirebaseService
import com.github.jetbrains.rssreader.androidApp.HorarioDia
import com.github.jetbrains.rssreader.androidApp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.jetbrains.rssreader.androidApp.components.CustomTextField
import com.github.jetbrains.rssreader.androidApp.components.DiaHorarioPicker
import com.github.jetbrains.rssreader.androidApp.components.DropdownTimePickerTextField

@Composable
fun GestionarNegocioScreen(navController: NavHostController) {
    val focusManager = LocalFocusManager.current
    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val diasIniciales = listOf("L", "M", "X", "J", "V", "S", "D")
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }

    val horarios: Map<String, MutableState<HorarioDia>> = remember {
        diasSemana.associateWith { mutableStateOf(HorarioDia()) }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Datos", "Horarios")
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
                    val horarioDia = HorarioDia(
                        aperturaManana = diaData?.get("aperturaManana") ?: "",
                        cierreManana = diaData?.get("cierreManana") ?: "",
                        aperturaTarde = diaData?.get("aperturaTarde") ?: "",
                        cierreTarde = diaData?.get("cierreTarde") ?: ""
                    )
                    horarios[dia]?.value = horarioDia
                }
            },
            onError = { Log.e("FIREBASE", "No se pudieron cargar los datos del negocio") }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C2D3C))
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
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
                modifier = Modifier.height(100.dp).padding(bottom = 16.dp)
            )

            TabRow(selectedTabIndex = selectedTabIndex, backgroundColor = Color(0xFF1C2D3C), contentColor = Color.White) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }) {
                        Text(title)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> {
                    Text("Editar datos del negocio", color = Color.White, style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(12.dp))
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
                    val horario = horarios[diaSeleccionado]!!.value
                    DiaHorarioPicker(horario) { horarios[diaSeleccionado]!!.value = it }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        FirebaseService.guardarDatosNegocio(
                            nombre, direccion, telefono, codigoPostal,
                            horarios.mapValues { it.value.value },
                            logoUrl = "",
                            galeria = emptyList(),
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Datos guardados correctamente")
                                }
                            },
                            onFailure = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error al guardar los datos")
                                }
                            }
                        )
                    }
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
}
