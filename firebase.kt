package com.github.jetbrains.rssreader.androidApp

import com.github.jetbrains.rssreader.androidApp.models.Barberia
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDate
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HorarioDia(
    val aperturaManana: String = "",
    val cierreManana: String = "",
    val aperturaTarde: String = "",
    val cierreTarde: String = ""
)

data class Peluquero(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val email: String = "",
    val fotoUrl: String? = null
)

data class Cita(
    val id: String = "", // <-- Esta es la clave
    val idCliente: String = "",
    val idServicio: String = "",
    val nombreCliente: String = "",
    val idPeluquero: String = "",
    val servicio: String = "",
    val fecha: String = "",
    val hora: String = "",
    val estado: String = ""
)

data class Cliente(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val email: String = ""
)

data class DatosNegocio(
    val nombre: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val codigoPostal: String = "",
    val horario: Map<String, HorarioDia> = emptyMap()
)

object FirebaseService {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun logout() {
        auth.signOut()
    }

    fun getNegocioIdActual(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val currentUser = auth.currentUser ?: return onFailure(Exception("Usuario no autenticado"))
        firestore.collection("usuarios").document(currentUser.uid).get()
            .addOnSuccessListener { userDoc ->
                val negocioId = userDoc.getString("negocioId")
                if (!negocioId.isNullOrEmpty()) onSuccess(negocioId)
                else onFailure(Exception("Negocio no encontrado"))
            }
            .addOnFailureListener(onFailure)
    }

    fun getPeluquerosDelNegocio(negocioId: String, onResult: (List<Peluquero>) -> Unit) {
        firestore.collection("negocios").document(negocioId)
            .collection("peluqueros").get()
            .addOnSuccessListener { result ->
                val peluqueros = result.map {
                    Peluquero(
                        id = it.id,
                        nombre = it.getString("nombre") ?: "",
                        apellidos = it.getString("apellidos") ?: "",
                        email = it.getString("email") ?: "",
                        fotoUrl = it.getString("fotoPerfil")
                    )
                }
                onResult(peluqueros)
            }
    }

    fun getClientesDelNegocio(negocioId: String, onResult: (List<Cliente>) -> Unit) {
        firestore.collection("usuarios")
            .whereEqualTo("rol", "cliente")
            .whereEqualTo("negocioId", negocioId)
            .get()
            .addOnSuccessListener { result ->
                val clientes = result.map {
                    Cliente(
                        id = it.id,
                        nombre = it.getString("nombre") ?: "",
                        apellidos = it.getString("apellidos") ?: "",
                        email = it.getString("email") ?: ""
                    )
                }
                onResult(clientes)
            }
    }

    fun getCitasPorPeluquero(negocioId: String, peluqueroId: String, onResult: (List<Cita>) -> Unit) {
        firestore.collection("negocios").document(negocioId)
            .collection("reservas")
            .whereEqualTo("peluqueroId", peluqueroId).get()
            .addOnSuccessListener { result ->
                val citas = result.map {
                    Cita(
                        nombreCliente = it.getString("nombreCliente") ?: "",
                        servicio = it.getString("servicio") ?: "",
                        fecha = it.getString("fecha") ?: "",
                        hora = it.getString("hora") ?: "",
                        estado = it.getString("estado") ?: ""
                    )
                }
                onResult(citas)
            }
    }

    fun getReservasDelNegocio(
        negocioId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("negocios")
            .document(negocioId)
            .collection("reservas")
            .get()
            .addOnSuccessListener { snapshot ->
                val reservas = snapshot.documents.mapNotNull { it.data }
                onSuccess(reservas)
            }
            .addOnFailureListener(onFailure)
    }

    fun crearReserva(
        negocioId: String,
        idPeluquero: String,
        idServicio: String,
        idCliente: String,
        fecha: String,
        hora: String,
        estado: String = "pendiente",
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val reservaData = hashMapOf(
            "idPeluquero" to idPeluquero,
            "idServicio" to idServicio,
            "idCliente" to idCliente,
            "fecha" to fecha,
            "hora" to hora,
            "estado" to estado
        )

        firestore.collection("negocios").document(negocioId)
            .collection("reservas")
            .add(reservaData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun subirImagenNegocio(uri: Uri, tipo: String, onSuccess: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val nombreArchivo = "$tipo/${System.currentTimeMillis()}.jpg"
        val fileRef = storageRef.child(nombreArchivo)

        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri -> onSuccess(uri.toString()) }
            }
            .addOnFailureListener { e -> Log.e("Firebase", "Error subiendo imagen: ${e.message}") }
    }

