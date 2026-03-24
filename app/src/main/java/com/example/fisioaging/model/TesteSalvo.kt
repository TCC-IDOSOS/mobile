package com.example.fisioaging.model

import java.io.File

data class TesteSalvo(
    val arquivo: File,
    val nomeExibicao: String,
    var isSelecionado: Boolean = false
)