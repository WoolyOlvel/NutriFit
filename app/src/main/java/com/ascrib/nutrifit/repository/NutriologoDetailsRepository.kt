package com.ascrib.nutrifit.repository

import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.NutriologoDetailResponse
import com.ascrib.nutrifit.api.models.NutriologoDetailsResponse
import retrofit2.Response

class NutriologoDetailsRepository {
    suspend fun getNutriologoDetallesById(userId: Int): Response<NutriologoDetailsResponse> {
        return RetrofitClient.apiService.getNutriologoDetallesById(userId)
    }
}