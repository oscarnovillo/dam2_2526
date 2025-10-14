package com.example.myapplication.ui.pantallalistado

import com.example.myapplication.domain.modelo.Cancion

data class ListadoCancionesState(
    val canciones: List<Cancion> = emptyList(),
    val mensaje: String? = null
)

