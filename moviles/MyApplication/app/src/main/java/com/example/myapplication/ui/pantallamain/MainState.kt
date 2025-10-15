package com.example.myapplication.ui.pantallamain

import com.example.myapplication.domain.modelo.Cancion
import com.example.myapplication.ui.common.UiEvent

data class MainState(

    val numCanciones : Int = 0,
    val indiceCancion: Int = 0,
    val isDisable: Boolean = false,
    val cancion: Cancion = Cancion(),
    val uiEvent: UiEvent? = null,

    )
