package com.example.fisioaging.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.ui.sincronia.SincroniaActivity
import com.example.fisioaging.ui.marcha.MarchaDetalhesActivity
import com.example.fisioaging.ui.utt.UttDetalhesActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Define o título da barra superior
        supportActionBar?.title = "Novo Teste"

        val cardMarcha: TextView = findViewById(R.id.card_marcha)
        val cardPontaPes: TextView = findViewById(R.id.card_ponta_pes)

        // 1. Clique no card de Marcha Estacionária
        cardMarcha.setOnClickListener {
            val intent = Intent(this, MarchaDetalhesActivity::class.java)
            startActivity(intent)
        }

        // 2. Clique no card "Na ponta dos pés" (Teste UTT)
        cardPontaPes.setOnClickListener {
            val intent = Intent(this, UttDetalhesActivity::class.java)
            startActivity(intent)
        }
    }

    // --- LÓGICA DO MENU DROPDOWN (Canto Superior Direito) ---

    // Este método "infla" o arquivo XML de menu que você criou
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Este método trata o clique no item "Sincronizar" do menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sincronizar -> {
                // Aqui chamamos a sua SincroniaActivity
                val intent = Intent(this, SincroniaActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}