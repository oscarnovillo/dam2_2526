package com.example.myapplication.ui.pantallalistado

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.domain.usecases.Canciones.GetCancionesUseCase

class ListadoCancionesViewModel(
    private val getCancionesUseCase: GetCancionesUseCase = GetCancionesUseCase()
) : ViewModel() {
    var state: MutableLiveData<ListadoCancionesState> = MutableLiveData()
        private set

    init {
        cargarCanciones()
    }

    fun cargarCanciones() {
        val canciones = getCancionesUseCase()
        state.value = ListadoCancionesState(canciones = canciones)
    }

    fun limpiarMensaje() {
        state.value = state.value?.copy(mensaje = null)
    }
}
