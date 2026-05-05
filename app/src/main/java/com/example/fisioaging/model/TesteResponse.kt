package com.example.fisioaging.model

import java.io.Serializable

data class TesteResponse(
    val id: Long,
    val testType: String,
    val testDateTime: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val totalRepetitionsApp: Int,
    val rawS3Key: String?,
    val processedS3Key: String?
) : Serializable
