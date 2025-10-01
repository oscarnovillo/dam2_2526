package com.example.myapplication.ui.pantallamain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.domain.usecases.Canciones.VerCancionUseCase

class MainViewModel : ViewModel() {



    private var _state : MutableLiveData<MainState> = MutableLiveData(MainState())
    val state : LiveData<MainState> get() = _state


    fun clickButtonUno()
    {
        val useCase = VerCancionUseCase()


        _state.value = state.value?.copy(
            textoLabel = useCase.invoke(0).titulo,

        )
    }

    fun clickButtonPrimer()
    {
        _state.value = state.value?.copy(textoLabel = "kkk", textoCaja = "texto2")
       // _state.value = MainState(textoCaja = "texto2",textoLabel = "kkk", )
//        _state.value?.textoLabel = "kkk"
//        _state.value?.textoCaja = "texto2"
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