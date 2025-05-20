package com.github.jetbrains.rssreader.androidApp

import android.net.Uri
import android.util.Log
import com.github.jetbrains.rssreader.androidApp.models.Barberia
import com.github.jetbrains.rssreader.androidApp.utils.createHttpClient
import com.github.jetbrains.rssreader.androidApp.utils.enviarNotificacionPushMultiple
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.messaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


fun calcularHorasDisponiblesSimple(
    horarioDia: HorarioDia?,
    citas: List<Cita>,
    fechaSeleccionada: LocalDate
): List<String> {
    if (horarioDia == null) return emptyList()

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val tramos = mutableListOf<String>()

    listOf(
        horarioDia.aperturaManana to horarioDia.cierreManana,
        horarioDia.aperturaTarde to horarioDia.cierreTarde
    ).forEach { (inicio, fin) ->
        if (!inicio.isNullOrBlank() && !fin.isNullOrBlank()) {
            var horaActual = LocalTime.parse(inicio)
            val horaFin = LocalTime.parse(fin)
            while (!horaActual.isAfter(horaFin)) {
                tramos.add(horaActual.toString().substring(0, 5))
                horaActual = horaActual.plusMinutes(30)
            }
        }
    }

    val fechaStr = fechaSeleccionada.format(formatter)
    val horasOcupadas = citas.filter { it.fecha == fechaStr }.map { it.hora.trim() }

    return tramos.filterNot { horasOcupadas.contains(it) }
}

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
    val estado: String = "",
    val precio: Double = 0.0
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
    suspend fun getTelefonoNegocioDelCliente(): String? {
        val user = Firebase.auth.currentUser ?: return null
        val clienteSnap = Firebase.firestore.collection("clientes").document(user.uid).get().await()
        val negocioId = clienteSnap.getString("idnegocio") ?: return null
        val negocioSnap = Firebase.firestore.collection("negocios").document(negocioId).get().await()
        return negocioSnap.getString("telefono")?.trim()
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

        val db = Firebase.firestore

        db.collection("negocios").document(negocioId)
            .collection("reservas")
            .add(reservaData)
            .addOnSuccessListener {
                onSuccess()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val tokenPeluquero = getTokenDelPeluquero(idPeluquero)
                        val tokenCliente = getTokenDelCliente(idCliente)

                        val tokens = listOfNotNull(tokenPeluquero, tokenCliente)

                        if (tokens.isNotEmpty()) {
                            val client = createHttpClient()

                            enviarNotificacionPushMultiple(
                                client = client,
                                tokens = tokens,
                                titulo = "Nueva cita reservada",
                                mensaje = "Tu cita fue reservada para el $fecha a las $hora"
                            )

                            client.close()
                        }
                    } catch (e: Exception) {
                        Log.e("Notificación", "Error al enviar notificación: ${e.message}")
                    }
                }
            }
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

                val dataNegocio = hashMapOf(
                    "nombre" to nombre,
                    "apellidos" to apellidos,
                    "email" to email,
                    "rol" to "peluquero",
                    "fotoPerfil" to "",
                    "token" to ""
                )

                val dataUsuario = hashMapOf(
                    "email" to email,
                    "rol" to "peluquero",
                    "negocioId" to negocioId
                )

                val db = FirebaseFirestore.getInstance()

                db.collection("negocios")
                    .document(negocioId)
                    .collection("peluqueros")
                    .document(uid)
                    .set(dataNegocio)
                    .addOnSuccessListener {
                        db.collection("usuarios")
                            .document(uid)
                            .set(dataUsuario)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener(onFailure)
                    }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }

    fun actualizarPeluquero(
        negocioId: String,
        peluqueroId: String,
        nombre: String,
        email: String,
        nuevaPassword: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        val peluqueroRef = firestore
            .collection("negocios")
            .document(negocioId)
            .collection("peluqueros")
            .document(peluqueroId)

        val camposActualizados = mutableMapOf<String, Any>()

        if (nombre.isNotBlank()) camposActualizados["nombre"] = nombre
        if (email.isNotBlank()) camposActualizados["email"] = email

        peluqueroRef.update(camposActualizados)
            .addOnSuccessListener {
                if (!nuevaPassword.isNullOrBlank()) {
                    FirebaseAuth.getInstance().currentUser?.updatePassword(nuevaPassword)
                        ?.addOnSuccessListener { onSuccess() }
                        ?.addOnFailureListener { onFailure(it) }
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun eliminarReserva(
        negocioId: String,
        reservaId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = Firebase.firestore
        val reservaRef = db.collection("negocios")
            .document(negocioId)
            .collection("reservas")
            .document(reservaId)

        reservaRef.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    return@addOnSuccessListener onFailure(Exception("La reserva no existe"))
                }

                val idCliente = doc.getString("idCliente")
                val idPeluquero = doc.getString("idPeluquero")
                val fecha = doc.getString("fecha")
                val hora = doc.getString("hora")

                reservaRef.delete()
                    .addOnSuccessListener {
                        onSuccess()

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val tokenCliente = idCliente?.let { getTokenDelCliente(it) }
                                val tokenPeluquero = idPeluquero?.let { getTokenDelPeluquero(it) }

                                val tokens = listOfNotNull(tokenCliente, tokenPeluquero)

                                if (tokens.isNotEmpty()) {
                                    val client = createHttpClient()
                                    enviarNotificacionPushMultiple(
                                        client = client,
                                        tokens = tokens,
                                        titulo = "Cita cancelada",
                                        mensaje = "Tu cita del $fecha a las $hora ha sido cancelada."
                                    )
                                    client.close()
                                }
                            } catch (e: Exception) {
                                Log.e("Notificación", "Error al enviar notificación de cancelación: ${e.message}")
                            }
                        }
                    }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
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
    fun getHorarioNegocioCliente(
        negocioId: String,
        onSuccess: (Map<String, HorarioDia>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
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
        val reservaRef = db.collection("negocios")
            .document(negocioId)
            .collection("reservas")
            .document(reservaId)

        reservaRef.update(datosActualizados)
            .addOnSuccessListener {
                onSuccess()

                // 🔄 Releer los datos desde Firestore para asegurar que tenemos idCliente, idPeluquero, fecha y hora correctos
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val doc = reservaRef.get().await()

                        val idCliente = doc.getString("idCliente")
                        val idPeluquero = doc.getString("idPeluquero")
                        val fecha = doc.getString("fecha")
                        val hora = doc.getString("hora")

                        Log.d("Notificación", "Reserva editada - Cliente: $idCliente, Peluquero: $idPeluquero")

                        val tokens = listOfNotNull(
                            idCliente?.let { getTokenDelCliente(it) },
                            idPeluquero?.let { getTokenDelPeluquero(it) }
                        )

                        if (tokens.isNotEmpty()) {
                            val client = createHttpClient()
                            enviarNotificacionPushMultiple(
                                client,
                                tokens,
                                titulo = "Cita actualizada",
                                mensaje = "Tu cita ha sido modificada para el $fecha a las $hora"
                            )
                            client.close()
                        } else {
                            Log.e("Notificación", "No se encontraron tokens válidos tras edición.")
                        }
                    } catch (e: Exception) {
                        Log.e("Notificación", "Error al enviar notificación tras editar: ${e.message}")
                    }
                }
            }
            .addOnFailureListener { onFailure(it) }
    }
    fun actualizarReservaCliente(
        negocioId: String,
        reservaId: String,
        nuevaFecha: String,
        nuevaHora: String,
        nuevoPeluqueroId: String,
        nuevoServicioId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val reservaRef = db
            .collection("negocios")
            .document(negocioId)
            .collection("reservas")
            .document(reservaId)

        val nuevosDatos = mapOf(
            "fecha" to nuevaFecha,
            "hora" to nuevaHora,
            "peluqueroId" to nuevoPeluqueroId,
            "servicioId" to nuevoServicioId
        )

        reservaRef.update(nuevosDatos)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }
    fun borrarReservaCliente(
        negocioId: String,
        reservaId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("negocios")
            .document(negocioId)
            .collection("reservas")
            .document(reservaId)
            .delete()
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
                    val horarioMap = doc.get("horario") as? Map<*, *>
                    val hoy = FirebaseService.obtenerDiaActual()
                    val horarioDia = horarioMap?.get(hoy) as? Map<*, *>
                    val cierre = horarioDia?.get("cierreTarde")?.toString() ?: "20:00"

                    Barberia(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        direccion = doc.getString("direccion") ?: "",
                        horaCierre = cierre,
                        logoUrl = doc.getString("logoUrl") ?: "",
                        galeria = doc.get("galeria") as? List<String> ?: emptyList()
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
        val doc = FirebaseFirestore.getInstance().collection("clientes").document(userId).get().await()

        val nombre = doc.getString("nombre")?.takeIf { it.isNotBlank() }
        val apellidos = doc.getString("apellidos")?.takeIf { it.isNotBlank() }

        return when {
            nombre != null -> nombre
            apellidos != null -> apellidos
            else -> {
                // Fallback: intenta sacar algo del email
                val email = doc.getString("email")
                email?.substringBefore("@")?.replaceFirstChar { it.uppercaseChar() }
            }
        }
    }


    fun getUltimaCitaCliente(onResult: (Cita?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return onResult(null)
        val db = Firebase.firestore

        db.collection("clientes").document(userId).get()
            .addOnSuccessListener { doc ->
                val negocioId = doc.getString("idnegocio") ?: return@addOnSuccessListener onResult(null)

                val reservasRef = db.collection("negocios").document(negocioId).collection("reservas")
                val hoy = SimpleDateFormat("dd/MM/yyyy").parse(SimpleDateFormat("dd/MM/yyyy").format(Date()))

                // Buscar primero cita pendiente
                reservasRef
                    .whereEqualTo("idCliente", userId)
                    .whereEqualTo("estado", "pendiente")
                    .get()
                    .addOnSuccessListener { result ->
                        val citaPendiente = result.documents
                            .mapNotNull { doc ->
                                val fechaStr = doc.getString("fecha") ?: return@mapNotNull null
                                val fechaDate = try {
                                    SimpleDateFormat("dd/MM/yyyy").parse(fechaStr)
                                } catch (e: Exception) {
                                    null
                                } ?: return@mapNotNull null

                                Triple(doc.id, fechaDate, doc)
                            }
                            .filter { it.second >= hoy }
                            .minByOrNull { it.second }

                        if (citaPendiente != null) {
                            procesarCitaConDetalle(citaPendiente.third, citaPendiente.first, negocioId, userId, onResult)
                        } else {
                            // Si no hay cita pendiente, buscar la última finalizada antes de hoy
                            reservasRef
                                .whereEqualTo("idCliente", userId)
                                .whereEqualTo("estado", "finalizada")
                                .get()
                                .addOnSuccessListener { finalizadas ->
                                    val citaFinalizada = finalizadas.documents
                                        .mapNotNull { doc ->
                                            val fechaStr = doc.getString("fecha") ?: return@mapNotNull null
                                            val fechaDate = try {
                                                SimpleDateFormat("dd/MM/yyyy").parse(fechaStr)
                                            } catch (e: Exception) {
                                                null
                                            } ?: return@mapNotNull null

                                            Triple(doc.id, fechaDate, doc)
                                        }
                                        .filter { it.second <= hoy }
                                        .maxByOrNull { it.second }

                                    if (citaFinalizada != null) {
                                        procesarCitaConDetalle(citaFinalizada.third, citaFinalizada.first, negocioId, userId, onResult)
                                    } else {
                                        onResult(null)
                                    }
                                }
                                .addOnFailureListener { onResult(null) }
                        }
                    }
                    .addOnFailureListener { onResult(null) }
            }
            .addOnFailureListener { onResult(null) }
    }
    private fun procesarCitaConDetalle(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        reservaId: String,
        negocioId: String,
        userId: String,
        onResult: (Cita?) -> Unit
    ) {
        val db = Firebase.firestore
        val idServicio = doc.getString("idServicio") ?: ""
        val idPeluquero = doc.getString("idPeluquero") ?: ""

        db.collection("negocios").document(negocioId)
            .collection("servicios").document(idServicio)
            .get()
            .addOnSuccessListener { servicioDoc ->
                val nombreServicio = servicioDoc.getString("nombre") ?: ""
                val precioServicio = servicioDoc.getDouble("precio") ?: 0.0

                db.collection("negocios").document(negocioId)
                    .collection("peluqueros").document(idPeluquero)
                    .get()
                    .addOnSuccessListener { peluqueroDoc ->
                        val nombrePeluquero = peluqueroDoc.getString("nombre") ?: ""

                        val cita = Cita(
                            id = reservaId,
                            fecha = doc.getString("fecha") ?: "",
                            hora = doc.getString("hora") ?: "",
                            servicio = nombreServicio,
                            nombreCliente = nombrePeluquero,
                            idCliente = userId,
                            idServicio = idServicio,
                            idPeluquero = idPeluquero,
                            estado = doc.getString("estado") ?: "pendiente",
                            precio = precioServicio
                        )
                        onResult(cita)
                    }
                    .addOnFailureListener { onResult(null) }
            }
            .addOnFailureListener { onResult(null) }
    }
    fun subirFotoPerfil(userId: String, bitmap: android.graphics.Bitmap, onResult: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("fotos_perfil/$userId.jpg")

        val baos = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Guardar la URL en Firestore también
                    FirebaseFirestore.getInstance().collection("usuarios").document(userId)
                        .update("fotoPerfil", uri.toString())
                        .addOnSuccessListener { onResult(uri.toString()) }
                        .addOnFailureListener { onResult(null) }
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    suspend fun getNegocioFavoritoCliente(
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = getCurrentUser()
        if (user != null) {
            val doc = firestore.collection("clientes").document(user.uid).get().await()
            val negocioId = doc.getString("idnegocio")
            if (negocioId != null) {
                onSuccess(negocioId)
            } else {
                onFailure(Exception("Negocio favorito no definido para el cliente"))
            }
        } else {
            onFailure(Exception("Usuario no autenticado"))
        }
    }
    fun getCitasPorPeluquero(
        negocioId: String,
        peluqueroId: String,
        onSuccess: (List<Cita>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = Firebase.firestore
        db.collection("negocios")
            .document(negocioId)
            .collection("reservas")
            .whereEqualTo("idPeluquero", peluqueroId)
            .get()
            .addOnSuccessListener { result ->
                val citas = result.documents.mapNotNull { doc ->
                    doc.toObject(Cita::class.java)?.copy(id = doc.id)
                }
                onSuccess(citas)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    suspend fun getTokenDelPeluquero(peluqueroId: String): String? {
        return try {
            val doc = Firebase.firestore.collection("usuarios").document(peluqueroId).get().await()
            doc.getString("token")
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error al obtener token del peluquero: ${e.message}")
            null
        }
    }

    suspend fun getTokenDelCliente(clienteId: String): String? {
        return try {
            val doc = Firebase.firestore.collection("clientes").document(clienteId).get().await()
            doc.getString("token")
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error al obtener token del cliente: ${e.message}")
            null
        }
    }

    suspend fun subirImagenNegocio(uri: Uri, nombre: String): String {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return ""
        val storageRef = FirebaseStorage.getInstance().reference
            .child("imagenes_negocio/$userId/$nombre")

        return withContext(Dispatchers.IO) {
            storageRef.putFile(uri).await()
            storageRef.downloadUrl.await().toString()
        }
    }
    fun actualizarTokenUsuario(userId: String, token: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("usuarios").document(userId)
            .update("token", token)
    }
    suspend fun eliminarCitasDePeluquero(negocioId: String, peluqueroId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val reservasRef = db.collection("negocios").document(negocioId).collection("reservas")
            val snapshot = reservasRef.whereEqualTo("idPeluquero", peluqueroId).get().await()

            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }

            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    suspend fun reasignarCitasDelPeluquero(
        negocioId: String,
        antiguoPeluqueroId: String,
        nuevoPeluqueroId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val db = FirebaseFirestore.getInstance()
            val reservasRef = db.collection("negocios").document(negocioId).collection("reservas")
            val snapshot = reservasRef.whereEqualTo("idPeluquero", antiguoPeluqueroId).get().await()

            for (doc in snapshot.documents) {
                doc.reference.update("idPeluquero", nuevoPeluqueroId).await()
            }

            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    suspend fun eliminarPeluquero(negocioId: String, peluqueroId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("negocios").document(negocioId)
                .collection("peluqueros").document(peluqueroId)
            ref.delete().await()
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }
    fun eliminarPeluqueroYReservas(
        negocioId: String,
        peluqueroId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val reservasRef = db.collection("negocios").document(negocioId).collection("reservas")

        reservasRef.whereEqualTo("idPeluquero", peluqueroId)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }

                db.collection("negocios").document(negocioId)
                    .collection("peluqueros").document(peluqueroId).delete()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun reasignarCitasYEliminarPeluquero(
        negocioId: String,
        peluqueroIdAntiguo: String,
        nuevoPeluqueroId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val reservasRef = db.collection("negocios").document(negocioId).collection("reservas")

        reservasRef.whereEqualTo("idPeluquero", peluqueroIdAntiguo)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach {
                    batch.update(it.reference, "idPeluquero", nuevoPeluqueroId)
                }

                db.collection("negocios").document(negocioId)
                    .collection("peluqueros").document(peluqueroIdAntiguo).delete()
                    .addOnSuccessListener {
                        batch.commit()
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onFailure(it) }
                    }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun getPeluqueroPorId(negocioId: String, peluqueroId: String, onResult: (Peluquero?) -> Unit) {
        val db = Firebase.firestore
        db.collection("negocios")
            .document(negocioId)
            .collection("peluqueros")
            .document(peluqueroId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val peluquero = Peluquero(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        apellidos = doc.getString("apellidos") ?: ""
                    )
                    onResult(peluquero)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun guardarTokenEnFirestore(userId: String, rol: String) {
        Log.d("RAUL", "🚀 Inicio guardarTokenEnFirestore para $userId con rol $rol")

        Firebase.messaging.token
            .addOnSuccessListener { token ->
                Log.d("RAUL", "🔑 Token FCM obtenido: $token")

                val db = Firebase.firestore

                // 1. Actualizar token en `usuarios`
                db.collection("usuarios").document(userId)
                    .update("token", token)
                    .addOnSuccessListener {
                        Log.d("TOKEN", "✅ Token actualizado en colección usuarios")
                    }
                    .addOnFailureListener {
                        Log.e("TOKEN", "❌ Error al actualizar token en usuarios: ${it.message}")
                    }

                // 2. Si es peluquero o superpeluquero, actualizar también en `peluqueros/{userId}`
                if (rol == "peluquero" || rol == "superpeluquero") {
                    db.collection("usuarios").document(userId).get()
                        .addOnSuccessListener { userDoc ->
                            val negocioId = userDoc.getString("negocioId")
                            if (!negocioId.isNullOrEmpty()) {
                                val peluqueroRef = db.collection("negocios")
                                    .document(negocioId)
                                    .collection("peluqueros")
                                    .document(userId)

                                peluqueroRef.update("token", token)
                                    .addOnSuccessListener {
                                        Log.d("TOKEN", "✅ Token actualizado en peluquero con ID")
                                    }
                                    .addOnFailureListener {
                                        Log.e("TOKEN", "❌ Error actualizando token en peluquero: ${it.message}")
                                    }
                            } else {
                                Log.e("TOKEN", "❌ negocioId vacío para usuario $userId")
                            }
                        }
                        .addOnFailureListener {
                            Log.e("TOKEN", "❌ Error al obtener documento de usuario: ${it.message}")
                        }
                }
            }
            .addOnFailureListener {
                Log.e("TOKEN", "❌ Error al obtener token FCM: ${it.message}")
            }
    }

}

