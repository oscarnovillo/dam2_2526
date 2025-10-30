package com.example.navigationhiltroom.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "alumnos",
)
data class AlumnoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val edad: Int
)

fun prubea(){
    val nombre = "casa"
    val nombreModificado = nombre.hablarconlaI()
}

fun String.hablarconlaI(): String{
    return this.replace("a","i")
        .replace("e","i")
        .replace("o","i")
        .replace("u","i")
}



