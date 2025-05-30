package com.ascrib.nutrifit.repository

import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.NutriologoDetailResponse
import retrofit2.Response

class NutriologoDetailRepository {
    suspend fun getNutriologoById(userId: Int): Response<NutriologoDetailResponse> {
        return RetrofitClient.apiService.getNutriologoById(userId)
    }
}