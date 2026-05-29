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
import com.example.fisioaging.model.RelatorioTesteResponse
import com.example.fisioaging.model.TesteResponse
import com.example.fisioaging.model.Usuario
import com.example.fisioaging.network.RetrofitClient
import com.example.fisioaging.util.SessionManager
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
                txtMsgProcessamento.text =
                    "Resultados ainda em processamento."
            }
        }
    }

    private fun iniciarViews() {
        txtTituloResultado =
            findViewById(R.id.txt_titulo_resultado)

        txtData =
            findViewById(R.id.txt_detalhe_data)

        txtStatus =
            findViewById(R.id.txt_detalhe_status)

        layoutResultados =
            findViewById(R.id.layout_resultados_analise)

        txtMsgProcessamento =
            findViewById(R.id.txt_msg_processamento)

        progressRelatorio =
            findViewById(R.id.progress_loading_relatorio)

        btnVoltar =
            findViewById(R.id.btn_voltar_historico)
    }

    private fun preencherCabecalho() {

        teste?.let {

            val nomeTeste =
                if (it.testType == "MARCHA")
                    "Marcha Estacionária"
                else
                    "Ponta dos Pés (UTT)"

            txtTituloResultado.text =
                "$nomeTeste • ${it.totalRepetitionsApp} repetições"

            txtData.text =
                formatarDataParaExibicao(it.testDateTime)

            txtStatus.text =
                traduzirStatus(it.status)

            val corStatus = when (it.status.uppercase()) {
                "PENDING" -> R.color.status_pending
                "UPLOADED" -> R.color.status_uploaded
                "PROCESSED" -> R.color.status_processed
                "COMPLETED" -> R.color.status_processed
                else -> R.color.black
            }

            txtStatus.setTextColor(
                ContextCompat.getColor(this, corStatus)
            )
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

                val service =
                    RetrofitClient.create(token)

                val relatorio =
                    service.getRelatorioTeste(
                        t.id,
                        p.email
                    )

                withContext(Dispatchers.Main) {

                    progressRelatorio.visibility = View.GONE

                    if (relatorio.status == "processing") {

                        txtMsgProcessamento.visibility = View.VISIBLE
                        txtMsgProcessamento.text =
                            "Resultados ainda estão sendo processados."

                    } else if (relatorio.totalRepeticoes != null) {

                        exibirDadosRelatorio(relatorio)

                    } else {

                        txtMsgProcessamento.visibility = View.VISIBLE
                        txtMsgProcessamento.text =
                            "Dados do relatório indisponíveis."
                    }
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    progressRelatorio.visibility = View.GONE
                    txtMsgProcessamento.visibility = View.VISIBLE
                    txtMsgProcessamento.text =
                        "Erro ao buscar resultados."
                }
            }
        }
    }

    private fun exibirDadosRelatorio(
        r: RelatorioTesteResponse
    ) {

        layoutResultados.visibility = View.VISIBLE

        val locale = Locale("pt", "BR")

        findViewById<TextView>(R.id.txt_res_repeticoes_completas).text =
            "Repetições completas: ${r.repeticoesCompletas} de ${r.totalRepeticoes}"

        findViewById<TextView>(R.id.txt_res_percentual).text =
            "Eficiência: ${r.percentualCompletas?.toInt()} %"

        findViewById<TextView>(R.id.txt_res_altura_media).text =
            String.format(
                locale,
                "Altura média: %.1f cm",
                r.alturaMedia ?: 0.0
            )

        findViewById<TextView>(R.id.txt_res_cadencia).text =
            String.format(
                locale,
                "Cadência: %.2f rep/min",
                r.cadencia ?: 0.0
            )

        findViewById<TextView>(R.id.txt_res_amplitude).text =
            String.format(
                locale,
                "Amplitude máxima: %.2f cm",
                r.amplitudeMaximaOscilacao ?: 0.0
            )

        findViewById<TextView>(R.id.txt_res_tempo).text =
            String.format(
                locale,
                "Tempo total: %.1f s",
                r.tempoTotalExecucao ?: 0.0
            )

        findViewById<TextView>(R.id.txt_res_velocidade).text =
            String.format(
                locale,
                "Velocidade média: %.2f cm/s",
                r.velocidadeMediaOscilacao ?: 0.0
            )

        r.desvioPadraoAceleracoes?.let {

            findViewById<TextView>(R.id.txt_res_desvio).apply {
                visibility = View.VISIBLE
                text = String.format(
                    locale,
                    "Desvio padrão: %.2f m/s²",
                    it
                )
            }
        }

        r.indiceEstabilidade?.let {

            findViewById<TextView>(R.id.txt_res_estabilidade).apply {
                visibility = View.VISIBLE
                text = String.format(
                    locale,
                    "Índice de estabilidade: %.2f",
                    it
                )
            }
        }

        r.classificacao?.let {

            findViewById<TextView>(R.id.txt_res_classificacao).apply {
                visibility = View.VISIBLE
                text = "Classificação: $it"
            }
        }
    }

    private fun formatarDataParaExibicao(
        dataBruta: String
    ): String {

        val formatos = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyyMMdd_HHmmss"
        )

        for (formato in formatos) {

            try {

                val parser =
                    SimpleDateFormat(
                        formato,
                        Locale.getDefault()
                    )

                val date = parser.parse(dataBruta)

                if (date != null) {

                    return SimpleDateFormat(
                        "dd/MM/yyyy 'às' HH:mm",
                        Locale("pt", "BR")
                    ).format(date)
                }

            } catch (_: Exception) {
            }
        }

        return dataBruta
    }

    private fun traduzirStatus(
        status: String
    ): String {

        return when (status.uppercase()) {
            "UPLOADED" -> "Sincronizado"
            "PENDING" -> "Pendente"
            "PROCESSED" -> "Processado"
            "COMPLETED" -> "Concluído"
            else -> status
        }
    }
}