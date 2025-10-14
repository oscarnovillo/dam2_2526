package com.example.myapplication.domain.usecases.Canciones

import com.example.myapplication.data.RepositorioCanciones
import com.example.myapplication.domain.modelo.Cancion

class GetCancionesUseCase {
    operator fun invoke(): List<Cancion> = RepositorioCanciones.getCanciones()
}

