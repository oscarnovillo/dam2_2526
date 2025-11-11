package com.example.navigationhiltroom.data

import com.example.navigationhiltroom.data.local.dao.AlumnoSDao
import com.example.navigationhiltroom.data.local.entities.toAlumno
import com.example.navigationhiltroom.data.local.entities.toAlumnoEntity
import com.example.navigationhiltroom.domain.model.Alumno
import jakarta.inject.Inject

class RepositoryAlumnos @Inject constructor( private val ropaDao: AlumnoSDao) {

    suspend fun getAlumnos(): List<Alumno> {
        return ropaDao.getAllAlumnos().map { it.toAlumno() }
    }

    suspend fun insertAlumno(alumno: Alumno) {
        ropaDao.insertUsuario(
            alumno.toAlumnoEntity()
        )
    }
}