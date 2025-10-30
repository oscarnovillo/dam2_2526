package com.example.navigationhiltroom.data

import com.example.navigationhiltroom.domain.model.Alumno
import jakarta.inject.Inject

class RepositoryAlumnos @Inject constructor() {

    fun getAlumnos(): List<Alumno> {
        return listOf(
            Alumno(1, "Juan", "García Pérez", 20),
            Alumno(2, "María", "López Martínez", 22),
            Alumno(3, "Pedro", "Sánchez Rodríguez", 21),
            Alumno(4, "Ana", "Fernández González", 23),
            Alumno(5, "Luis", "Martín Jiménez", 20),
            Alumno(6, "Carmen", "Díaz Ruiz", 22),
            Alumno(7, "José", "Moreno Álvarez", 21),
            Alumno(8, "Laura", "Muñoz Romero", 20),
            Alumno(9, "Carlos", "Gómez Torres", 23),
            Alumno(10, "Elena", "Navarro Gil", 22)
        )
    }
}