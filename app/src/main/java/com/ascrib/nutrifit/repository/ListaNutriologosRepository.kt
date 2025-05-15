package com.ascrib.nutrifit.repository

import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.ListaNutriologosResponse
import retrofit2.Response

class ListaNutriologosRepository {
    suspend fun getNutriologos(): Response<ListaNutriologosResponse> {
        return RetrofitClient.apiService.getNutriologos()
    }
}