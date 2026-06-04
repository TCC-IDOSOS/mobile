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
import com.example.fisioaging.util.TestConfig

class MarchaDetalhesActivity : AppCompatActivity() {

    private fun formatDurationLabel(durationMs: Long): String {
        val seconds = durationMs / 1000
        val minutes = seconds / 60
        val remainderSeconds = seconds % 60

        return if (minutes > 0) {
            if (remainderSeconds > 0) {
                "Duração: ${minutes} minuto${if (minutes > 1) "s" else ""} e ${remainderSeconds} segundos"
            } else {
                "Duração: ${minutes} minuto${if (minutes > 1) "s" else ""}"
            }
        } else {
            "Duração: ${remainderSeconds} segundos"
        }
    }

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

        val txtDuracaoTeste =
            findViewById<TextView>(R.id.text_duracao_teste)

        val videoView =
            findViewById<VideoView>(R.id.videoView)

        val buttonIniciarTeste =
            findViewById<Button>(R.id.button_iniciar_teste)

        txtNomePaciente.text =
            "Paciente: ${paciente?.name ?: "Paciente não identificado"}"

        txtDuracaoTeste.text = formatDurationLabel(TestConfig.DURACAO_MARCHA_PADRAO_MS)

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