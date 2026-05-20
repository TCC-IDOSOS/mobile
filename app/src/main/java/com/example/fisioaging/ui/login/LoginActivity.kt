package com.example.fisioaging.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.model.LoginRequest
import com.example.fisioaging.network.RetrofitClient
import com.example.fisioaging.ui.main.MainActivity
import com.example.fisioaging.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sessionManager = SessionManager(this)

        if (sessionManager.fetchAuthToken() != null) {
            iniciarApp()
        }

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            val email = findViewById<EditText>(R.id.edt_email).text.toString()
            val pass = findViewById<EditText>(R.id.edt_password).text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                efetuarLogin(email, pass, sessionManager)
            } else {
                Toast.makeText(this, "Preencha os campos corretamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun efetuarLogin(email: String, pass: String, session: SessionManager) {
        val authService = RetrofitClient.instance

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loginResponse = authService.login(LoginRequest(email, pass))
                val idLogado = loginResponse.userId.toInt()
                val tokenRecebido = loginResponse.token
                val authenticatedService = RetrofitClient.create(tokenRecebido)
                val usuarioCompleto = authenticatedService.getUsuarioById(idLogado)

                withContext(Dispatchers.Main) {
                    session.saveAuthToken(tokenRecebido)
                    session.saveUserId(idLogado)
                    session.saveProfessionalEmail(email)
                    val cnpj = usuarioCompleto.healthUnit?.cnpj ?: ""
                    session.saveHealthUnitCnpj(cnpj)

                    iniciarApp()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace() // Isso vai imprimir no Logcat o motivo exato (ex: 403 Forbidden)
                    Toast.makeText(this@LoginActivity, "Falha na autenticação ou busca de dados", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun iniciarApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}