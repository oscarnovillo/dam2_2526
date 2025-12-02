package com.example.navigationhiltroom.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.navigationhiltroom.data.local.dao.AlumnoSDao
import com.example.navigationhiltroom.data.local.entities.AlumnoEntity
import com.example.navigationhiltroom.data.local.entities.AsignaturaConAlumnos
import com.example.navigationhiltroom.data.local.entities.AsignturaEntity

@Database(
    entities = [AlumnoEntity::class, AsignturaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alumnoDao(): AlumnoSDao

}