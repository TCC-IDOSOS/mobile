package com.example.fisioaging.ui.marcha

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.model.Usuario

class MarchaDetalhesActivity : AppCompatActivity() {

    private var paciente: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcha_detalhes)

        paciente = intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario

        val videoView = findViewById<VideoView>(R.id.videoView)
        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.marcha}")
        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener { mediaPlayer -> mediaPlayer.isLooping = false }
        videoView.setOnCompletionListener {
            videoView.postDelayed({ videoView.start() }, 1500)
        }
        videoView.start()

        supportActionBar?.title = "2 Minutos Marcha estacionária"

        if (paciente != null) {
            supportActionBar?.subtitle = "Paciente: ${paciente?.name}"
        }

        val buttonIniciarTeste: Button = findViewById(R.id.button_iniciar_teste)

        buttonIniciarTeste.setOnClickListener {
            val intentExecucao = Intent(this, MarchaExecucaoActivity::class.java)

            intentExecucao.putExtra("PACIENTE_SELECIONADO", paciente)

            startActivity(intentExecucao)
            finish()
        }
    }
}