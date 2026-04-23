package com.example.fisioaging.model

data class TesteRequest(
    val tipo_teste: String,
    val data_hora: String,
    val sensor: String,
    val frequencia: Int,
    val total_repeticoes_app: Int,
    val sexo: String,
    val idade: Int,
    val massa_kg: Double,
    val registros: List<Registro>
)

data class Registro(
    val time: Int,
    val x: Double,
    val y: Double,
    val z: Double
)