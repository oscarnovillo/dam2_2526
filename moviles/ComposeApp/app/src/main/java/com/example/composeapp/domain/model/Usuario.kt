package com.example.composeapp.domain.model


data class Usuario(
    var nombre: String = "",
    var apellidos: String = "",
    var telefono: String = "",
    var email: String = "",
    var fechaNacimiento: String = "",
    var genero: String = "",
    var comentarios: String = "",
    var tieneTV: Boolean = false
)