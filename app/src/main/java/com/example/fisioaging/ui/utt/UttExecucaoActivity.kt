package com.example.fisioaging.ui.utt

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.model.Usuario
import com.example.fisioaging.util.SessionManager
import com.example.fisioaging.util.TestConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*

private enum class EstadoUTT {
    PRONTO,
    PREPARANDO,
    RODANDO,
    CONCLUIDO
}

class UttExecucaoActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var textTimer: TextView
    private lateinit var lblStatus: TextView
    private lateinit var txtNomePaciente: TextView

    private lateinit var layoutBotaoPlay: LinearLayout
    private lateinit var layoutBotoesRodando: LinearLayout
    private lateinit var layoutBotoesConcluido: LinearLayout

    private lateinit var btnPlay: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var btnRestartRodando: ImageButton
    private lateinit var btnRestartConcluido: ImageButton
    private lateinit var btnDiscard: ImageButton
    private lateinit var btnSave: ImageButton

    private var timer: CountDownTimer? = null
    private val tempoTotalEmMillis: Long = TestConfig.DURACAO_UTT_MS
    private var timerPreparacao: CountDownTimer? = null

    // Sensores
    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null

    private val dadosColetados = mutableListOf<JSONObject>()

    private var tempoInicioTeste: Long = 0
    private var pesoPaciente: Double = 0.0
    private var paciente: Usuario? = null

    private lateinit var sessionManager: SessionManager

    private var contagemRepeticoes = 0
    private var ultimoTempo = 0L
    private var fase = 0

    private var toneGenerator: ToneGenerator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utt_execucao)

        paciente =
            intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario

        pesoPaciente =
            intent.getDoubleExtra("PESO_PACIENTE", 0.0)

        supportActionBar?.title = "Acompanhar Teste"
        supportActionBar?.subtitle = null

        sessionManager = SessionManager(this)

        inicializarUI()
        configurarSensor()
        configurarBotoes()

        txtNomePaciente.text =
            "Paciente: ${paciente?.name ?: "Paciente não identificado"}"

        toneGenerator =
            ToneGenerator(AudioManager.STREAM_MUSIC, 100)

        atualizarUI(EstadoUTT.PRONTO)
    }

    private fun inicializarUI() {
        txtNomePaciente =
            findViewById(R.id.text_nome_paciente)

        textTimer =
            findViewById(R.id.text_timer_contador)

        lblStatus =
            findViewById(R.id.lbl_status_teste)

        layoutBotaoPlay =
            findViewById(R.id.layout_botao_play)

        layoutBotoesRodando =
            findViewById(R.id.layout_botoes_rodando)

        layoutBotoesConcluido =
            findViewById(R.id.layout_botoes_concluido)

        btnPlay =
            findViewById(R.id.btn_play)

        btnStop =
            findViewById(R.id.btn_stop)

        btnRestartRodando =
            findViewById(R.id.btn_restart_rodando)

        btnRestartConcluido =
            findViewById(R.id.btn_restart_concluido)

        btnDiscard =
            findViewById(R.id.btn_discard)

        btnSave =
            findViewById(R.id.btn_save)
    }

    private fun configurarSensor() {
        sensorManager =
            getSystemService(SENSOR_SERVICE) as SensorManager

        acelerometro =
            sensorManager.getDefaultSensor(
                Sensor.TYPE_LINEAR_ACCELERATION
            )
    }

    private fun configurarBotoes() {

        btnPlay.setOnClickListener {
            atualizarUI(EstadoUTT.PREPARANDO)
            iniciarTimerPreparacao()
        }

        btnStop.setOnClickListener {
            pararTeste()
        }

        btnRestartRodando.setOnClickListener {
            pararTeste()
            atualizarUI(EstadoUTT.PRONTO)
        }

        btnRestartConcluido.setOnClickListener {
            atualizarUI(EstadoUTT.PRONTO)
        }

        btnDiscard.setOnClickListener {
            Toast.makeText(
                this,
                "Teste descartado.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

        btnSave.setOnClickListener {

            salvarJSON()

            Toast.makeText(
                this,
                "Teste salvo com sucesso.",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    private fun iniciarColeta() {
        dadosColetados.clear()
        contagemRepeticoes = 0
        fase = 0

        tempoInicioTeste =
            System.currentTimeMillis()

        acelerometro?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    private fun pararTeste() {
        timer?.cancel()
        sensorManager.unregisterListener(this)
        atualizarUI(EstadoUTT.CONCLUIDO)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { e ->

            if (e.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {

                val tempoAtual =
                    System.currentTimeMillis()

                val tempoRelativo =
                    tempoAtual - tempoInicioTeste

                val registro = JSONObject()

                registro.put("time", tempoRelativo)
                registro.put("x", e.values[0])
                registro.put("y", e.values[1])
                registro.put("z", e.values[2])

                dadosColetados.add(registro)

                val y = e.values[1]

                when (fase) {
                    0 -> if (y > 1.2) fase = 1
                    1 -> if (y < 0) fase = 2
                    2 -> {
                        if (y < -1.2 &&
                            tempoAtual - ultimoTempo > 600
                        ) {
                            contagemRepeticoes++
                            ultimoTempo = tempoAtual
                            fase = 0
                        }
                    }
                }
            }
        }
    }

    private fun iniciarTimerPreparacao() {

        timerPreparacao =
            object : CountDownTimer(3000, 1000) {

                override fun onTick(ms: Long) {
                    val segundos =
                        (ms / 1000) + 1

                    textTimer.text =
                        segundos.toString()

                    lblStatus.text =
                        "Preparar"

                    toneGenerator?.startTone(
                        ToneGenerator.TONE_PROP_BEEP,
                        150
                    )
                }

                override fun onFinish() {

                    toneGenerator?.startTone(
                        ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
                        800
                    )

                    iniciarColeta()
                    iniciarTimer(tempoTotalEmMillis)

                    atualizarUI(EstadoUTT.RODANDO)
                }
            }

        timerPreparacao?.start()
    }

    private fun iniciarTimer(duracao: Long) {

        timer =
            object : CountDownTimer(duracao, 1000) {

                override fun onTick(ms: Long) {

                    textTimer.text =
                        String.format(
                            Locale.getDefault(),
                            "0:%02d",
                            ms / 1000
                        )

                    lblStatus.text =
                        "Tempo Restante"
                }

                override fun onFinish() {

                    toneGenerator?.startTone(
                        ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,
                        1000
                    )

                    pararTeste()
                }
            }

        timer?.start()
    }

    private fun atualizarUI(estado: EstadoUTT) {

        layoutBotaoPlay.visibility = View.GONE
        layoutBotoesRodando.visibility = View.GONE
        layoutBotoesConcluido.visibility = View.GONE

        when (estado) {

            EstadoUTT.PRONTO -> {
                textTimer.text = "0:30"
                lblStatus.text = "Tempo Restante"
                layoutBotaoPlay.visibility = View.VISIBLE
            }

            EstadoUTT.PREPARANDO -> {
                layoutBotoesRodando.visibility = View.VISIBLE
            }

            EstadoUTT.RODANDO -> {
                layoutBotoesRodando.visibility = View.VISIBLE
            }

            EstadoUTT.CONCLUIDO -> {
                lblStatus.text = "Teste interrompido"
                layoutBotoesConcluido.visibility = View.VISIBLE
            }
        }
    }

    private fun calcularIdade(dataNascString: String?): Int {

        if (dataNascString.isNullOrEmpty())
            return 0

        return try {

            val formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd")

            val dataNascimento =
                LocalDate.parse(
                    dataNascString,
                    formatter
                )

            val hoje = LocalDate.now()

            Period.between(
                dataNascimento,
                hoje
            ).years

        } catch (e: Exception) {
            0
        }
    }

    private fun salvarJSON() {

        if (dadosColetados.isEmpty()) return

        val idPac = paciente?.id ?: 0
        val nomePac = paciente?.name?.replace(" ", "") ?: "Desconhecido"
        val emailPac = paciente?.email?.replace(" ", "") ?: "Desconhecido"
        val emailCodificado = URLEncoder.encode(emailPac, "UTF-8")
        val generoPac = paciente?.genre ?: "Não informado"
        val idadePac = calcularIdade(paciente?.birthDate)

        val idProfissional = sessionManager.fetchUserId()
        val emailProfissional = sessionManager.fetchProfessionalEmail()
        val cnpjUnidade = sessionManager.fetchHealthUnitCnpj()

        val dataStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val horaStr = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
        val nomeArquivo = "UTT_${dataStr}_${horaStr}_${idPac}_${nomePac}_${emailCodificado}.json"

        // Monta o payload final conforme contrato do Swagger
        val json = JSONObject()

        json.put("tipo_teste", "UTT")
        json.put("data_hora", "${dataStr}_${horaStr}")
        json.put("sensor", "ANDROID")
        json.put("frequencia", 50)
        json.put("total_repeticoes_app", contagemRepeticoes)
        json.put("id_profissional", idProfissional)
        json.put("email_profissional", emailProfissional)
        json.put("sexo", generoPac)
        json.put("idade", idadePac)
        json.put("massa_kg", pesoPaciente) // Informado manualmente na tela anterior
        json.put("registros", JSONArray(dadosColetados))
        json.put("unidadeSaudeCnpj", cnpjUnidade)

        try {
            val fos: FileOutputStream = openFileOutput(nomeArquivo, Context.MODE_PRIVATE)
            fos.write(json.toString(4).toByteArray())
            fos.close()

            Toast.makeText(
                this,
                "Teste UTT salvo com sucesso!",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            e.printStackTrace()

            Toast.makeText(
                this,
                "Erro ao salvar teste.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
    }

    override fun onDestroy() {
        super.onDestroy()

        timer?.cancel()
        timerPreparacao?.cancel()

        sensorManager.unregisterListener(this)

        toneGenerator?.release()
    }
}