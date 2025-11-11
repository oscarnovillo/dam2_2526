package com.example.navigationhiltroom.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "asignaturas",
)
data class AsignturaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val nombre: String,
    val creditos: Int
)
