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
    val nombre: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val email: String = "",
    val fechaNacimiento: String = "",
    val generoSeleccionado: String = "",
    val comentarios: String = "",
    val tieneTV: Boolean = false
)

@HiltViewModel
class UserViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UserFormState())
    val uiState: StateFlow<UserFormState> = _uiState.asStateFlow()

    fun updateNombre(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }

    fun updateApellidos(apellidos: String) {
        _uiState.update { it.copy(apellidos = apellidos) }
    }

    fun updateTelefono(telefono: String) {
        _uiState.update { it.copy(telefono = telefono) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updateFechaNacimiento(fecha: String) {
        _uiState.update { it.copy(fechaNacimiento = fecha) }
    }

    fun updateGenero(genero: String) {
        _uiState.update { it.copy(generoSeleccionado = genero) }
    }

    fun updateComentarios(comentarios: String) {
        _uiState.update { it.copy(comentarios = comentarios) }
    }

    fun updateTieneTV(tieneTV: Boolean) {
        _uiState.update { it.copy(tieneTV = tieneTV) }
    }

    fun cargarUsuario(indice: Int) {
        viewModelScope.launch {
            val usuarios = _uiState.value.usuarios
            if (indice >= 0 && indice < usuarios.size) {
                val usuario = usuarios[indice]
                _uiState.update {
                    it.copy(
                        indiceActual = indice,
                        nombre = usuario.nombre,
                        apellidos = usuario.apellidos,
                        telefono = usuario.telefono,
                        email = usuario.email,
                        fechaNacimiento = usuario.fechaNacimiento,
                        generoSeleccionado = usuario.genero,
                        comentarios = usuario.comentarios,
                        tieneTV = usuario.tieneTV
                    )
                }
            }
        }
    }

    fun limpiarFormulario() {
        _uiState.update {
            it.copy(
                nombre = "",
                apellidos = "",
                telefono = "",
                email = "",
                fechaNacimiento = "",
                generoSeleccionado = "",
                comentarios = "",
                tieneTV = false,
                indiceActual = -1
            )
        }
    }

    fun guardarUsuario() {
        viewModelScope.launch {
            val state = _uiState.value
            val nuevoUsuario = Usuario(
                nombre = state.nombre,
                apellidos = state.apellidos,
                telefono = state.telefono,
                email = state.email,
                fechaNacimiento = state.fechaNacimiento,
                genero = state.generoSeleccionado,
                comentarios = state.comentarios,
                tieneTV = state.tieneTV
            )

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
                val usuarioActualizado = Usuario(
                    nombre = state.nombre,
                    apellidos = state.apellidos,
                    telefono = state.telefono,
                    email = state.email,
                    fechaNacimiento = state.fechaNacimiento,
                    genero = state.generoSeleccionado,
                    comentarios = state.comentarios,
                    tieneTV = state.tieneTV
                )

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
                            nombre = "",
                            apellidos = "",
                            telefono = "",
                            email = "",
                            fechaNacimiento = "",
                            generoSeleccionado = "",
                            comentarios = "",
                            tieneTV = false
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

