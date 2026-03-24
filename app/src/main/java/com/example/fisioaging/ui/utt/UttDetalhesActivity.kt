package com.example.fisioaging.ui.utt

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R

class UttDetalhesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utt_detalhes)
        supportActionBar?.title = "Detalhes UTT"

        findViewById<Button>(R.id.btn_iniciar_utt).setOnClickListener {
            startActivity(Intent(this, UttExecucaoActivity::class.java))
            finish()
        }
    }
}