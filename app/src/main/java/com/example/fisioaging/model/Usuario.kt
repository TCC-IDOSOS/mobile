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
    val healthUnit: HealthUnit? = null
): Serializable

data class HealthUnit(
    val id: Long,
    val name: String?,
    val cnpj: String,
    val phone: String?,
    val email: String?
): Serializable