package com.example.myapplication.ui.pantallamain

import com.example.myapplication.domain.modelo.Cancion

data class MainState(
    val textoLabel: String = "" ,
    val textoCaja: String = "",
    val cancion: Cancion = Cancion(),
    val error :String? = null,

    )
