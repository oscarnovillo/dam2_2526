package com.example.navigationhiltroom.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation
import com.example.navigationhiltroom.domain.model.Asignatura

data class AsignaturaConAlumnos(

    @Embedded val asignatura: AsignturaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val ropas: List<AlumnoEntity>

)


fun AsignaturaConAlumnos.toAsignatura() : Asignatura {
    return Asignatura(
        this.asignatura.id,
        this.asignatura.nombre,
        this.asignatura.creditos,
        this.ropas.map { it.toAlumno() }
    )
}