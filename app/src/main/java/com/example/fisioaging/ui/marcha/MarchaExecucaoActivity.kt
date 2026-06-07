package com.example.fisioaging.ui.marcha

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
import java.util.*

private enum class EstadoTeste {
    PRONTO,
    PREPARANDO,
    RODANDO,
    CONCLUIDO
}

class MarchaExecucaoActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var textTimer: TextView
    private lateinit var lblStatus: TextView
    private lateinit var txtNomePaciente: TextView

    private lateinit var layoutBotaoPlay: LinearLayout
    private lateinit var layoutBotoesRodando: LinearLayout
    private lateinit var layoutBotoesConcluido: LinearLayout

    private lateinit var btnPlay: ImageButton
    private lateinit var btnRestartRodando: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var btnRestartConcluido: ImageButton
    private lateinit var btnDiscard: ImageButton
    private lateinit var btnSave: ImageButton

    private var timer: CountDownTimer? = null
    private val tempoTotalEmMillis: Long = TestConfig.DURACAO_MARCHA_PADRAO_MS

    private var timerPreparacao: CountDownTimer? = null

    // Sensores
    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null
    private var giroscopio: Sensor? = null

    private val dadosColetados = mutableListOf<JSONObject>()

    private var tempoInicioTeste = 0L
    private var paciente: Usuario? = null

    private lateinit var sessionManager: SessionManager

    private var contagemRepeticoes = 0
    private var ultimoTempo = 0L
    private var fase = 0

    private var toneGenerator: ToneGenerator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcha_execucao)

        paciente = intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario

        supportActionBar?.title = "Acompanhar Teste"

        sessionManager = SessionManager(this)

        inicializarUI()
        configurarSensores()
        configurarBotoes()

        txtNomePaciente.text = "Paciente: ${paciente?.name ?: "Paciente não identificado"}"

        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

        atualizarUI(EstadoTeste.PRONTO)
    }

    private fun inicializarUI() {
        txtNomePaciente = findViewById(R.id.text_nome_paciente)
        textTimer = findViewById(R.id.text_timer_contador)
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
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        giroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    private fun configurarBotoes() {
        btnPlay.setOnClickListener {
            atualizarUI(EstadoTeste.PREPARANDO)
            iniciarTimerPreparacao()
        }

        btnStop.setOnClickListener {
            pararColeta()
            atualizarUI(EstadoTeste.CONCLUIDO)
        }

        btnRestartRodando.setOnClickListener {
            pararColeta()
            atualizarUI(EstadoTeste.PRONTO)
        }

        btnRestartConcluido.setOnClickListener {
            atualizarUI(EstadoTeste.PRONTO)
        }

        btnDiscard.setOnClickListener {
            Toast.makeText(this, "Teste descartado.", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnSave.setOnClickListener {
            salvarJSON()
            finish()
        }
    }

    private fun iniciarTimerPreparacao() {
        timerPreparacao = object : CountDownTimer(TestConfig.TEMPO_PREPARACAO_MS, 1000) {
            override fun onTick(ms: Long) {
                val segundos = (ms / 1000) + 1
                textTimer.text = segundos.toString()
                lblStatus.text = "Preparar"
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            }

            override fun onFinish() {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 800)
                iniciarColeta()
                iniciarTimer()
                atualizarUI(EstadoTeste.RODANDO)
            }
        }.start()
    }

    private fun iniciarTimer() {
        timer = object : CountDownTimer(tempoTotalEmMillis, 1000) {
            override fun onTick(ms: Long) {
                val min = (ms / 1000) / 60
                val sec = (ms / 1000) % 60
                textTimer.text = String.format(Locale.getDefault(), "%d:%02d", min, sec)
                lblStatus.text = "Tempo Restante"
            }

            override fun onFinish() {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000)
                pararColeta()
                atualizarUI(EstadoTeste.CONCLUIDO)
            }
        }.start()
    }

    private fun iniciarColeta() {
        dadosColetados.clear()
        contagemRepeticoes = 0
        fase = 0
        tempoInicioTeste = System.currentTimeMillis()

        acelerometro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        giroscopio?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    private fun pararColeta() {
        timer?.cancel()
        timerPreparacao?.cancel()
        sensorManager.unregisterListener(this)
    }

    private fun atualizarUI(estado: EstadoTeste) {
        layoutBotaoPlay.visibility = View.GONE
        layoutBotoesRodando.visibility = View.GONE
        layoutBotoesConcluido.visibility = View.GONE

        when (estado) {
            EstadoTeste.PRONTO -> {
                val min = (tempoTotalEmMillis / 1000) / 60
                val sec = (tempoTotalEmMillis / 1000) % 60
                textTimer.text = String.format(Locale.getDefault(), "%d:%02d", min, sec)
                lblStatus.text = "Tempo Restante"
                layoutBotaoPlay.visibility = View.VISIBLE
            }
            EstadoTeste.PREPARANDO -> {
                layoutBotoesRodando.visibility = View.VISIBLE
            }
            EstadoTeste.RODANDO -> {
                layoutBotoesRodando.visibility = View.VISIBLE
            }
            EstadoTeste.CONCLUIDO -> {
                lblStatus.text = "Teste concluído"
                layoutBotoesConcluido.visibility = View.VISIBLE
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { e ->
            val tempoAtual = System.currentTimeMillis()
            val tempoRelativo = tempoAtual - tempoInicioTeste
            val registro = JSONObject()

            registro.put("time", tempoRelativo)
            registro.put("x", e.values[0])
            registro.put("y", e.values[1])
            registro.put("z", e.values[2])

            if (e.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                registro.put("sensor", "acelerometro")
                dadosColetados.add(registro)

                val y = e.values[1]
                when (fase) {
                    0 -> if (y > 1.5) fase = 1
                    1 -> if (y < 0) fase = 2
                    2 -> {
                        if (y < -1.5 && (tempoAtual - ultimoTempo > 400)) {
                            contagemRepeticoes++
                            ultimoTempo = tempoAtual
                            fase = 0
                        }
                    }
                }
            } else if (e.sensor.type == Sensor.TYPE_GYROSCOPE) {
                registro.put("sensor", "giroscopio")
                dadosColetados.add(registro)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun calcularIdade(dataNascString: String?): Int {
        if (dataNascString.isNullOrEmpty()) return 0
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dataNasc = sdf.parse(dataNascString) ?: return 0
            val calNasc = Calendar.getInstance()
            calNasc.time = dataNasc
            val calHoje = Calendar.getInstance()
            var idade = calHoje.get(Calendar.YEAR) - calNasc.get(Calendar.YEAR)
            if (calHoje.get(Calendar.DAY_OF_YEAR) < calNasc.get(Calendar.DAY_OF_YEAR)) {
                idade--
            }
            idade
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
        val nomeArquivo = "MARCHA_${dataStr}_${horaStr}_${idPac}_${nomePac}_${emailCodificado}.json"

        val json = JSONObject()
        json.put("tipo_teste", "MARCHA")
        json.put("data_hora", "${dataStr}_${horaStr}")
        json.put("sensor", "ANDROID")
        json.put("frequencia", 50)
        // Removida a contagem local do app pois não deve ser considerada
        json.put("total_repeticoes_app", 0)
        json.put("id_profissional", idProfissional)
        json.put("email_profissional", emailProfissional)
        json.put("sexo", generoPac)
        json.put("idade", idadePac)
        json.put("massa_kg", 70.0)
        json.put("registros", JSONArray(dadosColetados))
        json.put("unidadeSaudeCnpj", cnpjUnidade)

        try {
            val fos: FileOutputStream = openFileOutput(nomeArquivo, Context.MODE_PRIVATE)
            fos.write(json.toString(4).toByteArray())
            fos.close()
            Toast.makeText(this, "Teste de Marcha salvo com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar o teste de Marcha.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pararColeta()
        toneGenerator?.release()
    }
}
