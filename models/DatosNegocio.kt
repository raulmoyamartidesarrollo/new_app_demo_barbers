package com.github.jetbrains.rssreader.androidApp.models

data class HorarioDia(
    val aperturaManana: String = "",
    val cierreManana: String = "",
    val aperturaTarde: String = "",
    val cierreTarde: String = ""
)

data class DatosNegocio(
    val nombre: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val codigoPostal: String = "",
    val horario: Map<String, HorarioDia> = emptyMap()
)