package com.example.fisioaging.ui.utt

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.model.Usuario

class UttDetalhesActivity : AppCompatActivity() {

    private var paciente: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utt_detalhes)

        paciente =
            intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario

        supportActionBar?.title = "Instruções do Teste"
        supportActionBar?.subtitle = null

        val txtNomePaciente =
            findViewById<TextView>(R.id.text_nome_paciente)

        val videoView =
            findViewById<VideoView>(R.id.videoView2)

        val edtPeso =
            findViewById<EditText>(R.id.edt_peso_paciente)

        val btnIniciar =
            findViewById<Button>(R.id.btn_iniciar_utt)

        txtNomePaciente.text =
            "Paciente: ${paciente?.name ?: "Paciente não identificado"}"

        val videoUri =
            Uri.parse(
                "android.resource://${packageName}/${R.raw.utt}"
            )

        videoView.setVideoURI(videoUri)

        videoView.setOnPreparedListener {
            it.isLooping = false
        }

        videoView.setOnCompletionListener {
            videoView.postDelayed({
                videoView.start()
            }, 1500)
        }

        videoView.start()

        btnIniciar.isEnabled = false

        edtPeso.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                btnIniciar.isEnabled =
                    !s.isNullOrBlank()
            }

            override fun afterTextChanged(
                s: Editable?
            ) {
            }
        })

        btnIniciar.setOnClickListener {

            val pesoTexto = edtPeso.text.toString().trim()

            if (pesoTexto.isEmpty()) {
                edtPeso.error = "Informe o peso do paciente"

                Toast.makeText(
                    this,
                    "Não é possível realizar o teste sem informar o peso do paciente.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val pesoDigitado =
                pesoTexto.toDoubleOrNull()

            if (pesoDigitado == null || pesoDigitado <= 0) {

                edtPeso.error = "Peso inválido"

                Toast.makeText(
                    this,
                    "Informe um peso válido para continuar.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val intentExec =
                Intent(this, UttExecucaoActivity::class.java)

            intentExec.putExtra(
                "PACIENTE_SELECIONADO",
                paciente
            )

            intentExec.putExtra(
                "PESO_PACIENTE",
                pesoDigitado
            )

            startActivity(intentExec)
            finish()
        }
    }
}