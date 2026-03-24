package com.example.fisioaging.ui.utt

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
import java.util.*
import kotlin.math.abs

private enum class EstadoUTT { PRONTO, RODANDO, CONCLUIDO }

class UttExecucaoActivity : AppCompatActivity(), SensorEventListener {

    // Componentes da UI
    private lateinit var textTimer: TextView
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

    // Controle do Timer
    private var timer: CountDownTimer? = null
    private val tempoTotalEmMillis: Long = 30 * 1000 // 30 segundos exatos para UTT

    // Sensor e Coleta
    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null
    private val dadosColetados = mutableListOf<JSONObject>()
    private var tempoInicioTeste: Long = 0

    // Algoritmo Simples de Feedback (Ponta dos pés - Foco no Eixo X)
    private var contagemRepeticoes = 0
    private var ultimoPicoTempo: Long = 0
    private val COOLDOWN_PICO_MS = 600 // Tempo mínimo entre repetições
    private val LIMITE_PICO_X = 3.5 // Sensibilidade para o eixo X

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utt_execucao) // Reutiliza seu XML padrão

        supportActionBar?.title = "Teste UTT - Na Ponta dos Pés"

        inicializarComponentesUI()
        configurarSensores()
        configurarListenersBotoes()

        atualizarUI(EstadoUTT.PRONTO)
    }

    private fun inicializarComponentesUI() {
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

    private fun configurarSensores() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        // O UTT utiliza o acelerômetro (preferencialmente linear se disponível, ou comum)
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun configurarListenersBotoes() {
        // Iniciar Teste
        btnPlay.setOnClickListener {
            iniciarColeta()
            iniciarTimer(tempoTotalEmMillis)
            atualizarUI(EstadoUTT.RODANDO)
        }

        // Parar durante execução
        btnStop.setOnClickListener {
            pararTeste()
        }

        // Reiniciar enquanto roda
        btnRestartRodando.setOnClickListener {
            pararTeste()
            atualizarUI(EstadoUTT.PRONTO)
            Toast.makeText(this, "Teste resetado", Toast.LENGTH_SHORT).show()
        }

        // Reiniciar após concluir
        btnRestartConcluido.setOnClickListener {
            atualizarUI(EstadoUTT.PRONTO)
        }

        // Descartar teste
        btnDiscard.setOnClickListener {
            Toast.makeText(this, "Teste descartado", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Salvar JSON e Sair
        btnSave.setOnClickListener {
            salvarDadosJSON()
            finish()
        }
    }

    private fun iniciarColeta() {
        dadosColetados.clear()
        contagemRepeticoes = 0
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
            if (e.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val tempoAtual = System.currentTimeMillis()
                val tempoRelativo = tempoAtual - tempoInicioTeste

                val x = e.values[0]
                val y = e.values[1]
                val z = e.values[2]


                // Criar registro JSON conforme script UTT_01
                val registro = JSONObject()
                registro.put("time", tempoRelativo)
                registro.put("acc_x", x) // O script UTT foca no X
                registro.put("acc_y", y)
                registro.put("acc_z", z)
                dadosColetados.add(registro)

                // Algoritmo de contagem preliminar para feedback no app
                // Baseado na variação brusca do Eixo X (movimento de subida/descida lateral)
                if (abs(x) > LIMITE_PICO_X && (tempoAtual - ultimoPicoTempo > COOLDOWN_PICO_MS)) {
                    contagemRepeticoes++
                    ultimoPicoTempo = tempoAtual
                }
            }
        }
    }

    private fun salvarDadosJSON() {
        if (dadosColetados.isEmpty()) return

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val nomeArquivo = "UTT_$timestamp.json"

        try {
            val jsonFinal = JSONObject()
            jsonFinal.put("tipo_teste", "UTT")
            jsonFinal.put("data_hora", timestamp)
            jsonFinal.put("total_repeticoes_app", contagemRepeticoes)
            jsonFinal.put("registros", JSONArray(dadosColetados))

            val fos: FileOutputStream = openFileOutput(nomeArquivo, MODE_PRIVATE)
            fos.write(jsonFinal.toString(4).toByteArray())
            fos.close()

            Toast.makeText(this, "UTT salvo com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar JSON", Toast.LENGTH_SHORT).show()
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

    private fun atualizarUI(novoEstado: EstadoUTT) {
        layoutBotaoPlay.visibility = View.GONE
        layoutBotoesRodando.visibility = View.GONE
        layoutBotoesConcluido.visibility = View.GONE

        when (novoEstado) {
            EstadoUTT.PRONTO -> {
                textTimer.visibility = View.VISIBLE
                textResultado.visibility = View.GONE
                textTimer.text = "0:30"
                lblStatus.text = "Tempo Restante"
                layoutBotaoPlay.visibility = View.VISIBLE
            }
            EstadoUTT.RODANDO -> {
                textTimer.visibility = View.VISIBLE
                textResultado.visibility = View.GONE
                layoutBotoesRodando.visibility = View.VISIBLE
            }
            EstadoUTT.CONCLUIDO -> {
                textTimer.visibility = View.GONE
                textResultado.visibility = View.VISIBLE
                textResultado.text = "$contagemRepeticoes Repetições"
                lblStatus.text = "Resultado UTT Concluído"
                layoutBotoesConcluido.visibility = View.VISIBLE
            }
        }
    }

    override fun onAccuracyChanged(s: Sensor?, a: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        sensorManager.unregisterListener(this)
    }
}