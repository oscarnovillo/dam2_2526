package com.example.myapplication.ui.pantallamain

import com.example.myapplication.domain.modelo.Cancion

data class MainState(

    val indiceCancion: Int = 0,
    val isDisable: Boolean = false,
    val cancion: Cancion = Cancion(),
    val mensaje :String? = null,

    )
