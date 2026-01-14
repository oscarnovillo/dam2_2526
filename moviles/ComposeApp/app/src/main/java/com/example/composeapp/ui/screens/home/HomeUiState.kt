package com.example.composeapp.ui.screens.home

import com.example.composeapp.domain.model.Character

data class HomeUiState(
    val isLoading: Boolean = false,
    val characters: List<Character> = emptyList(),
    val error: String? = null
)

