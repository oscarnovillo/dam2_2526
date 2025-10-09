package com.example.myapplication.domain.modelo

data class Cancion(
    val titulo: String = "",
    val interprete: String = "",
    val tipo: String = "solista" // "solista" o "grupo"
)
