package com.github.jetbrains.rssreader.androidApp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jetbrains.rssreader.androidApp.utils.createHttpClient
import com.github.jetbrains.rssreader.androidApp.utils.enviarNotificacionPushMultiple
import kotlinx.coroutines.launch

@Composable
fun TestNotificacionScreen() {
    val coroutineScope = rememberCoroutineScope()
    val httpClient = remember { createHttpClient() }

    val tokenDestino = "ecxjOVvyTOmJWB3PfkI5MD:APA91bHiRK3dtav0ALQTzayuJsSx3QevJYLSyB40Nhn26qP87hSf2vAdiIpUkL7Sxa4ONYfJWAroJYAvMGfUiSe4k-U40QttoOL7yuStAV_w9G8REC61NNw"


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Test Notificación Push", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            coroutineScope.launch {
                enviarNotificacionPushMultiple(
                    client = httpClient,
                    tokens = listOf(tokenDestino),
                    titulo = "🎉 Push directa",
                    mensaje = "Probando si llega la notificación"
                )
            }
        }) {
            Text("Enviar notificación")
        }
    }
}