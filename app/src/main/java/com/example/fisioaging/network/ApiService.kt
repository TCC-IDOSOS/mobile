package com.example.fisioaging.network

import com.example.fisioaging.model.TesteRequest
import com.example.fisioaging.model.Usuario
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query


interface ApiService {
    @GET("users/all")
    suspend fun getUsuarios(): List<Usuario>

    @POST("users/tests")
    suspend fun enviarTeste(
        @Query("email") email: String,
        @Body body: TesteRequest
    ): Response<Unit>
}