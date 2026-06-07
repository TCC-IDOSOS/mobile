package com.example.fisioaging.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("FisioAgingPrefs", Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun fetchAuthToken(): String? = prefs.getString("auth_token", null)

    fun saveUserId(userId: Int) {
        prefs.edit().putInt("user_id", userId).apply()
    }

    fun fetchUserId(): Int = prefs.getInt("user_id", 0)

    fun saveProfessionalEmail(email: String) {
        prefs.edit().putString("prof_email", email).apply()
    }

    fun fetchProfessionalEmail(): String = prefs.getString("prof_email", "") ?: ""

    fun saveUserPassword(password: String) {
        prefs.edit().putString("user_password", password).apply()
    }

    fun fetchUserPassword(): String = prefs.getString("user_password", "") ?: ""

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun saveHealthUnitCnpj(cnpj: String) {
        prefs.edit().putString("health_unit_cnpj", cnpj).apply()
    }

    fun fetchHealthUnitCnpj(): String = prefs.getString("health_unit_cnpj", "") ?: ""
}