package com.example.navigationhiltroom.data.remote.entity


data class RickMortyCharacter(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: Location,
    val location: Location,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
)

data class Location(
    val name: String,
    val url: String
)

data class RickMortyResponse(
    val info: Info,
    val results: List<RickMortyCharacter>
)

data class Info(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)

