package com.example.fisioaging.ui.historico

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fisioaging.R
import com.example.fisioaging.model.TesteResponse
import java.text.SimpleDateFormat
import java.util.*

class ResultadoTesteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultado_teste)

        val teste = intent.getSerializableExtra("TESTE_SELECIONADO") as? TesteResponse

        supportActionBar?.title = "Resultado do Teste"

        val txtTipo = findViewById<TextView>(R.id.txt_detalhe_tipo)
        val txtData = findViewById<TextView>(R.id.txt_detalhe_data)
        val txtRepeticoes = findViewById<TextView>(R.id.txt_detalhe_repeticoes)
        val txtStatus = findViewById<TextView>(R.id.txt_detalhe_status)
        val btnVoltar = findViewById<Button>(R.id.btn_voltar_historico)

        teste?.let {
            txtTipo.text = "Tipo: ${if (it.testType == "MARCHA") "Marcha Estacionária" else "Ponta dos Pés (UTT)"}"
            
            txtData.text = "Data: ${formatarData(it.testDateTime)}"
            txtRepeticoes.text = "Repetições: ${it.totalRepetitionsApp}"
            
            txtStatus.text = "Status: ${traduzirStatus(it.status)}"
            val colorRes = when (it.status.uppercase()) {
                "PENDING" -> R.color.status_pending
                "UPLOADED" -> R.color.status_uploaded
                "PROCESSED", "COMPLETED" -> R.color.status_processed
                else -> R.color.black
            }
            txtStatus.setTextColor(ContextCompat.getColor(this, colorRes))
        }

        btnVoltar.setOnClickListener { finish() }
    }

    private fun formatarData(dataBruta: String): String {
        val formatos = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyyMMdd_HHmmss"
        )

        for (formato in formatos) {
            try {
                val inputFormat = SimpleDateFormat(formato, Locale.getDefault())
                if (formato.contains("Z")) {
                    inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = inputFormat.parse(dataBruta)
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                return date?.let { outputFormat.format(it) } ?: dataBruta
            } catch (e: Exception) {
                continue
            }
        }
        return dataBruta
    }

    private fun traduzirStatus(status: String): String {
        return when (status.uppercase()) {
            "UPLOADED" -> "Sincronizado"
            "PENDING" -> "Pendente"
            "PROCESSED" -> "Processado"
            "COMPLETED" -> "Concluído"
            else -> status
        }
    }
}
