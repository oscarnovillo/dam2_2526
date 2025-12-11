package com.example.composeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composeapp.Usuario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserFormState(
    val usuarios: List<Usuario> = emptyList(),
    val indiceActual: Int = -1,
    val usuarioActual: Usuario = Usuario()
)

@HiltViewModel
class UserViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UserFormState())
    val uiState: StateFlow<UserFormState> = _uiState.asStateFlow()

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
            val nuevoUsuario = state.usuarioActual.copy()

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
        }
    }

    fun actualizarUsuario() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.indiceActual >= 0 && state.indiceActual < state.usuarios.size) {
                val usuarioActualizado = state.usuarioActual.copy()

                val nuevaLista = state.usuarios.toMutableList().apply {
                    set(state.indiceActual, usuarioActualizado)
                }

                _uiState.update {
                    it.copy(usuarios = nuevaLista)
                }
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

