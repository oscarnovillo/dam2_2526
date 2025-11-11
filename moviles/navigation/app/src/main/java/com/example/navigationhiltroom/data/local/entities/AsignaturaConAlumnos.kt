package com.example.navigationhiltroom.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class AsignaturaConAlumnos(

    @Embedded val asignatura: AsignturaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "asignaturaID"
    )
    val ropas: List<AlumnoEntity>

)
