package com.example.fisioaging.ui.utt

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
private enum class EstadoUTT { PRONTO, RODANDO, CONCLUIDO }

class UttExecucaoActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var textTimer: TextView

    private var pesoPaciente: Double = 0.0
    private lateinit var textResultado: TextView
    private lateinit var lblStatus: TextView

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
    private val tempoTotalEmMillis: Long = 30 * 1000

    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null

    private val dadosColetados = mutableListOf<JSONObject>()
    private var tempoInicioTeste: Long = 0

    private var paciente: Usuario? = null

    private var contagemRepeticoes = 0
    private var ultimoTempo = 0L
    private var fase = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utt_execucao)

        paciente = intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario
        supportActionBar?.title = "UTT: ${paciente?.name ?: "Ponta dos Pés"}"

        if (paciente == null) {
            Toast.makeText(this, "ERRO: Paciente não recebeu dados!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Paciente: ${paciente?.name}", Toast.LENGTH_SHORT).show()
        }
        inicializarUI()
        configurarSensor()
        configurarBotoes()

        atualizarUI(EstadoUTT.PRONTO)

        paciente = intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario
        pesoPaciente = intent.getDoubleExtra("PESO_PACIENTE", 0.0)
    }

    private fun inicializarUI() {
        textTimer = findViewById(R.id.text_timer_contador)
        textResultado = findViewById(R.id.text_resultado_final)
        lblStatus = findViewById(R.id.lbl_status_teste)

        layoutBotaoPlay = findViewById(R.id.layout_botao_play)
        layoutBotoesRodando = findViewById(R.id.layout_botoes_rodando)
        layoutBotoesConcluido = findViewById(R.id.layout_botoes_concluido)

        btnPlay = findViewById(R.id.btn_play)
        btnStop = findViewById(R.id.btn_stop)
        btnRestartRodando = findViewById(R.id.btn_restart_rodando)
        btnRestartConcluido = findViewById(R.id.btn_restart_concluido)
        btnDiscard = findViewById(R.id.btn_discard)
        btnSave = findViewById(R.id.btn_save)
    }

    private fun configurarSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    }

    private fun configurarBotoes() {
        btnPlay.setOnClickListener {
            iniciarColeta()
            iniciarTimer(tempoTotalEmMillis)
            atualizarUI(EstadoUTT.RODANDO)
        }

        btnStop.setOnClickListener { pararTeste() }

        btnRestartRodando.setOnClickListener {
            pararTeste()
            atualizarUI(EstadoUTT.PRONTO)
        }

        btnRestartConcluido.setOnClickListener {
            atualizarUI(EstadoUTT.PRONTO)
        }

        btnDiscard.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            salvarJSON()
            finish()
        }
    }

    private fun iniciarColeta() {
        dadosColetados.clear()
        contagemRepeticoes = 0
        fase = 0
        tempoInicioTeste = System.currentTimeMillis()

        acelerometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
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
                val tempoAtual = System.currentTimeMillis()
                val tempoRelativo = tempoAtual - tempoInicioTeste

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
                        if (y < -1.2 && (tempoAtual - ultimoTempo > 600)) {
                            contagemRepeticoes++
                            ultimoTempo = tempoAtual
                            fase = 0
                        }
                    }
                }
            }
        }
    }



    private fun calcularIdade(dataNascString: String?): Int {
        if (dataNascString.isNullOrEmpty()) return 0
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val dataNascimento = LocalDate.parse(dataNascString, formatter)
            val hoje = LocalDate.now()
            Period.between(dataNascimento, hoje).years
        } catch (e: Exception) {
            0
        }
    }

    private fun salvarJSON() {
        if (dadosColetados.isEmpty()) return

        val idPac = paciente?.id ?: 0
        val nomePac = paciente?.name?.replace(" ", "") ?: "Desconhecido"
        val emailPac = paciente?.email?.replace(" ", "") ?: "Desconhecido"
        val emailSeguro = emailPac
            .replace("@", "-at-")
            .replace(".", "-")
        val generoPac = paciente?.genre ?: "N/A"
        val idadePac = calcularIdade(paciente?.birthDate)

        val dataStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val horaStr = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
        val nomeArquivo = "MARCHA_${dataStr}_${horaStr}_${idPac}_${nomePac}_${emailPac}.json"

        val json = JSONObject()
        json.put("userId", idPac)
        json.put("sexo", generoPac)
        json.put("idade", idadePac)

        // NOVO CAMPO SOLICITADO
        json.put("peso", pesoPaciente)

        json.put("tipo_teste", "UTT")
        json.put("data_hora", "${dataStr}_${horaStr}")
        json.put("total_repeticoes_app", contagemRepeticoes)
        json.put("registros", JSONArray(dadosColetados))

        try {
            val fos: FileOutputStream = openFileOutput(nomeArquivo, Context.MODE_PRIVATE)
            fos.write(json.toString(4).toByteArray())
            fos.close()
            Toast.makeText(this, "Teste salvo com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun iniciarTimer(duracao: Long) {
        timer = object : CountDownTimer(duracao, 1000) {
            override fun onTick(ms: Long) {
                textTimer.text = String.format("0:%02d", ms / 1000)
            }
            override fun onFinish() {
                pararTeste()
            }
        }.start()
    }

    private fun atualizarUI(estado: EstadoUTT) {
        layoutBotaoPlay.visibility = View.GONE
        layoutBotoesRodando.visibility = View.GONE
        layoutBotoesConcluido.visibility = View.GONE

        when (estado) {
            EstadoUTT.PRONTO -> {
                textTimer.text = "0:30"
                layoutBotaoPlay.visibility = View.VISIBLE
            }
            EstadoUTT.RODANDO -> layoutBotoesRodando.visibility = View.VISIBLE
            EstadoUTT.CONCLUIDO -> {
                textResultado.text = "$contagemRepeticoes Repetições"
                layoutBotoesConcluido.visibility = View.VISIBLE
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}