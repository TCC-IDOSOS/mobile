package com.example.fisioaging.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.ui.pacientes.ListaPacientesActivity
import com.example.fisioaging.ui.selecao.SelecaoTesteActivity
import com.example.fisioaging.ui.sincronia.SincroniaActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "FisioAging"


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
}