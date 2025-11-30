package com.example.navigationhiltroom.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.navigationhiltroom.data.local.entities.AlumnoEntity
import com.example.navigationhiltroom.data.local.entities.AsignaturaConAlumnos
import com.example.navigationhiltroom.domain.model.Alumno
import kotlinx.coroutines.flow.Flow

@Dao
interface AlumnoSDao {

    @Query("SELECT * FROM alumnos ORDER BY nombre ASC")
    suspend fun getAllAlumnos(): List<AlumnoEntity>


    @Query("SELECT * FROM alumnos ORDER BY nombre ASC")
    fun getAllAlumnosFlow(): Flow<List<AlumnoEntity>>

    @Query("SELECT * FROM alumnos WHERE id = :id")
    fun getUsuarioById(id: Long): Flow<AlumnoEntity?>


    @Transaction
    @Query("SELECT * FROM asignaturas WHERE id = :usuarioId")
    suspend fun getAsigCalu(usuarioId: Long): AsignaturaConAlumnos

    @Insert
    suspend fun insertUsuario(usuario: AlumnoEntity): Long

    @Update
    suspend fun updateUsuario(usuario: AlumnoEntity)

    @Delete
    suspend fun deleteUsuario(usuario: AlumnoEntity)
}