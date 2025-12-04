package com.example.navigationhiltroom.data

import com.example.navigationhiltroom.common.NetworkResult
import com.example.navigationhiltroom.data.remote.api.RickMortyApiService
import com.example.navigationhiltroom.domain.model.RickMortyCharacter
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RickMortyRepository @Inject constructor(
    private val apiService: RickMortyApiService
) {

    suspend fun getCharacters(page: Int = 1): NetworkResult<List<RickMortyCharacter>> {
        try {
            val response = apiService.getCharacters(page)
            return NetworkResult.Success(response.results)
        } catch (e: HttpException) {
            return NetworkResult.Error("Error ${e.code()} }")
        }
    }

    suspend fun searchCharacters(name: String, page: Int = 1): List<RickMortyCharacter> {
        try {

            val response = apiService.searchCharacters(name, page)

            if (response.isSuccessful) {
                return (response.body()?.results ?: emptyList())
            } else {
                response.errorBody() // esto se parsearia
                response.code()
                response.message()
                return emptyList();
            }
        } catch (e: Exception) {

            return emptyList()
        }

    }
}