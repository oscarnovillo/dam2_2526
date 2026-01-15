package com.example.composeapp.data.repository

import com.example.composeapp.data.mapper.toDomain
import com.example.composeapp.data.remote.DragonBallApi
import com.example.composeapp.domain.model.Character
import javax.inject.Inject

class CharacterRepository @Inject constructor(
    private val api: DragonBallApi
) {
    suspend fun getCharacters(): Result<List<Character>> {
        return try {
            val response = api.getCharacters()
            Result.success(response.items.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

