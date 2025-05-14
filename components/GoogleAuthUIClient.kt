package com.github.jetbrains.rssreader.androidApp.components

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class GoogleAuthUIClient(
    private val context: Context
) {
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)

    private val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId("402857061695-402a0bhoehf3urfqj0molqhavgq6j3q7.apps.googleusercontent.com") // ID de cliente web
                .setFilterByAuthorizedAccounts(false) // Importante: permitir todas las cuentas
                .build()
        )
        .setAutoSelectEnabled(false) // Importante: forzar el selector de cuentas
        .build()

    suspend fun signIn(): IntentSenderRequest? {
        Log.d("GOOGLE_SIGN_IN", "🔵 Iniciando signIn()...")
        return try {
            val result = oneTapClient.beginSignIn(signInRequest).await()
            Log.d("GOOGLE_SIGN_IN", "✅ IntentSender obtenido: mostrando selector de cuentas")
            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
        } catch (e: Exception) {
            Log.e("GOOGLE_SIGN_IN", "❌ Error en beginSignIn: ${e.message}", e)
            null
        }
    }

    suspend fun signInWithIntent(intent: Intent): Result<Unit> {
        Log.d("GOOGLE_SIGN_IN", "🔵 Iniciando signInWithIntent()...")
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val idToken = credential.googleIdToken
            val email = credential.id

            Log.d("GOOGLE_SIGN_IN", "📧 Email: $email")
            Log.d("GOOGLE_SIGN_IN", "🔐 ID Token recibido: ${idToken != null}")

            if (idToken == null) {
                Log.e("GOOGLE_SIGN_IN", "⚠️ Token de Google es null")
                return Result.failure(Exception("Token de Google es null"))
            }

            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            Firebase.auth.signInWithCredential(authCredential).await()
            Log.d("GOOGLE_SIGN_IN", "✅ Login en Firebase completado con éxito")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GOOGLE_SIGN_IN", "❌ Error en signInWithIntent: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        Log.d("GOOGLE_SIGN_IN", "🔵 Cerrando sesión...")
        try {
            oneTapClient.signOut().await()
            Firebase.auth.signOut()
            Log.d("GOOGLE_SIGN_IN", "✅ Sesión cerrada correctamente")
        } catch (e: Exception) {
            Log.e("GOOGLE_SIGN_IN", "❌ Error al cerrar sesión: ${e.message}", e)
        }
    }
}