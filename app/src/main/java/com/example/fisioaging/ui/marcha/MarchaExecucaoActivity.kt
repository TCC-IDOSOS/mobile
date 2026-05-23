package com.example.fisioaging.ui.marcha

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fisioaging.R
import com.example.fisioaging.model.Usuario
import com.example.fisioaging.util.SessionManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*

private enum class EstadoTeste { PRONTO, RODANDO, CONCLUIDO }

class MarchaExecucaoActivity : AppCompatActivity(), SensorEventListener {

    // UI
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

    // Timer
    private var timer: CountDownTimer? = null
    private var tempoTotalEmMillis: Long = 2 * 60 * 1000

    // Sensores
    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null
    private var giroscopio: Sensor? = null

    // Dados
    private val dadosColetados = mutableListOf<JSONObject>()
    private var tempoInicioTeste: Long = 0
    private var paciente: Usuario? = null
    private lateinit var sessionManager: SessionManager

    // Lógica de Marcha (Contagem)
    private var contagemRepeticoes = 0
    private var ultimoTempo = 0L
    private var fase = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marcha_execucao)

        paciente = intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario
        supportActionBar?.title = "Marcha: ${paciente?.name ?: "Desconhecido"}"

        sessionManager = SessionManager(this)

        inicializarUI()
        configurarSensores()
        configurarBotoes()

        atualizarUI(EstadoTeste.PRONTO)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_marcha_execucao, menu)

        val itemModo = menu?.findItem(R.id.action_modo_apresentacao)
        if (tempoTotalEmMillis == 30 * 1000L) {
            itemModo?.title = "Modo Padrão (2 min)"
        } else {
            itemModo?.title = "Modo Apresentação (30s)"
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_modo_apresentacao -> {
                if (tempoTotalEmMillis == 2 * 60 * 1000L) {
                    tempoTotalEmMillis = 30 * 1000L
                    item.title = "Modo Padrão (2 min)"
                    textTimer.text = "0:30"
                } else {
                    tempoTotalEmMillis = 2 * 60 * 1000L
                    item.title = "Modo Apresentação (30s)"
                    textTimer.text = "2:00"
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun inicializarUI() {
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

        // Configura Acelerômetro
        val sensorLinear = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        acelerometro = sensorLinear ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Configura Giroscópio
        giroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    private fun configurarBotoes() {
        btnPlay.setOnClickListener {
            iniciarColeta()
            iniciarTimer(tempoTotalEmMillis)
            atualizarUI(EstadoTeste.RODANDO)
        }

        btnRestartRodando.setOnClickListener {
            pararColeta()
            atualizarUI(EstadoTeste.PRONTO)
        }

        btnStop.setOnClickListener {
            pararColeta()
            atualizarUI(EstadoTeste.CONCLUIDO)
        }

        btnRestartConcluido.setOnClickListener {
            atualizarUI(EstadoTeste.PRONTO)
        }

        btnDiscard.setOnClickListener {
            finish()
        }

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

        // Registra o Acelerômetro
        acelerometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        // Registra o Giroscópio
        giroscopio?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun pararColeta() {
        timer?.cancel()
        sensorManager.unregisterListener(this)
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

            if (e.sensor.type == acelerometro?.type) {
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
            } else if (e.sensor.type == giroscopio?.type) {
                // Salva dados do giroscópio em rad/s
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
            e.printStackTrace()
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
        json.put("total_repeticoes_app", contagemRepeticoes)
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

    private fun iniciarTimer(duracao: Long) {
        timer = object : CountDownTimer(duracao, 1000) {
            override fun onTick(ms: Long) {
                val min = (ms / 1000) / 60
                val sec = (ms / 1000) % 60
                textTimer.text = String.format("%d:%02d", min, sec)
            }

            override fun onFinish() {
                pararColeta()
                atualizarUI(EstadoTeste.CONCLUIDO)
            }
        }.start()
    }

    private fun atualizarUI(estado: EstadoTeste) {
        layoutBotaoPlay.visibility = View.GONE
        layoutBotoesRodando.visibility = View.GONE
        layoutBotoesConcluido.visibility = View.GONE

        when (estado) {
            EstadoTeste.PRONTO -> {
                val min = (tempoTotalEmMillis / 1000) / 60
                val sec = (tempoTotalEmMillis / 1000) % 60
                textTimer.text = String.format("%d:%02d", min, sec)

                layoutBotaoPlay.visibility = View.VISIBLE
            }
            EstadoTeste.RODANDO -> {
                layoutBotoesRodando.visibility = View.VISIBLE
            }
            EstadoTeste.CONCLUIDO -> {
                textResultado.text = "$contagemRepeticoes Repetições"
                layoutBotoesConcluido.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pararColeta()
    }
}