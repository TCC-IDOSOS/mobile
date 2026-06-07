package com.example.fisioaging.ui.historico

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fisioaging.R
import com.example.fisioaging.model.RelatorioMarchaResponse
import com.example.fisioaging.model.RelatorioUttResponse
import com.example.fisioaging.model.TesteResponse
import com.example.fisioaging.model.Usuario
import com.example.fisioaging.network.RetrofitClient
import com.example.fisioaging.util.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class ResultadoTesteActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    private var teste: TesteResponse? = null
    private var paciente: Usuario? = null

    private lateinit var txtTituloResultado: TextView
    private lateinit var txtData: TextView
    private lateinit var txtStatus: TextView

    private lateinit var layoutResultados: LinearLayout
    private lateinit var txtMsgProcessamento: TextView
    private lateinit var progressRelatorio: ProgressBar
    private lateinit var btnVoltar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resultado_teste)

        sessionManager = SessionManager(this)

        teste = intent.getSerializableExtra("TESTE_SELECIONADO") as? TesteResponse
        paciente = intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario

        supportActionBar?.title = "Resultado do Teste"

        iniciarViews()
        preencherCabecalho()

        btnVoltar.setOnClickListener {
            finish()
        }

        teste?.let {
            if (it.status.uppercase() != "PENDING" && it.id > 0) {
                buscarRelatorio()
            } else {
                txtMsgProcessamento.visibility = View.VISIBLE
                txtMsgProcessamento.text = "Resultados ainda em processamento."
            }
        }
    }

    private fun iniciarViews() {
        txtTituloResultado = findViewById(R.id.txt_titulo_resultado)
        txtData = findViewById(R.id.txt_detalhe_data)
        txtStatus = findViewById(R.id.txt_detalhe_status)
        layoutResultados = findViewById(R.id.layout_resultados_analise)
        txtMsgProcessamento = findViewById(R.id.txt_msg_processamento)
        progressRelatorio = findViewById(R.id.progress_loading_relatorio)
        btnVoltar = findViewById(R.id.btn_voltar_historico)
    }

    private fun preencherCabecalho() {
        teste?.let {
            val nomeTeste = if (it.testType == "MARCHA") "Marcha Estacionária" else "Ponta dos Pés (UTT)"
            // Removida a exibição das repetições contadas pelo app localmente
            txtTituloResultado.text = nomeTeste
            txtData.text = formatarDataParaExibicao(it.testDateTime)
            txtStatus.text = traduzirStatus(it.status)

            val corStatus = when (it.status.uppercase()) {
                "PENDING" -> R.color.status_pending
                "UPLOADED" -> R.color.status_uploaded
                "PROCESSED", "COMPLETED" -> R.color.status_processed
                else -> R.color.black
            }
            txtStatus.setTextColor(ContextCompat.getColor(this, corStatus))
        }
    }

    private fun buscarRelatorio() {
        val t = teste ?: return
        val p = paciente ?: return

        progressRelatorio.visibility = View.VISIBLE
        txtMsgProcessamento.visibility = View.GONE
        layoutResultados.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sessionManager.fetchAuthToken()
                val service = RetrofitClient.create(token)
                val jsonElement = service.getRelatorioTeste(t.id, p.email)

                withContext(Dispatchers.Main) {
                    progressRelatorio.visibility = View.GONE
                    val gson = Gson()
                    val jsonObject = jsonElement.asJsonObject

                    val status = jsonObject.get("status")?.asString
                    if (status == "processing") {
                        txtMsgProcessamento.visibility = View.VISIBLE
                        txtMsgProcessamento.text = "Resultados ainda estão sendo processados."
                    } else {
                        if (t.testType == "MARCHA") {
                            val relatorio = gson.fromJson(jsonElement, RelatorioMarchaResponse::class.java)
                            exibirDadosMarcha(relatorio)
                        } else {
                            val relatorio = gson.fromJson(jsonElement, RelatorioUttResponse::class.java)
                            exibirDadosUtt(relatorio)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressRelatorio.visibility = View.GONE
                    txtMsgProcessamento.visibility = View.VISIBLE
                    txtMsgProcessamento.text = "Erro ao buscar resultados."
                    e.printStackTrace()
                }
            }
        }
    }

    private fun exibirDadosUtt(r: RelatorioUttResponse) {
        layoutResultados.visibility = View.VISIBLE
        val locale = Locale("pt", "BR")

        // Aqui exibimos as repetições processadas pela análise (API), que são as oficiais
        findViewById<TextView>(R.id.txt_res_repeticoes_completas).text =
            "Repetições completas: ${r.repeticoesCompletas ?: 0} de ${r.totalRepeticoes ?: 0}"

        findViewById<TextView>(R.id.txt_res_percentual).text =
            "Eficiência: ${r.percentualCompletas?.toInt() ?: 0} %"

        findViewById<TextView>(R.id.txt_res_altura_media).text =
            String.format(locale, "Altura média: %.1f cm", r.alturaMedia ?: 0.0)

        findViewById<TextView>(R.id.txt_res_cadencia).text =
            String.format(locale, "Cadência: %.2f rep/min", r.cadencia ?: 0.0)

        findViewById<TextView>(R.id.txt_res_amplitude).text =
            String.format(locale, "Amplitude máxima: %.2f cm", r.amplitudeMaximaOscilacao ?: 0.0)

        findViewById<TextView>(R.id.txt_res_tempo).text =
            String.format(locale, "Tempo total: %.1f s", r.tempoTotalExecucao ?: 0.0)

        findViewById<TextView>(R.id.txt_res_velocidade).text =
            String.format(locale, "Velocidade média: %.2f cm/s", r.velocidadeMediaOscilacao ?: 0.0)

        findViewById<TextView>(R.id.txt_res_desvio).apply {
            visibility = if (r.desvioPadraoAceleracoes != null) View.VISIBLE else View.GONE
            text = String.format(locale, "Desvio padrão: %.2f m/s²", r.desvioPadraoAceleracoes ?: 0.0)
        }

        findViewById<TextView>(R.id.txt_res_estabilidade).apply {
            visibility = if (r.indiceEstabilidade != null) View.VISIBLE else View.GONE
            text = String.format(locale, "Índice de estabilidade: %.2f", r.indiceEstabilidade ?: 0.0)
        }

        findViewById<TextView>(R.id.txt_res_classificacao).apply {
            visibility = if (r.classificacao != null) View.VISIBLE else View.GONE
            text = "Classificação: ${r.classificacao}"
        }
    }

    private fun exibirDadosMarcha(r: RelatorioMarchaResponse) {
        layoutResultados.visibility = View.VISIBLE
        val locale = Locale("pt", "BR")

        findViewById<TextView>(R.id.txt_res_repeticoes_completas).text =
            "Número de picos: ${r.nPeaks ?: 0}"

        findViewById<TextView>(R.id.txt_res_percentual).text =
            "Coef. Var. Velocidade: ${String.format(locale, "%.2f", (r.cvVel ?: 0.0) * 100)} %"

        findViewById<TextView>(R.id.txt_res_altura_media).text =
            String.format(locale, "Velocidade Max: %.1f deg/s", r.velMaxDegS ?: 0.0)

        findViewById<TextView>(R.id.txt_res_cadencia).text =
            String.format(locale, "Cadência: %.2f ciclos/min", r.cadenceCyclesMin ?: 0.0)

        findViewById<TextView>(R.id.txt_res_amplitude).text =
            String.format(locale, "Velocidade Min: %.1f deg/s", r.velMinDegS ?: 0.0)

        findViewById<TextView>(R.id.txt_res_tempo).text =
            String.format(locale, "Tempo médio ciclo: %.2f s", r.timeMeanS ?: 0.0)

        findViewById<TextView>(R.id.txt_res_velocidade).text =
            String.format(locale, "Velocidade média: %.2f deg/s", r.velMeanDegS ?: 0.0)

        findViewById<TextView>(R.id.txt_res_desvio).apply {
            visibility = View.VISIBLE
            text = String.format(locale, "Desvio padrão vel: %.2f deg/s", r.velStdDegS ?: 0.0)
        }

        findViewById<TextView>(R.id.txt_res_estabilidade).apply {
            visibility = View.VISIBLE
            text = String.format(locale, "CV Tempo: %.4f", r.cvTime ?: 0.0)
        }

        findViewById<TextView>(R.id.txt_res_classificacao).apply {
            visibility = if (r.strategy != null) View.VISIBLE else View.GONE
            text = "Estratégia: ${r.strategy}"
        }
    }

    private fun formatarDataParaExibicao(dataBruta: String): String {
        val formatos = listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyyMMdd_HHmmss")
        for (formato in formatos) {
            try {
                val parser = SimpleDateFormat(formato, Locale.getDefault())
                val date = parser.parse(dataBruta)
                if (date != null) {
                    return SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(date)
                }
            } catch (_: Exception) { }
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
