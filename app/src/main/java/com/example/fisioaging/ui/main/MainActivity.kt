package com.example.fisioaging.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.ui.selecao.SelecaoTesteActivity
import com.example.fisioaging.ui.sincronia.SincroniaActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = "FisioAging"

        val btnNovoTeste: Button = findViewById(R.id.btn_novo_teste)
        val btnSincronizar: Button = findViewById(R.id.btn_sincronizar)

        btnNovoTeste.setOnClickListener {
            val intent = Intent(this, SelecaoTesteActivity::class.java)
            startActivity(intent)
        }

        btnSincronizar.setOnClickListener {
            val intent = Intent(this, SincroniaActivity::class.java)
            startActivity(intent)
        }
    }
}