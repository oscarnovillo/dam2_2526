package com.example.composeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composeapp.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
}

data class UserFormState(
    val usuarios: List<Usuario> = emptyList(),
    val indiceActual: Int = -1,
    val usuarioActual: Usuario = Usuario()
)

@HiltViewModel
class UserViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UserFormState())
    val uiState: StateFlow<UserFormState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }

    fun updateUsuario(usuario: Usuario) {
        _uiState.update { it.copy(usuarioActual = usuario) }
    }

    fun cargarUsuario(indice: Int) {
        viewModelScope.launch {
            val usuarios = _uiState.value.usuarios
            if (indice >= 0 && indice < usuarios.size) {
                val usuario = usuarios[indice]
                _uiState.update {
                    it.copy(
                        indiceActual = indice,
                        usuarioActual = usuario.copy()
                    )
                }
            }
        }
    }

    fun limpiarFormulario() {
        _uiState.update {
            it.copy(
                usuarioActual = Usuario(),
                indiceActual = -1
            )
        }
    }

    fun guardarUsuario() {
        viewModelScope.launch {
            val state = _uiState.value
            val usuario = state.usuarioActual

            // Validaciones
            if (usuario.nombre.isBlank()) {
                sendEvent(UiEvent.ShowSnackbar("El nombre es obligatorio"))
                return@launch
            }

            if (usuario.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(usuario.email).matches()) {
                sendEvent(UiEvent.ShowSnackbar("El email no es válido"))
                return@launch
            }

            if (usuario.telefono.isNotBlank() && usuario.telefono.length < 9) {
                sendEvent(UiEvent.ShowSnackbar("El teléfono debe tener al menos 9 dígitos"))
                return@launch
            }

            val nuevoUsuario = usuario.copy()
            val nuevaLista = state.usuarios.toMutableList().apply {
                add(nuevoUsuario)
            }

            _uiState.update {
                it.copy(
                    usuarios = nuevaLista,
                    indiceActual = nuevaLista.size - 1
                )
            }

            limpiarFormulario()
            sendEvent(UiEvent.ShowSnackbar("Usuario guardado correctamente"))
        }
    }

    fun actualizarUsuario() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.indiceActual >= 0 && state.indiceActual < state.usuarios.size) {
                val usuario = state.usuarioActual

                // Validaciones
                if (usuario.nombre.isBlank()) {
                    sendEvent(UiEvent.ShowSnackbar("El nombre es obligatorio"))
                    return@launch
                }

                if (usuario.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(usuario.email).matches()) {
                    sendEvent(UiEvent.ShowSnackbar("El email no es válido"))
                    return@launch
                }

                if (usuario.telefono.isNotBlank() && usuario.telefono.length < 9) {
                    sendEvent(UiEvent.ShowSnackbar("El teléfono debe tener al menos 9 dígitos"))
                    return@launch
                }

                val usuarioActualizado = usuario.copy()
                val nuevaLista = state.usuarios.toMutableList().apply {
                    set(state.indiceActual, usuarioActualizado)
                }

                _uiState.update {
                    it.copy(usuarios = nuevaLista)
                }

                sendEvent(UiEvent.ShowSnackbar("Usuario actualizado correctamente"))
            } else {
                sendEvent(UiEvent.ShowSnackbar("No hay usuario seleccionado para actualizar"))
            }
        }
    }

    fun borrarUsuario() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.indiceActual >= 0 && state.indiceActual < state.usuarios.size) {
                val nuevaLista = state.usuarios.toMutableList().apply {
                    removeAt(state.indiceActual)
                }

                if (nuevaLista.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            usuarios = nuevaLista,
                            indiceActual = -1,
                            usuarioActual = Usuario()
                        )
                    }
                } else {
                    val nuevoIndice = if (state.indiceActual >= nuevaLista.size) {
                        nuevaLista.size - 1
                    } else {
                        state.indiceActual
                    }

                    _uiState.update {
                        it.copy(
                            usuarios = nuevaLista,
                            indiceActual = nuevoIndice
                        )
                    }

                    cargarUsuario(nuevoIndice)
                }

                sendEvent(UiEvent.ShowSnackbar("Usuario borrado correctamente"))
            } else {
                sendEvent(UiEvent.ShowSnackbar("No hay usuario seleccionado para borrar"))
            }
        }
    }

    fun navegarAnterior() {
        val state = _uiState.value
        if (state.indiceActual > 0) {
            cargarUsuario(state.indiceActual - 1)
        }
    }

    fun navegarSiguiente() {
        val state = _uiState.value
        if (state.indiceActual < state.usuarios.size - 1) {
            cargarUsuario(state.indiceActual + 1)
        }
    }
}

