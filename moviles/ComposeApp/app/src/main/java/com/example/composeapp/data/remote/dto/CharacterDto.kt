package com.example.composeapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CharactersResponse(
    val items: List<CharacterDto>,
    val meta: Meta,
    val links: Links
)

data class CharacterDto(
    val id: Int,
    val name: String,
    val ki: String,
    val maxKi: String,
    val race: String,
    val gender: String,
    val description: String,
    val image: String,
    val affiliation: String
)

data class Meta(
    val totalItems: Int,
    val itemCount: Int,
    val itemsPerPage: Int,
    val totalPages: Int,
    val currentPage: Int
)

data class Links(
    val first: String,
    val previous: String?,
    val next: String?,
    val last: String
)

