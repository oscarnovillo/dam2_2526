package com.example.navigationhiltroom.ui.primerFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navigationhiltroom.domain.model.Alumno
import com.example.navigationhiltroom.domain.usecase.GetAlumnosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class AlumnosViewModel @Inject constructor(
    private val getAlumnosUseCase: GetAlumnosUseCase) : ViewModel() {

    private val _alumnos = MutableLiveData<List<Alumno>>()
    val alumnos: LiveData<List<Alumno>> = _alumnos

    init {
        loadAlumnos()
    }

    private fun loadAlumnos() {

       viewModelScope.launch {
           try {

             _alumnos.value = getAlumnosUseCase()
           } catch (E: Exception) {


           }


       }
    }
}

