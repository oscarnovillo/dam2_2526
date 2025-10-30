package com.example.navigationhiltroom.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


data class Alumno(

    val id: Int,
    val nombre: String,
    val apellidos: String,
    val edad: Int
)
