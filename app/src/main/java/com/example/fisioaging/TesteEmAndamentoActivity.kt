package com.example.fisioaging

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

private enum class EstadoTeste { PRONTO, RODANDO, CONCLUIDO }

class TesteEmAndamentoActivity : AppCompatActivity(), SensorEventListener {

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
    private val tempoTotalEmMillis: Long = 30 * 1000 // Configurado para 30s (PoC)

    // Sensor e Dados
    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null

    // Lista para armazenamento temporário dos dados brutos
    private val dadosColetados = mutableListOf<String>()
    private var tempoInicioTeste: Long = 0

    // Variáveis do Algoritmo de Contagem
    private var contagemRepeticoes = 0
    private var ultimoPicoTempo: Long = 0

    // Configurações de sensibilidade (Ajustado para evitar rebote em marcha estacionária)
    private val COOLDOWN_PICO_MS = 900
    private var LIMITE_PICO = 5.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teste_em_andamento)

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
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

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
            salvarDadosCSV()
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
                textTimer.text = "0:30"
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

                // Armazena dados brutos formatados para padrão BR (vírgula)
                val linhaCsv = "$tempoRelativo;${x.toString().replace('.', ',')};${y.toString().replace('.', ',')};${z.toString().replace('.', ',')}"
                dadosColetados.add(linhaCsv)

                // Algoritmo de contagem em tempo real
                val magnitude = sqrt((x * x + y * y + z * z).toDouble())

                if (magnitude > LIMITE_PICO && (tempoAtual - ultimoPicoTempo > COOLDOWN_PICO_MS)) {
                    contagemRepeticoes++
                    ultimoPicoTempo = tempoAtual
                    Log.d("FISIO_AGING", "Pico detectado: $contagemRepeticoes")
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

    private fun salvarDadosCSV() {
        if (dadosColetados.isEmpty()) return

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // Prefixo MARCHA para identificar o tipo do teste na lista
        val nomeArquivo = "MARCHA_$timestamp.csv"

        try {
            val fileOutputStream: FileOutputStream = openFileOutput(nomeArquivo, Context.MODE_PRIVATE)
            fileOutputStream.write("time;x;y;z\n".toByteArray())
            dadosColetados.forEach { linha ->
                fileOutputStream.write("$linha\n".toByteArray())
            }
            fileOutputStream.close()
            Toast.makeText(this, "Teste salvo com sucesso", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar arquivo", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@TesteEmAndamentoActivity, "Teste Concluído!", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        pararColetaSensor()
    }
}