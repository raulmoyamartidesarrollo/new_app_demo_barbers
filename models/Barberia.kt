package com.github.jetbrains.rssreader.androidApp.models

data class Barberia(
    val id: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val horaCierre: String = "",
    val logoUrl: String = "",
    val galeria: List<String> = emptyList()
)