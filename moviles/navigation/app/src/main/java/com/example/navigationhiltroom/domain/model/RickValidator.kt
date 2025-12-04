package com.example.navigationhiltroom.domain.model

import com.example.navigationhiltroom.common.NetworkResult

fun isValidPage(page: Int): NetworkResult<Boolean> {
        return  if (page < 0)  NetworkResult.Error("Page number must be non-negative") else  NetworkResult.Success(true)
    }

fun isPageMayorCero(page: Int): NetworkResult<Boolean> {
    return  if (page >42)  NetworkResult.Error("Page number must be non-negative") else  NetworkResult.Success(true)
}
