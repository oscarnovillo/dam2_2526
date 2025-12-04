package com.example.navigationhiltroom.data.remote.api

import com.example.navigationhiltroom.domain.model.RickMortyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface RickMortyApiService {

    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int = 1): RickMortyResponse

    @GET("character")
    suspend fun searchCharacters(
        @Query("name") name: String,
        @Query("page") page: Int = 1
    ): Response<RickMortyResponse>



//    @GET("/users/{id}")
//    suspend fun getUser(@Path("id") id : Int) : Response<UserRemote>
//
//    @POST("/users")
//    suspend fun postUser(@Body user: UserRemote) : Response<UserRemote>
//
//    @PUT("/users/{id}")
//    suspend fun putUser(@Path("id") id : Int,@Body user: UserRemote) : Response<UserRemote>
//
//
//    @DELETE("/users/{id}")
//    suspend fun delUser(@Path("id") id : Int) : Response<Unit>

}

