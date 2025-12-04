package com.example.navigationhiltroom.ui.rickymorty

import com.example.navigationhiltroom.domain.model.RickMortyCharacter

// Estado persistente de la pantalla
data class RickMortyUiState(
    val characters: List<RickMortyCharacter> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)
