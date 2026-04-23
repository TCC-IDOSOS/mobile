package com.example.fisioaging.model

import java.io.Serializable

data class Usuario(
    val id: Long,
    val name: String,
    val profile: String,
    val email: String,
    val cpf: String,
    val genre: String,
    val birthDate: String,
): Serializable