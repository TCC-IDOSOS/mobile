package com.example.fisioaging.network

import com.example.fisioaging.model.Usuario
import retrofit2.http.GET

interface ApiService {
    @GET("users/all")
    suspend fun getUsuarios(): List<Usuario>
}