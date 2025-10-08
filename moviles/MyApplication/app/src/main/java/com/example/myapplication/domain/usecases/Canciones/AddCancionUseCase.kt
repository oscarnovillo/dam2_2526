package com.example.myapplication.domain.usecases.Canciones

import com.example.myapplication.data.RepositorioCanciones
import com.example.myapplication.domain.modelo.Cancion

class AddCancionUseCase {

    operator fun invoke(cancion : Cancion): Boolean {
        return RepositorioCanciones.addCancion(cancion)
    }

}