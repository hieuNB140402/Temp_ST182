package com.audio.example.core.service
import com.audio.example.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("/api/ST177_FingerPlay")
    suspend fun getAllData(): Response<List<PartAPI>>
}