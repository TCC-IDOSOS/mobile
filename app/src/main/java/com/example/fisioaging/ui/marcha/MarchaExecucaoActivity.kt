package com.example.fisioaging.ui.marcha

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
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

private enum class EstadoTeste { PRONTO, RODANDO, CONCLUIDO }

class MarchaExecucaoActivity : AppCompatActivity(), SensorEventListener {

    // Componentes da UI
    private lateinit var textTimer: TextView
    private lateinit var textResultado: TextView
    private lateinit var lblStatus: TextView

    private lateinit var layoutBotaoPlay: LinearLayout
    private lateinit var layoutBotoesRodando: LinearLayout
    private lateinit var layoutBotoesConcluido: LinearLayout

    private lateinit var btnPlay: ImageButton
    private lateinit var btnRestartRodando: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var btnRestartConcluido: ImageButton
    private lateinit var btnDiscard: ImageButton
    private lateinit var btnSave: ImageButton

    // Controle do Timer
    private var timer: CountDownTimer? = null
    private val tempoTotalEmMillis: Long = 2 * 60 * 1000 // 2 minutos

    // Sensor e Dados
    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null

    // Lista para armazenamento temporário dos dados brutos
    private val dadosColetados = mutableListOf<JSONObject>()
    private var tempoInicioTeste: Long = 0

    // Variáveis do Algoritmo de Contagem
    private var contagemRepeticoes = 0
    private var ultimoPicoTempo: Long = 0

