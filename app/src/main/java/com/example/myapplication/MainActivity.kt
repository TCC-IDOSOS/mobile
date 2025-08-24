package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private lateinit var tvAccelerometer: TextView
    private lateinit var tvGyroscope: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnStartRecording: Button
    private lateinit var btnStopRecording: Button
    private lateinit var btnViewData: Button

    private var isRecording = false
    private var csvFile: File? = null
    private var csvWriter: FileWriter? = null
    private var recordingStartTime: Long = 0
    private var dataPointCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar views
        tvAccelerometer = findViewById(R.id.tvAccelerometer)
        tvGyroscope = findViewById(R.id.tvGyroscope)
        tvStatus = findViewById(R.id.tvStatus)
        btnStartRecording = findViewById(R.id.btnStartRecording)
        btnStopRecording = findViewById(R.id.btnStopRecording)
        btnViewData = findViewById(R.id.btnViewData)

        // Configurar botões
        btnStartRecording.setOnClickListener { startRecording() }
        btnStopRecording.setOnClickListener { stopRecording() }
        btnViewData.setOnClickListener { viewStoredData() }

        // Inicializar sensores
        setupSensors()
        updateButtonStates()
    }

    private fun setupSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        var status = "Sensores disponíveis: "

        if (accelerometer != null) {
            status += "Acelerômetro ✓ "
        } else {
            status += "Acelerômetro ✗ "
        }

        if (gyroscope != null) {
            status += "Giroscópio ✓"
        } else {
            status += "Giroscópio ✗"
        }

        tvStatus.text = status
    }

    private fun startRecording() {
        try {
            // Criar arquivo CSV com timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "sensor_data_$timestamp.csv"

            csvFile = File(getExternalFilesDir(null), fileName)
            csvWriter = FileWriter(csvFile!!)

            // Escrever cabeçalho do CSV
            csvWriter!!.write("timestamp,elapsed_ms,acc_x,acc_y,acc_z,acc_magnitude,gyro_x,gyro_y,gyro_z,gyro_magnitude\n")

            isRecording = true
            recordingStartTime = System.currentTimeMillis()
            dataPointCount = 0

            updateButtonStates()
            Toast.makeText(this, "Gravação iniciada: $fileName", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao iniciar gravação: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRecording() {
        try {
            isRecording = false
            csvWriter?.close()

            val duration = (System.currentTimeMillis() - recordingStartTime) / 1000.0
            updateButtonStates()

            Toast.makeText(this,
                "Gravação finalizada!\n$dataPointCount pontos em ${duration}s\nArquivo: ${csvFile?.name}",
                Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao parar gravação: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun viewStoredData() {
        val dataDir = getExternalFilesDir(null)
        val csvFiles = dataDir?.listFiles { file -> file.name.endsWith(".csv") }

        if (csvFiles == null || csvFiles.isEmpty()) {
            Toast.makeText(this, "Nenhum arquivo encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar lista dos arquivos (implementação simples)
        val fileNames = csvFiles.map { "${it.name} (${it.length() / 1024}KB)" }.joinToString("\n")

        val message = "Arquivos salvos em:\n${dataDir?.absolutePath}\n\nArquivos:\n$fileNames"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Dados Armazenados")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Abrir Pasta") { _, _ ->
                // Opcional: abrir pasta (requer intent)
                Toast.makeText(this, "Copie os arquivos via USB ou compartilhe", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun updateButtonStates() {
        btnStartRecording.isEnabled = !isRecording
        btnStopRecording.isEnabled = isRecording
        btnViewData.isEnabled = !isRecording

        if (isRecording) {
            tvStatus.text = "🔴 GRAVANDO - $dataPointCount pontos"
        }
    }

    override fun onResume() {
        super.onResume()

        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)

        // Para gravação se estiver ativa
        if (isRecording) {
            stopRecording()
        }
    }

    // Variáveis para armazenar últimos valores
    private var lastAccData = FloatArray(3)
    private var lastGyroData = FloatArray(3)
    private var hasAccData = false
    private var hasGyroData = false

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    val magnitude = sqrt(x*x + y*y + z*z)

                    // Atualizar display
                    tvAccelerometer.text = String.format(
                        "Acelerômetro:\nX: %.2f\nY: %.2f\nZ: %.2f\nMagnitude: %.2f m/s²",
                        x, y, z, magnitude
                    )

                    // Armazenar para CSV
                    lastAccData[0] = x
                    lastAccData[1] = y
                    lastAccData[2] = z
                    hasAccData = true
                }

                Sensor.TYPE_GYROSCOPE -> {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    val magnitude = sqrt(x*x + y*y + z*z)

                    // Atualizar display
                    tvGyroscope.text = String.format(
                        "Giroscópio:\nX: %.2f\nY: %.2f\nZ: %.2f\nMagnitude: %.2f rad/s",
                        x, y, z, magnitude
                    )

                    // Armazenar para CSV
                    lastGyroData[0] = x
                    lastGyroData[1] = y
                    lastGyroData[2] = z
                    hasGyroData = true
                }
            }

            // Salvar no CSV se estiver gravando e tiver ambos os dados
            if (isRecording && hasAccData && hasGyroData) {
                saveSensorDataToCsv()
            }
        }
    }

    private fun saveSensorDataToCsv() {
        try {
            val currentTime = System.currentTimeMillis()
            val elapsedMs = currentTime - recordingStartTime

            val accMagnitude = sqrt(
                lastAccData[0]*lastAccData[0] +
                        lastAccData[1]*lastAccData[1] +
                        lastAccData[2]*lastAccData[2]
            )

            val gyroMagnitude = sqrt(
                lastGyroData[0]*lastGyroData[0] +
                        lastGyroData[1]*lastGyroData[1] +
                        lastGyroData[2]*lastGyroData[2]
            )

            val line = String.format(Locale.US,
                "%d,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f\n",
                currentTime, elapsedMs,
                lastAccData[0], lastAccData[1], lastAccData[2], accMagnitude,
                lastGyroData[0], lastGyroData[1], lastGyroData[2], gyroMagnitude
            )

            csvWriter?.write(line)
            csvWriter?.flush() // Garantir que dados sejam escritos

            dataPointCount++

            // Atualizar status a cada 10 pontos para não sobrecarregar a UI
            if (dataPointCount % 10 == 0) {
                runOnUiThread {
                    updateButtonStates()
                }
            }

        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Erro ao salvar dados: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Implementar se necessário
    }
}