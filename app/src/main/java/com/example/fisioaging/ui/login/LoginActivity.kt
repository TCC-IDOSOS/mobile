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
            val email = findViewById<EditText>(R.id.edt_email).text.toString().trim()
            val pass = findViewById<EditText>(R.id.edt_password).text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            efetuarLogin(email, pass, sessionManager)
        }
    }

    private fun efetuarLogin(email: String, pass: String, session: SessionManager) {
        val authService = RetrofitClient.instance

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Faz o login para obter o token
                val loginResponse = authService.login(LoginRequest(email, pass))
                val idLogado = loginResponse.userId.toInt()
                val tokenRecebido = loginResponse.token

                // 2. Busca os dados completos do usuário para verificar o perfil
                val authenticatedService = RetrofitClient.create(tokenRecebido)
                val usuarioCompleto = authenticatedService.getUsuarioById(idLogado)

                val perfil = usuarioCompleto.profile.uppercase()

                if (perfil == "PROFISSIONAL" || perfil == "ADMIN") {
                    withContext(Dispatchers.Main) {
                        // 3. Salva os dados da sessão
                        session.saveAuthToken(tokenRecebido)
                        session.saveUserId(idLogado)
                        session.saveProfessionalEmail(usuarioCompleto.email)
                        session.saveUserPassword(pass) // Salva a senha para sincronização offline posterior

                        val cnpj = usuarioCompleto.healthUnit?.cnpj ?: ""
                        session.saveHealthUnitCnpj(cnpj)

                        iniciarApp()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "Acesso restrito para profissionais e administradores", Toast.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                    Toast.makeText(this@LoginActivity, "Falha na autenticação: Verifique seus dados", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun iniciarApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
