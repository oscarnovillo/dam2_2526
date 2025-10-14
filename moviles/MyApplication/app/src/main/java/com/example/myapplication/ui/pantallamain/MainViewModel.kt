package com.example.myapplication.ui.pantallamain

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.RepositorioCanciones
import com.example.myapplication.domain.modelo.Cancion
import com.example.myapplication.domain.usecases.Canciones.AddCancionUseCase
import com.example.myapplication.domain.usecases.Canciones.VerCancionUseCase

class MainViewModel(
    private val addCancionUseCase: AddCancionUseCase,
    private val verCancionUseCase: VerCancionUseCase
) : ViewModel() {

    var state: MutableLiveData<MainState> = MutableLiveData()
        private set

    init {
        state.value = MainState(numCanciones = RepositorioCanciones.size())
    }

    fun clickButtonGuardar(cancion: Cancion) {
        if (addCancionUseCase.invoke(cancion)) {
            state.value = state.value?.copy(mensaje = "Cancion añadida", cancion = cancion)
        } else {
            state.value = state.value?.copy(mensaje = "ERROR COMO UNA CASA")
        }
    }

    fun clickButtonPrimer() {
    }

    fun limpiarMensaje() {
        state.value = state.value?.copy(mensaje = null)
    }

    fun pasarCancion() {
        val indice = state.value?.indiceCancion ?: 0
        val cancion = verCancionUseCase.invoke(indice)
        state.value = state.value?.copy(
            cancion = cancion,
            indiceCancion = indice + 1,
            isDisable = indice + 1 > 0
        )
    }
}


/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class MainViewModelFactory(
    private val addCancionUseCase: AddCancionUseCase = AddCancionUseCase(),
    private val verCancionUseCase: VerCancionUseCase = VerCancionUseCase()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                addCancionUseCase,
                verCancionUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}