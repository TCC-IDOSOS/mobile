package com.example.fisioaging.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RelatorioMarchaResponse(
    @SerializedName("n_peaks") val nPeaks: Int? = null,
    @SerializedName("strategy") val strategy: String? = null,
    @SerializedName("cadence_cycles_min") val cadenceCyclesMin: Double? = null,
    @SerializedName("vel_ini_deg_s") val velIniDegS: Double? = null,
    @SerializedName("vel_end_deg_s") val velEndDegS: Double? = null,
    @SerializedName("slope_deg_s2") val slopeDegS2: Double? = null,
    @SerializedName("vel_mean_deg_s") val velMeanDegS: Double? = null,
    @SerializedName("vel_std_deg_s") val velStdDegS: Double? = null,
    @SerializedName("cv_vel") val cvVel: Double? = null,
    @SerializedName("vel_max_deg_s") val velMaxDegS: Double? = null,
    @SerializedName("vel_min_deg_s") val velMinDegS: Double? = null,
    @SerializedName("time_mean_s") val timeMeanS: Double? = null,
    @SerializedName("time_std_s") val timeStdS: Double? = null,
    @SerializedName("cv_time") val cvTime: Double? = null,
    @SerializedName("time_max_s") val timeMaxS: Double? = null,
    @SerializedName("time_min_s") val timeMinS: Double? = null,
    @SerializedName("cycles") val cycles: List<MarchaCycle>? = null,
    @SerializedName("t_s") val tS: List<Double>? = null,
    @SerializedName("signal_deg_s") val signalDegS: List<Double>? = null,
    @SerializedName("detect_signal") val detectSignal: String? = null,
    @SerializedName("peaks_t_s") val peaksTS: List<Double>? = null,
    @SerializedName("peaks_value_deg_s") val peaksValueDegS: List<Double>? = null,
    @SerializedName("peaks_pred_deg_s") val peaksPredDegS: List<Double>? = null,
    @SerializedName("subject_sex") val subjectSex: String? = null,
    @SerializedName("subject_age") val subjectAge: Int? = null,
    @SerializedName("subject_id") val subjectId: String? = null,
    @SerializedName("model_target_mode") val modelTargetMode: String? = null,
    @SerializedName("status") val status: String? = null
) : Serializable

data class MarchaCycle(
    @SerializedName("peak_idx") val peakIdx: Int,
    @SerializedName("t_peak_s") val tPeakS: Double,
    @SerializedName("w_phoneX_peak_deg_s") val wPhoneXPeakDegS: Double,
    @SerializedName("w_phoneY_peak_deg_s") val wPhoneYPeakDegS: Double,
    @SerializedName("w_phoneZ_peak_deg_s") val wPhoneZPeakDegS: Double,
    @SerializedName("w_pred_deg_s") val wPredDegS: Double
) : Serializable
