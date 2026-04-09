package com.example.fisioaging.ui.marcha

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R

class MarchaDetalhesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcha_detalhes)

        val videoView = findViewById<VideoView>(R.id.videoView)

        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.marcha}")

        videoView.setVideoURI(videoUri)

        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = false
        }

        videoView.setOnCompletionListener {
            videoView.postDelayed({
                videoView.start()
            }, 1500)
        }
        videoView.start()

        supportActionBar?.title = "2 Minutos Marcha estacionária"


        val textViewTitulo: TextView = findViewById(R.id.text_titulo_teste)
        val textViewDescricao: TextView = findViewById(R.id.text_descricao_teste)
        val buttonIniciarTeste: Button = findViewById(R.id.button_iniciar_teste)

        buttonIniciarTeste.setOnClickListener {
            val intent = Intent(this, MarchaExecucaoActivity::class.java)
            startActivity(intent)
        }
    }
}