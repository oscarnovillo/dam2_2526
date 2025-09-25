package com.example.myapplication.data

import com.example.myapplication.domain.modelo.Cancion

object RepositorioCanciones {

    private val canciones = mutableListOf<Cancion>()

    init {

        canciones.add(Cancion("j","j"))
    }

    fun getCancion(id:Int) = canciones[id]

//    fun getCancion(id:Int) : Cancion
//    {
//        return canciones[id]
//    }
}