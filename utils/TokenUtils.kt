package com.github.jetbrains.rssreader.androidApp.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

//Function URL (sendPushNotification(us-central1)): https://us-central1-fir-app-barbers.cloudfunctions.net/sendPushNotification
fun guardarTokenEnFirestore(userId: String, rol: String) {
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "❌ No se pudo obtener el token", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "✅ Token FCM obtenido: $token")

            val db = FirebaseFirestore.getInstance()
            val docRef = when (rol) {
                "cliente" -> db.collection("clientes").document(userId)
                else -> db.collection("usuarios").document(userId)
            }

            docRef.update("token", token)
                .addOnSuccessListener {
                    Log.d("FCM", "📥 Token guardado correctamente en Firestore")
                }
                .addOnFailureListener {
                    Log.e("FCM", "❌ Error al guardar token: ${it.message}")
                }
        }
}