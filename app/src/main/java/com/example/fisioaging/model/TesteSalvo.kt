package com.example.fisioaging.model

import java.io.File
import java.io.Serializable

data class TesteSalvo(
    val arquivo: File,
    val nomeExibicao: String,
    val idPaciente: Long,
    val nomePaciente: String,
    val emailPaciente: String,
    val tipoTeste: String,
    var isSelecionado: Boolean = false
) : Serializable