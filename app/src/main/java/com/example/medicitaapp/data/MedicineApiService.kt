package com.example.medicitaapp.data

import retrofit2.http.GET
import retrofit2.http.Query

interface MedicineApiService {
    @GET("resource/i7cb-raxc.json")
    suspend fun getMedicines(
        @Query("\$limit") limit: Int = 500,
        @Query("\$offset") offset: Int = 0
    ): List<MedicineApiResponse>
}