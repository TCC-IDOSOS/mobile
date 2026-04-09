package com.example.fisioaging.ui.selecao

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.ui.marcha.MarchaDetalhesActivity
import com.example.fisioaging.ui.utt.UttDetalhesActivity

class SelecaoTesteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selecao_teste)

        supportActionBar?.title = "Selecionar Avaliação"

        val btnMarcha = findViewById<Button>(R.id.btn_marcha)
        val btnUtt = findViewById<Button>(R.id.btn_utt)

        btnMarcha.setOnClickListener {
            startActivity(Intent(this, MarchaDetalhesActivity::class.java))
        }

        btnUtt.setOnClickListener {
            startActivity(Intent(this, UttDetalhesActivity::class.java))
        }
    }
}