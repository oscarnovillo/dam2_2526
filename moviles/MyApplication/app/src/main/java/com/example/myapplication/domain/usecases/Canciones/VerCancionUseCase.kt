package com.example.myapplication.domain.usecases.Canciones

import com.example.myapplication.data.RepositorioCanciones
import com.example.myapplication.domain.modelo.Cancion

class VerCancionUseCase {

    operator fun invoke(id: Int): Cancion = RepositorioCanciones.getCancion(id)



}