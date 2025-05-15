package com.github.jetbrains.rssreader.androidApp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.github.jetbrains.rssreader.androidApp.ui.theme.PeluqueriaAppTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class AppActivity : ComponentActivity() {

    companion object {
        var pendingNotification: Pair<String, String>? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 👇 Guarda los datos de la notificación si llegan desde intent
        val title = intent.getStringExtra("notif_title")
        val body = intent.getStringExtra("notif_body")
        if (!title.isNullOrEmpty() && !body.isNullOrEmpty()) {
            pendingNotification = title to body
        }

        // ✅ Crear canal de notificaciones si API >= 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "mi_canal_notificaciones", // ID del canal
                "Notificaciones de reservas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reservas creadas o modificadas"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // 🔥 Obtener token FCM y guardarlo en Firestore
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", "🔥 Token FCM (manual): $token")

                val user = FirebaseService.getCurrentUser()
                if (user != null) {
                    val userId = user.uid
                    val db = FirebaseFirestore.getInstance()

                    db.collection("usuarios").document(userId).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                db.collection("usuarios").document(userId)
                                    .update("token", token)
                                    .addOnSuccessListener {
                                        Log.d("FCM_TOKEN", "✅ Token guardado en 'usuarios'")
                                    }
                            } else {
                                db.collection("clientes").document(userId)
                                    .update("token", token)
                                    .addOnSuccessListener {
                                        Log.d("FCM_TOKEN", "✅ Token guardado en 'clientes'")
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Log.e("FCM_TOKEN", "❌ Error al buscar el usuario: ${it.message}")
                        }
                }
            } else {
                Log.e("FCM_TOKEN", "❌ Error al obtener token", task.exception)
            }
        }

        // 👇 Lanzar UI
        setContent {
            PeluqueriaAppTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}