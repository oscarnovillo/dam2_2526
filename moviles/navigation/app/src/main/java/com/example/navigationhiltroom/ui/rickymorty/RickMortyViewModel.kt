package com.example.navigationhiltroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navigationhiltroom.common.NetworkResult
import com.example.navigationhiltroom.data.RickMortyRepository

import com.example.navigationhiltroom.domain.usecase.GetRickMortyCharacters
import com.example.navigationhiltroom.ui.common.UiEvent
import com.example.navigationhiltroom.ui.rickymorty.RickMortyIntent
import com.example.navigationhiltroom.ui.rickymorty.RickMortyUiState

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RickMortyViewModel @Inject constructor(
    private val repository: RickMortyRepository,
    private val getCharactersUseCase: GetRickMortyCharacters,

) : ViewModel() {

    private val _uiState = MutableStateFlow(RickMortyUiState())
    val uiState: StateFlow<RickMortyUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()



    init {
        handleIntent(RickMortyIntent.LoadCharacters)
    }



    fun handleIntent(intent: RickMortyIntent) {
        when (intent) {
            is RickMortyIntent.LoadCharacters -> loadCharacters(1)
            is RickMortyIntent.LoadPage -> loadCharacters(intent.page)
            is RickMortyIntent.SearchCharacters -> searchCharacters(intent.name)
//            is RickMortyIntent.ClearError -> clearError()
        }
    }


    private fun loadCharacters(page: Int = 1) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading= true) }

            val result = getCharactersUseCase(page)
            when (result)
            {
                is NetworkResult.Error -> {
                    _uiState.update { it.copy( isLoading= false) }
                    _events.send(UiEvent.ShowSnackbar(result.message ?: "Unknown Error"))
                }
                is NetworkResult.Loading -> TODO()
                is NetworkResult.Success ->
                    _uiState.update { it.copy( characters = result.data  , isLoading= false) }
            }


        }
    }

    private fun searchCharacters(name: String) {
        if (name.isBlank()) {
            loadCharacters()
            return
        }

        viewModelScope.launch {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading= true) }
                _uiState.update { it.copy( characters =repository.searchCharacters(name)  , isLoading= false) }
            }
        }
    }
}

