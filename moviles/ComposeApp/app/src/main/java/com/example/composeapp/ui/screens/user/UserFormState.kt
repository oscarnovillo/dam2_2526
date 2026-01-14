package com.example.composeapp.ui.screens.user

import com.example.composeapp.domain.model.Usuario

data class UserFormState(
    val usuarios: List<Usuario> = emptyList(),
    val indiceActual: Int = -1,
    val usuarioActual: Usuario = Usuario()
)