package com.example.composeapp.data.remote

import com.example.composeapp.data.remote.dto.CharactersResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DragonBallApi {
    @GET("characters")
    suspend fun getCharacters(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): CharactersResponse
}

