package com.example.navigationhiltroom.domain.model

data class Asignatura(

    val id: Int,
    val nombre: String,
    val creditos: Int,
    val alumnos : List<Alumno>,


)