    fun getServiciosNegocio(
        negocioId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("negocios").document(negocioId)
            .collection("servicios").get()
            .addOnSuccessListener { result ->
                val servicios = result.documents.mapNotNull { doc ->
                    doc.data?.toMutableMap()?.apply { put("id", doc.id) }
                }
                onSuccess(servicios)
            }
            .addOnFailureListener(onFailure)
    }

    fun crearPeluquero(
        negocioId: String,
        nombre: String,
        apellidos: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener onFailure(Exception("UID no encontrado"))
                val data = hashMapOf(
                    "nombre" to nombre,
                    "apellidos" to apellidos,
                    "email" to email,
                    "rol" to "peluquero",
                    "fotoPerfil" to ""
                )
                FirebaseFirestore.getInstance()
                    .collection("negocios")
                    .document(negocioId)
                    .collection("peluqueros")
                    .document(uid)
                    .set(data)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }

    fun eliminarReserva(
        negocioId: String,
        reservaId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = Firebase.firestore
        db.collection("negocios")
            .document(negocioId)
            .collection("reservas")
            .document(reservaId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }


    fun getClientesPorNegocio(negocioId: String, onResult: (List<Cliente>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("clientes")
            .whereEqualTo("idnegocio", negocioId) // CAMPO CORRECTO
            .get()
            .addOnSuccessListener { result ->
                val clientes = result.map {
                    Cliente(
                        id = it.id,
                        nombre = it.getString("nombre") ?: "",
                        apellidos = it.getString("apellidos") ?: "",
                        email = it.getString("email") ?: ""
                    )
                }
                onResult(clientes)
            }
            .addOnFailureListener {
                Log.e("FirebaseService", "Error al obtener clientes: ${it.message}")
                onResult(emptyList())
            }
    }
    fun getDatosNegocio(
        onSuccess: (Map<String, Any>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return onError(Exception("Usuario no autenticado"))
        val uid = currentUser.uid

        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val negocioId = userDoc.getString("negocioId")
                if (!negocioId.isNullOrEmpty()) {
                    FirebaseFirestore.getInstance().collection("negocios").document(negocioId).get()
                        .addOnSuccessListener { negocioDoc ->
                            val data = negocioDoc.data ?: emptyMap()
                            val dataConId = data.toMutableMap().apply { put("id", negocioDoc.id) }
                            onSuccess(dataConId)
                        }
                        .addOnFailureListener(onError)
                } else {
                    onError(Exception("El usuario no tiene negocio asignado"))
                }
            }
            .addOnFailureListener(onError)
    }
    fun guardarDatosNegocio(
        nombre: String,
        direccion: String,
        telefono: String,
        codigoPostal: String,
        horario: Map<String, HorarioDia>,
        logoUrl: String,
        galeria: List<String>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return onFailure(Exception("Usuario no autenticado"))

        FirebaseFirestore.getInstance().collection("usuarios").document(currentUser.uid).get()
            .addOnSuccessListener { userDoc ->
                val negocioId = userDoc.getString("negocioId")
                    ?: return@addOnSuccessListener onFailure(Exception("No se encontró negocioId"))

                val datos = hashMapOf(
                    "nombre" to nombre,
                    "direccion" to direccion,
                    "telefono" to telefono,
                    "codigoPostal" to codigoPostal,
                    "horario" to horario,
                    "logoUrl" to logoUrl,
                    "galeria" to galeria
                )

                FirebaseFirestore.getInstance().collection("negocios").document(negocioId)
                    .update(datos as Map<String, Any>)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }
    fun getUserName(uid: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                val email = document.getString("email") ?: ""
                val nombre = email.substringBefore("@").replaceFirstChar { it.uppercaseChar() }
                onSuccess(nombre)
            }
            .addOnFailureListener(onFailure)
    }
    fun getUserRole(uid: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                val rol = document.getString("rol")
                if (rol != null) onSuccess(rol)
                else onFailure(Exception("Rol no definido para el usuario"))
            }
            .addOnFailureListener(onFailure)
    }
    fun registerWithEmailPassword(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.let { onSuccess(it) } ?: onFailure(Exception("Usuario no registrado"))
            }
            .addOnFailureListener(onFailure)
    }
    fun createNegocio(
        nombre: String,
        telefono: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "nombre" to nombre,
            "telefono" to telefono,
            "categoria" to "Peluquería unisex",
            "galeria" to emptyList<String>(),
            "logoUrl" to "",
            "horario" to mapOf<String, Any>(),
            "diasEspeciales" to emptyList<String>()
        )

