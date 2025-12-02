package com.example.navigationhiltroom.ui.common


// Eventos one-shot (incluyen errores)
sealed interface UiEvent {
    data class ShowError(val message: String) : UiEvent
    data class ShowSnackbar(val message: String) : UiEvent
    data object NavigateBack : UiEvent
}