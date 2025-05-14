package com.github.jetbrains.rssreader.androidApp.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
data class NotificacionMultiplePush(
    val tokens: List<String>,
    val title: String,
    val body: String
)

suspend fun enviarNotificacionPushMultiple(
    client: HttpClient,
    tokens: List<String>,
    titulo: String,
    mensaje: String
) {
    val payload = NotificacionMultiplePush(
        tokens = tokens,
        title = titulo,
        body = mensaje
    )

    try {
        val response: HttpResponse = client.post("https://us-central1-fir-app-barbers.cloudfunctions.net/sendPushNotification") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }

        if (response.status != HttpStatusCode.OK) {
            println("❌ Error al enviar notificación: ${response.status} - ${response.bodyAsText()}")
        } else {
            println("✅ Notificación enviada correctamente.")
        }
    } catch (e: Exception) {
        println("❌ Excepción al enviar push: ${e.message}")
    }
}