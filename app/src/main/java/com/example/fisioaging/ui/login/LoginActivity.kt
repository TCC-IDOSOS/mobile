package com.example.fisioaging.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val animation = AnimationUtils.loadAnimation(this, R.anim.splash_anim)
            animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}

                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    splashScreenView.remove()
                }

                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
            splashScreenView.iconView.startAnimation(animation)
        }

        val sessionManager = SessionManager(this)

        if (sessionManager.fetchAuthToken() != null) {
            iniciarApp()
        }

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            val email = findViewById<EditText>(R.id.edt_email).text.toString().trim()
            val pass = findViewById<EditText>(R.id.edt_password).text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this, "O E-mail é obrigatório", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.isEmpty()) {
                Toast.makeText(this, "A senha é obrigatória", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            efetuarLogin(email, pass, sessionManager)
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

                val perfil = usuarioCompleto.profile?.uppercase() ?: ""

                if (perfil == "PROFISSIONAL" || perfil == "ADMIN") {
                    withContext(Dispatchers.Main) {
                        session.saveAuthToken(tokenRecebido)
                        session.saveUserId(idLogado)
                        session.saveProfessionalEmail(usuarioCompleto.email ?: email)

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

                    val errorMessage = e.message ?: ""
                    if (errorMessage.contains("404") || errorMessage.contains("not found", ignoreCase = true)) {
                        Toast.makeText(this@LoginActivity, "O E-mail inválido", Toast.LENGTH_LONG).show()
                    } else if (errorMessage.contains("401") || errorMessage.contains("unauthorized", ignoreCase = true)) {
                        Toast.makeText(this@LoginActivity, "Senha inválida", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@LoginActivity, "Falha na autenticação ou busca de dados", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun iniciarApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}