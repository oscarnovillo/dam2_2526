package com.example.navigationhiltroom.ui.primerFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.navigationhiltroom.domain.model.Alumno
import com.example.navigationhiltroom.domain.usecase.GetAlumnosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class AlumnosViewModel @Inject constructor(
    private val getAlumnosUseCase: GetAlumnosUseCase) : ViewModel() {

    private val _alumnos = MutableLiveData<List<Alumno>>()
    val alumnos: LiveData<List<Alumno>> = _alumnos

    init {
        loadAlumnos()
    }

    private fun loadAlumnos() {
        _alumnos.value = getAlumnosUseCase()
    }
}

