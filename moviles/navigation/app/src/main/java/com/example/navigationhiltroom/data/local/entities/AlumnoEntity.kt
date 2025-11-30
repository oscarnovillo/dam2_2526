package com.example.navigationhiltroom.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.navigationhiltroom.domain.model.Alumno

@Entity(
    tableName = "alumnos",
    foreignKeys = [
        ForeignKey(
            entity = AsignturaEntity::class,
            parentColumns = ["id"],
            childColumns = ["asignaturaID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AlumnoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val nombre: String,
    val asignaturaID: Int,
    val apellidos: String,
    val edad: Int
)

fun Alumno.toAlumnoEntity() = AlumnoEntity(
    id = this.id,
    nombre = this.nombre,
    apellidos = this.apellidos,
    edad = this.edad,
    asignaturaID = 0
)
fun AlumnoEntity.toAlumno() = Alumno(
    id = this.id,
    nombre = this.nombre,
    apellidos = this.apellidos,
    edad = this.edad

)