    // Configurações de sensibilidade (Ajustado para evitar rebote em marcha estacionária)
    private val COOLDOWN_PICO_MS = 900
    private var LIMITE_PICO = 5.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcha_execucao)

        supportActionBar?.title = "2 Minutos Marcha estacionária"

        inicializarComponentesUI()
        configurarSensores()
        configurarListenersBotoes()

        atualizarVisibilidade(EstadoTeste.PRONTO)
    }

    private fun inicializarComponentesUI() {
        textTimer = findViewById(R.id.text_timer_contador)
        textResultado = findViewById(R.id.text_resultado_final)
        lblStatus = findViewById(R.id.lbl_status_teste)

        layoutBotaoPlay = findViewById(R.id.layout_botao_play)
        layoutBotoesRodando = findViewById(R.id.layout_botoes_rodando)
        layoutBotoesConcluido = findViewById(R.id.layout_botoes_concluido)

        btnPlay = findViewById(R.id.btn_play)
        btnRestartRodando = findViewById(R.id.btn_restart_rodando)
        btnStop = findViewById(R.id.btn_stop)
        btnRestartConcluido = findViewById(R.id.btn_restart_concluido)
        btnDiscard = findViewById(R.id.btn_discard)
        btnSave = findViewById(R.id.btn_save)
    }

    private fun configurarSensores() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Prioriza o Acelerômetro Linear para remover gravidade automaticamente
        val sensorLinear = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        if (sensorLinear != null) {
            acelerometro = sensorLinear
            LIMITE_PICO = 5.0
        } else {
            // Fallback para acelerômetro comum (inclui gravidade)
            acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            LIMITE_PICO = 14.0
        }
    }

    private fun configurarListenersBotoes() {
        btnPlay.setOnClickListener {
            iniciarColetaSensor()
            iniciarTimer(tempoTotalEmMillis)
            atualizarVisibilidade(EstadoTeste.RODANDO)
        }

        btnRestartRodando.setOnClickListener {
            timer?.cancel()
            pararColetaSensor()
            atualizarVisibilidade(EstadoTeste.PRONTO)
            Toast.makeText(this, "Timer Reiniciado", Toast.LENGTH_SHORT).show()
        }

        btnStop.setOnClickListener {
            timer?.cancel()
            pararColetaSensor()
            atualizarVisibilidade(EstadoTeste.CONCLUIDO)
            Toast.makeText(this, "Teste Parado", Toast.LENGTH_SHORT).show()
        }

        btnRestartConcluido.setOnClickListener {
            atualizarVisibilidade(EstadoTeste.PRONTO)
        }

        btnDiscard.setOnClickListener {
            Toast.makeText(this, "Teste descartado", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnSave.setOnClickListener {
            salvarDadosJSON()
            finish()
        }
    }

    private fun atualizarVisibilidade(novoEstado: EstadoTeste) {
        layoutBotaoPlay.visibility = View.GONE
        layoutBotoesRodando.visibility = View.GONE
        layoutBotoesConcluido.visibility = View.GONE

        when (novoEstado) {
            EstadoTeste.PRONTO -> {
                textTimer.visibility = View.VISIBLE
                textResultado.visibility = View.GONE
                textTimer.text = "2:00"
                lblStatus.text = "Tempo Restante"
                dadosColetados.clear()
                layoutBotaoPlay.visibility = View.VISIBLE
            }
            EstadoTeste.RODANDO -> {
                textTimer.visibility = View.VISIBLE
                textResultado.visibility = View.GONE
                lblStatus.text = "Tempo Restante"
                layoutBotoesRodando.visibility = View.VISIBLE
            }
            EstadoTeste.CONCLUIDO -> {
                textTimer.visibility = View.GONE
                textResultado.visibility = View.VISIBLE
                textResultado.text = "$contagemRepeticoes Repetições"
                lblStatus.text = "Resultado Preliminar"
                layoutBotoesConcluido.visibility = View.VISIBLE
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { eventoNaoNulo ->
            if (eventoNaoNulo.sensor.type == acelerometro?.type) {
                val tempoAtual = System.currentTimeMillis()
                val tempoRelativo = tempoAtual - tempoInicioTeste

                val x = eventoNaoNulo.values[0]
                val y = eventoNaoNulo.values[1]
                val z = eventoNaoNulo.values[2]

                //  Cria objeto JSON para o registro atual
                val registro = JSONObject()
                registro.put("time", tempoRelativo)
                registro.put("x", x)
                registro.put("y", y)
                registro.put("z", z)

                dadosColetados.add(registro)

                // Algoritmo de contagem
                val magnitude = sqrt((x * x + y * y + z * z).toDouble())
                if (magnitude > LIMITE_PICO && (tempoAtual - ultimoPicoTempo > COOLDOWN_PICO_MS)) {
                    contagemRepeticoes++
                    ultimoPicoTempo = tempoAtual
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    private fun iniciarColetaSensor() {
        dadosColetados.clear()
        contagemRepeticoes = 0
        tempoInicioTeste = System.currentTimeMillis()
        acelerometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun pararColetaSensor() {
        sensorManager.unregisterListener(this)
    }

    private fun salvarDadosJSON() {
        if (dadosColetados.isEmpty()) return

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val nomeArquivo = "MARCHA_$timestamp.json"

        try {
            val root = org.json.JSONObject()

            root.put("tipo_teste", "MARCHA") // Identificador para o servidor
            root.put("data_hora", timestamp)
            root.put("total_repeticoes_app", contagemRepeticoes)

            // Transforma a lista de registros em um JSONArray
            val jsonArrayRegistros = org.json.JSONArray(dadosColetados)
            root.put("registros", jsonArrayRegistros)

            // Escrita do arquivo na memória interna
            val fileOutputStream: java.io.FileOutputStream = openFileOutput(nomeArquivo, Context.MODE_PRIVATE)
            fileOutputStream.write(root.toString(4).toByteArray()) // O '4' organiza o texto (identação)
            fileOutputStream.close()

            Toast.makeText(this, "Teste de Marcha salvo!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar JSON", Toast.LENGTH_SHORT).show()
        }
    }

    private fun iniciarTimer(duracao: Long) {
        timer = object : CountDownTimer(duracao, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutos = (millisUntilFinished / 1000) / 60
                val segundos = (millisUntilFinished / 1000) % 60
                textTimer.text = String.format("%d:%02d", minutos, segundos)
            }

            override fun onFinish() {
                pararColetaSensor()
                atualizarVisibilidade(EstadoTeste.CONCLUIDO)
                Toast.makeText(this@MarchaExecucaoActivity, "Teste Concluído!", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        pararColetaSensor()
    }
}