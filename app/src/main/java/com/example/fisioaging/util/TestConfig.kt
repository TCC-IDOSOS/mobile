package com.example.fisioaging.util

object TestConfig {
    /**
     * Tempo de preparação (contagem regressiva inicial) em milissegundos.
     */
    const val TEMPO_PREPARACAO_MS: Long = 3000L

    /**
     * Duração do teste de Marcha Estacionária em milissegundos.
     */
    const val DURACAO_MARCHA_PADRAO_MS: Long = 120000L // 2 minutos

    /**
     * Duração do teste de Ponta dos Pés (UTT) em milissegundos.
     */
    const val DURACAO_UTT_MS: Long = 30000L // 30 segundos
}
