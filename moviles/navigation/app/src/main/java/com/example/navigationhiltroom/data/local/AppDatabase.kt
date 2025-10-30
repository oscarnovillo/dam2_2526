package com.example.navigationhiltroom.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.navigationhiltroom.data.local.entities.AlumnoEntity

@Database(
    entities = [AlumnoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {



}