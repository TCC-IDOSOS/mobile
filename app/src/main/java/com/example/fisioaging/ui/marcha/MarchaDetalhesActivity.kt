package com.example.fisioaging.ui.marcha

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.model.Usuario

class MarchaDetalhesActivity : AppCompatActivity() {

    private var paciente: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcha_detalhes)

        paciente =
            intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario

        supportActionBar?.title = "Instruções do Teste"
        supportActionBar?.subtitle = null

        val txtNomePaciente =
            findViewById<TextView>(R.id.text_nome_paciente)

        val videoView =
            findViewById<VideoView>(R.id.videoView)

        val buttonIniciarTeste =
            findViewById<Button>(R.id.button_iniciar_teste)

        txtNomePaciente.text =
            "Paciente: ${paciente?.name ?: "Paciente não identificado"}"

        val videoUri =
            Uri.parse(
                "android.resource://${packageName}/${R.raw.marcha}"
            )

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

        buttonIniciarTeste.setOnClickListener {

            val intentExecucao =
                Intent(
                    this,
                    MarchaExecucaoActivity::class.java
                )

            intentExecucao.putExtra(
                "PACIENTE_SELECIONADO",
                paciente
            )

            startActivity(intentExecucao)
            finish()
        }
    }
}