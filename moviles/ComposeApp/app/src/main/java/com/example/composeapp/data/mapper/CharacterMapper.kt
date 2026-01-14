package com.example.composeapp.data.mapper

import com.example.composeapp.data.remote.dto.CharacterDto
import com.example.composeapp.domain.model.Character

fun CharacterDto.toDomain(): Character {
    return Character(
        id = id,
        name = name,
        ki = ki,
        maxKi = maxKi,
        race = race,
        gender = gender,
        description = description,
        image = image,
        affiliation = affiliation
    )
}

