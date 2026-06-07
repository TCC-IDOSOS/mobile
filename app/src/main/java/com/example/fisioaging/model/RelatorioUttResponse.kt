package com.example.fisioaging.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RelatorioUttResponse(
    @SerializedName("total_repeticoes") val totalRepeticoes: Int? = null,
    @SerializedName("repeticoes_completas") val repeticoesCompletas: Int? = null,
    @SerializedName("percentual_completas") val percentualCompletas: Double? = null,
    @SerializedName("altura_media") val alturaMedia: Double? = null,
    @SerializedName("cadencia") val cadencia: Double? = null,
    @SerializedName("amplitude_maxima_oscilacao") val amplitudeMaximaOscilacao: Double? = null,
    @SerializedName("tempo_total_execucao") val tempoTotalExecucao: Double? = null,
    @SerializedName("desvio_padrao_aceleracoes") val desvioPadraoAceleracoes: Double? = null,
    @SerializedName("velocidade_media_oscilacao") val velocidadeMediaOscilacao: Double? = null,
    @SerializedName("indice_estabilidade") val indiceEstabilidade: Double? = null,
    @SerializedName("classificacao") val classificacao: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("cycles") val cycles: List<UttCycle>? = null,
    @SerializedName("t_s") val tS: List<Double>? = null,
    @SerializedName("displacement_cm") val displacementCm: List<Double>? = null,
    @SerializedName("peaks_t_s") val peaksTS: List<Double>? = null,
    @SerializedName("peaks_value_cm") val peaksValueCm: List<Double>? = null,
    @SerializedName("valleys_t_s") val valleysTS: List<Double>? = null,
    @SerializedName("valleys_value_cm") val valleysValueCm: List<Double>? = null,
    @SerializedName("peak_freq_full_hz") val peakFreqFullHz: Double? = null,
    @SerializedName("peak_freq_cut_hz") val peakFreqCutHz: Double? = null,
    @SerializedName("fc_f") val fcF: Double? = null,
    @SerializedName("nyq_f") val nyqF: Double? = null
) : Serializable

data class UttCycle(
    @SerializedName("ciclo") val ciclo: Int,
    @SerializedName("amplitude_cm") val amplitudeCm: Double,
    @SerializedName("velocidade_subida_cm_s") val velocidadeSubidaCmS: Double,
    @SerializedName("tempo_subida_s") val tempoSubidaS: Double,
    @SerializedName("tempo_ciclo_s") val tempoCicloS: Double,
    @SerializedName("potencia_w") val potenciaW: Double,
    @SerializedName("trabalho_j") val trabalhoJ: Double
) : Serializable
