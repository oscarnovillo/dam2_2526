package com.example.navigationhiltroom.domain.usecase

import com.example.navigationhiltroom.data.RepositoryAlumnos
import com.example.navigationhiltroom.domain.model.Alumno
import jakarta.inject.Inject

class GetAlumnosUseCase @Inject constructor (private val repository: RepositoryAlumnos) {

    operator fun invoke(): List<Alumno> {
        return repository.getAlumnos()
    }
}

