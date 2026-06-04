package com.example.fisioaging.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authHeaderValue = if (token.startsWith("Bearer ", ignoreCase = true)) {
            token
        } else {
            "Bearer $token"
        }

        val request = chain.request().newBuilder()
            .addHeader("Authorization", authHeaderValue)
            .build()

        return chain.proceed(request)
    }
}