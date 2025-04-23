package com.github.jetbrains.rssreader.androidApp.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatBotCliente() {
    var chatAbierto by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    val respuestas = remember { mutableStateListOf("Bot: Hola, soy tu asistente. ¿En qué puedo ayudarte?") }

    var mostrarMensajeInicial by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        delay(1500)
        mostrarMensajeInicial = true
        delay(4000)
        mostrarMensajeInicial = false
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (chatAbierto) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.08f))
                    .clickable(enabled = false) {}
            )
        }

        if (chatAbierto) {
            Card(
                backgroundColor = Color(0xFF1E1E1E).copy(alpha = 0.95f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .align(Alignment.BottomCenter)
                    .imePadding()
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tijeritas Asistente Virtual", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Cerrar", color = Color(0xFF00FF41), modifier = Modifier.clickable { chatAbierto = false })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        reverseLayout = true
                    ) {
                        items(respuestas.reversed()) { item ->
                            val isUser = item.startsWith("Tú:")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .background(
                                            color = if (isUser) Color(0xFF00FF41) else Color.DarkGray,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = item.removePrefix("Tú:").removePrefix("Bot:").trim(),
                                        color = if (isUser) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = mensaje,
                            onValueChange = { mensaje = it },
                            placeholder = { Text("Escribe tu mensaje...", color = Color.LightGray) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    coroutineScope.launch { procesarMensaje(mensaje, respuestas) }
                                    mensaje = ""
                                }
                            ),
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color.Transparent,
                                textColor = Color.White,
                                cursorColor = Color.White,
                                focusedIndicatorColor = Color(0xFF00FF41),
                                unfocusedIndicatorColor = Color(0xFF00FF41)
                            )
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch { procesarMensaje(mensaje, respuestas) }
                                mensaje = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF00FF41),
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Enviar")
                        }
                    }
                }
            }
        }

        if (!chatAbierto) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = mostrarMensajeInicial,
                    enter = fadeIn() + expandHorizontally(),
                ) {
                    Row(
                        modifier = Modifier
                            .background(Color.LightGray.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "Asistente",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Soy tu asistente virtual, ¿en qué puedo ayudarte?",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                FloatingActionButton(
                    onClick = { chatAbierto = true },
                    backgroundColor = Color(0xFF00FF41),
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat")
                }
            }
        }
    }
}

private fun procesarMensaje(mensaje: String, respuestas: MutableList<String>) {
    if (mensaje.isBlank()) return

    respuestas.add("Tú: $mensaje")

    val mensajeLower = mensaje.lowercase()
    val respuesta = when {
        mensajeLower.contains("cita") || mensajeLower.contains("reserva") || mensajeLower.contains("quiero pedir") -> {
            "Perfecto, ¿qué servicio deseas reservar? (Ej: corte, barba...)"
        }
        mensajeLower.contains("corte") || mensajeLower.contains("barba") -> {
            "¿Qué día te gustaría venir?"
        }
        mensajeLower.contains("mañana") || mensajeLower.contains("viernes") || mensajeLower.contains("lunes") -> {
            "¿Y a qué hora prefieres? Puedes indicar por ejemplo 'a las 10:30'"
        }
        mensajeLower.contains("10") || mensajeLower.contains("11") || mensajeLower.contains("16") -> {
            "¡Genial! Estoy procesando tu reserva..."
            // Aquí puedes llamar a la función de crear cita real si tienes contexto
        }
        else -> {
            "No estoy seguro de cómo ayudarte con eso todavía. Prueba escribiendo que quieres reservar una cita."
        }
    }

    respuestas.add("Bot: $respuesta")
}