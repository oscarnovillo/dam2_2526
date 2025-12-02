package com.example.navigationhiltroom.data.remote.api

import com.example.navigationhiltroom.data.remote.entity.RickMortyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface RickMortyApiService {

    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int = 1
    ): RickMortyResponse

    @GET("character")
    suspend fun searchCharacters(
        @Query("name") name: String,
        @Query("page") page: Int = 1
    ): Response<RickMortyResponse>
}

