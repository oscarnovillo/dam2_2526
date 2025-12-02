package com.example.navigationhiltroom.ui.rickymorty

// Intenciones del usuario
sealed interface RickMortyIntent {
    data object LoadCharacters : RickMortyIntent
    data class LoadPage(val page: Int) : RickMortyIntent
    data class SearchCharacters(val name: String) : RickMortyIntent
   // data object ClearError : RickMortyIntent
}