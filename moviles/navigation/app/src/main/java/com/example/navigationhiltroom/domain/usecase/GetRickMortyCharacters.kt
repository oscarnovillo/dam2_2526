package com.example.navigationhiltroom.domain.usecase

import com.example.navigationhiltroom.common.NetworkResult
import com.example.navigationhiltroom.data.RickMortyRepository
import com.example.navigationhiltroom.domain.model.RickMortyCharacter
import com.example.navigationhiltroom.domain.model.isPageMayorCero
import com.example.navigationhiltroom.domain.model.isValidPage
import jakarta.inject.Inject

class GetRickMortyCharacters @Inject constructor(private val repository: RickMortyRepository) {

    operator suspend fun invoke(page: Int): NetworkResult<List<RickMortyCharacter>> =
        isValidPage(page)
            .then  { isPageMayorCero(page) }
            .then { repository.getCharacters(page) }


}