        firestore.collection("negocios").add(data)
            .addOnSuccessListener { document -> onSuccess(document.id) }
            .addOnFailureListener(onFailure)
    }
    fun createUserData(
        uid: String,
        email: String,
        telefono: String,
        rol: String,
        negocioId: String? = null,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userData = mutableMapOf<String, Any>(
            "email" to email,
            "telefono" to telefono,
            "rol" to rol,
            "fotoPerfil" to "",
            "nombre" to "",
            "apellidos" to ""
        )

        negocioId?.let {
            userData["negocioId"] = it
        }

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                // Si el usuario es superpeluquero, lo añadimos también como peluquero del negocio
                if (rol == "superpeluquero" && negocioId != null) {
                    val peluqueroData = hashMapOf(
                        "nombre" to "",
                        "apellidos" to "",
                        "email" to email,
                        "rol" to "peluquero",
                        "fotoPerfil" to ""
                    )

                    FirebaseFirestore.getInstance()
                        .collection("negocios")
                        .document(negocioId)
                        .collection("peluqueros")
                        .document(uid)
                        .set(peluqueroData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener(onFailure)
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener(onFailure)
    }
    fun getUserData(
        uid: String,
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val rol = userDoc.getString("rol")
                val coleccion = if (rol == "cliente") "clientes" else "usuarios"

                FirebaseFirestore.getInstance().collection(coleccion).document(uid).get()
                    .addOnSuccessListener { finalDoc ->
                        if (finalDoc.exists()) {
                            onSuccess(finalDoc.data ?: emptyMap())
                        } else {
                            onFailure(Exception("Usuario no encontrado"))
                        }
                    }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }
    fun actualizarDatosUsuario(
        nombre: String,
        apellidos: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val updates = mapOf(
                "nombre" to nombre,
                "apellidos" to apellidos
            )
            FirebaseFirestore.getInstance().collection("usuarios")
                .document(currentUser.uid)
                .update(updates)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener(onFailure)
        } else {
            onFailure(Exception("Usuario no autenticado"))
        }
    }
    fun updatePassword(
        nuevaPassword: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            currentUser.updatePassword(nuevaPassword)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener(onFailure)
        } else {
            onFailure(Exception("Usuario no autenticado"))
        }
    }
    fun getHorarioNegocio(
        onSuccess: (Map<String, HorarioDia>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
            ?: return onFailure(Exception("Usuario no autenticado"))

        val uid = currentUser.uid

        FirebaseFirestore.getInstance().collection("usuarios").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val negocioId = userDoc.getString("negocioId")
                if (!negocioId.isNullOrEmpty()) {
                    FirebaseFirestore.getInstance().collection("negocios").document(negocioId).get()
                        .addOnSuccessListener { negocioDoc ->
                            val horarioMap = negocioDoc.get("horario") as? Map<*, *>
                            if (horarioMap != null) {
                                val horario = horarioMap.mapNotNull { (dia, datos) ->
                                    val datosMap = datos as? Map<*, *> ?: return@mapNotNull null
                                    val diaStr = dia.toString()
                                    val horarioDia = HorarioDia(
                                        aperturaManana = datosMap["aperturaManana"]?.toString() ?: "",
                                        cierreManana = datosMap["cierreManana"]?.toString() ?: "",
                                        aperturaTarde = datosMap["aperturaTarde"]?.toString() ?: "",
                                        cierreTarde = datosMap["cierreTarde"]?.toString() ?: ""
                                    )
                                    diaStr to horarioDia
                                }.toMap()
                                onSuccess(horario)
                            } else {
                                onSuccess(emptyMap())
                            }
                        }
                        .addOnFailureListener(onFailure)
                } else {
                    onFailure(Exception("El usuario no tiene negocio asignado"))
                }
            }
            .addOnFailureListener(onFailure)
    }
    fun getReservasPorPeluquero(
        negocioId: String,
        peluqueroId: String,
        onResult: (List<Cita>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("negocios").document(negocioId)
            .collection("reservas")
            .whereEqualTo("idPeluquero", peluqueroId)
            .get()
            .addOnSuccessListener { result ->
                val citas = result.map { doc ->
                    Cita(
                        id = doc.id, // ✅ ESTO ES LO QUE FALTABA
                        idCliente = doc.getString("idCliente") ?: "",
                        idServicio = doc.getString("idServicio") ?: "",
                        nombreCliente = "", // puedes completarlo si guardas nombre en la reserva
                        idPeluquero = doc.getString("idPeluquero") ?: "",
                        servicio = "", // lo puedes completar luego
                        fecha = doc.getString("fecha") ?: "",
                        hora = doc.getString("hora") ?: "",
                        estado = doc.getString("estado") ?: "reservada"
                    )
                }
                onResult(citas)
            }
            .addOnFailureListener { onError(it) }
    }
    fun actualizarReserva(
        negocioId: String,
        reservaId: String,
        datosActualizados: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = Firebase.firestore
        db.collection("negocios")
            .document(negocioId)
            .collection("reservas")
            .document(reservaId)
            .update(datosActualizados)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }


    fun getTodasLasBarberias(
        onSuccess: (List<Barberia>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("negocios")
            .get()
            .addOnSuccessListener { result ->
                val barberias = result.map { doc ->
                    Barberia(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        direccion = doc.getString("direccion") ?: "",
                        horaCierre = doc.get("horario")?.let { horarioMap ->
                            if (horarioMap is Map<*, *>) {
                                val hoy = obtenerDiaActual()
                                val horarioDia = horarioMap[hoy] as? Map<*, *>
                                horarioDia?.get("cierreTarde")?.toString() ?: "20:00"
                            } else "20:00"
                        } ?: "20:00",
                        logoUrl = doc.getString("logoUrl") ?: ""
                    )
                }
                onSuccess(barberias)
            }
            .addOnFailureListener(onFailure)
    }

    // Utilidad para obtener el día actual como "Lunes", "Martes", etc.
    fun obtenerDiaActual(): String {
        val dias = listOf(
            "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"
        )
        val calendario = java.util.Calendar.getInstance()
        return dias[calendario.get(java.util.Calendar.DAY_OF_WEEK) - 1]
    }

    fun guardarBarberiaFavoritaCliente(
        clienteId: String,
        idNegocio: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("FIREBASE_DEBUG", "Intentando guardar barbería favorita: $idNegocio para cliente: $clienteId")
        FirebaseFirestore.getInstance()
            .collection("clientes")
            .document(clienteId)
            .update("idnegocio", idNegocio)
            .addOnSuccessListener {
                Log.d("FIREBASE_DEBUG", "¡Barbería favorita guardada correctamente!")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE_DEBUG", "Error al guardar barbería favorita: ${e.message}")
                onFailure(e)
            }
    }

    fun quitarBarberiaFavoritaCliente(
        clienteId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("clientes")
            .document(clienteId)
            .update("idnegocio", null)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun obtenerBarberiaFavorita(
        clienteId: String,
        onResult: (String?) -> Unit
    ) {
        val db = Firebase.firestore
        db.collection("clientes") // <-- CAMBIAR AQUÍ
            .document(clienteId)
            .get()
            .addOnSuccessListener { document ->
                val favoritoId = document.getString("idnegocio") // <-- este campo sí se actualiza
                onResult(favoritoId)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
    fun createClienteData(
        uid: String,
        email: String,
        telefono: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val clienteData = hashMapOf(
            "email" to email,
            "telefono" to telefono,
            "rol" to "cliente",
            "fotoPerfil" to "",
            "nombre" to "",
            "apellidos" to "",
            "idnegocio" to null
        )

        FirebaseFirestore.getInstance()
            .collection("clientes")
            .document(uid)
            .set(clienteData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }
    suspend fun getUserName(): String? {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val doc = FirebaseFirestore.getInstance().collection("usuarios").document(userId).get().await()
        return doc.getString("nombre")
    }


    fun getUltimaCitaCliente(onResult: (Cita?) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return onResult(null)

        val db = Firebase.firestore

        db.collection("clientes").document(uid).get()
            .addOnSuccessListener { clienteDoc ->
                val negocioId = clienteDoc.getString("idnegocio")
                if (negocioId.isNullOrEmpty()) return@addOnSuccessListener onResult(null)

                db.collection("negocios").document(negocioId)
                    .collection("reservas")
                    .whereEqualTo("idCliente", uid)
                    .whereEqualTo("estado", "pendiente")
                    .get()
                    .addOnSuccessListener { result ->
                        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        val hoy = java.util.Date()

                        val citas = result.mapNotNull { doc ->
                            val fechaStr = doc.getString("fecha") ?: return@mapNotNull null
                            val hora = doc.getString("hora") ?: return@mapNotNull null
                            val fechaDate = try { dateFormat.parse(fechaStr) } catch (_: Exception) { null } ?: return@mapNotNull null

                            Cita(
                                id = doc.id,
                                idCliente = doc.getString("idCliente") ?: "",
                                idServicio = doc.getString("idServicio") ?: "",
                                nombreCliente = "",
                                idPeluquero = doc.getString("idPeluquero") ?: "",
                                servicio = "", // opcional: mapear nombre real si lo necesitas
                                fecha = fechaStr,
                                hora = hora,
                                estado = doc.getString("estado") ?: ""
                            ) to fechaDate
                        }

                        // Ordenamos por fecha más próxima a hoy
                        val citaMasProxima = citas
                            .filter { it.second.after(hoy) || dateFormat.format(it.second) == dateFormat.format(hoy) }
                            .minByOrNull { it.second }
                            ?.first

                        onResult(citaMasProxima)
                    }
                    .addOnFailureListener { onResult(null) }
            }
            .addOnFailureListener { onResult(null) }
    }

}