package com.example.fisioaging.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.ui.login.LoginActivity
import com.example.fisioaging.ui.pacientes.ListaPacientesActivity
import com.example.fisioaging.ui.sincronia.SincroniaActivity
import com.example.fisioaging.util.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "FisioAging"

        sessionManager = SessionManager(this)

        val btnSincronizar: Button = findViewById(R.id.btn_sincronizar)
        val btnPacientes = findViewById<Button>(R.id.btn_listar_pacientes)

        btnSincronizar.setOnClickListener {
            val intent = Intent(this, SincroniaActivity::class.java)
            startActivity(intent)
        }

        btnPacientes.setOnClickListener {
            val intent = Intent(this, ListaPacientesActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                deslogar()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deslogar() {
        sessionManager.clearSession()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}