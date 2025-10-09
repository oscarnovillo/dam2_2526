package com.example.myapplication.ui.pantallamain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.RepositorioCanciones
import com.example.myapplication.domain.modelo.Cancion
import com.example.myapplication.domain.usecases.Canciones.AddCancionUseCase
import com.example.myapplication.domain.usecases.Canciones.VerCancionUseCase

class MainViewModel : ViewModel() {



    private var _state : MutableLiveData<MainState> = MutableLiveData()
    val state : LiveData<MainState> get() = _state


    init {
        _state.value = MainState(numCanciones = RepositorioCanciones.size())
    }

    fun clickButtonGuardar(cancion: Cancion)

    {
        val addCancionUseCase = AddCancionUseCase()

        if (addCancionUseCase.invoke(cancion))
        {
            _state.value = _state.value?.copy(mensaje= "Cancion añadida", cancion = cancion)
        }
        else
        {
            _state.value = _state.value?.copy(mensaje= "ERROR COMO UNA CASA")
        }


    }

    fun clickButtonPrimer()
    {
    }

    fun limpiarMensaje() {
        _state.value = _state.value?.copy(mensaje= null)
    }

    fun pasarCancion() {
        val indice = _state.value?.indiceCancion ?: 0


        val cancion = VerCancionUseCase().invoke(indice)
        _state.value = _state.value?.copy(cancion = cancion, indiceCancion = indice+1,
            isDisable = indice+1>0)

    }

}


/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class MainViewModelFactory(



    ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(

            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}