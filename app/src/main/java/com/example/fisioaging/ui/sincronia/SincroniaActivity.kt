package com.example.fisioaging.ui.sincronia

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fisioaging.R
import com.example.fisioaging.model.TesteRequest
import com.example.fisioaging.model.TesteSalvo
import com.example.fisioaging.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

class SincroniaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SincroniaAdapter
    private var listaTestesSalvos = mutableListOf<TesteSalvo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sincronia)
        supportActionBar?.title = "Sincronizar Testes"

        configurarRecyclerView()
        configurarBotoes()
    }

    private fun configurarRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view_testes)
        adapter = SincroniaAdapter(listaTestesSalvos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun configurarBotoes() {
        val btnSincronizar: Button = findViewById(R.id.btn_sincronizar)
        val btnApagar: Button = findViewById(R.id.btn_apagar)

        btnApagar.setOnClickListener { apagarArquivosSelecionados() }
        btnSincronizar.setOnClickListener { sincronizarArquivosSelecionados() }
    }

    override fun onResume() {
        super.onResume()
        carregarTestesSalvos()
    }

    private fun carregarTestesSalvos() {
        listaTestesSalvos.clear()
        val diretorioArquivos = filesDir

        // Filtra arquivos JSON que seguem o padrão TIPO_DATA_HORA_ID_NOME.json
        val arquivos = diretorioArquivos.listFiles { _, nome ->
            nome.endsWith(".json") && (nome.startsWith("MARCHA") || nome.startsWith("UTT"))
        }

        arquivos?.forEach { arquivo ->
            try {
                // Remove o ".json" e divide o nome pelas sublinhas
                val partes = arquivo.name.replace(".json", "").split("_")

                if (partes.size >= 5) {
                    val tipoBruto = partes[0]
                    val dataBruta = partes[1]
                    val horaBruta = partes[2]
                    val idPacienteExtraido = partes[3].toLong()
                    val nomePacienteExtraido = partes[4]
                    val emailPacienteExtraido = partes.getOrNull(5) ?: "desconhecido"

                    val tipoFormatado = if (tipoBruto == "MARCHA") "Marcha Estacionária" else "Ponta dos Pés (UTT)"

                    // Formata a exibição: "21/04/2026 às 16:30"
                    val infoFormatada = "${dataBruta.substring(6,8)}/${dataBruta.substring(4,6)}/${dataBruta.substring(0,4)} às ${horaBruta.substring(0,2)}:${horaBruta.substring(2,4)}"

                    listaTestesSalvos.add(
                        TesteSalvo(
                            arquivo = arquivo,
                            nomeExibicao = infoFormatada,
                            idPaciente = idPacienteExtraido,
                            nomePaciente = nomePacienteExtraido,
                            emailPaciente = emailPacienteExtraido,
                            tipoTeste = tipoFormatado
                        )
                    )
                } else {
                    listaTestesSalvos.add(
                        TesteSalvo(arquivo, "Arquivo Antigo", 0L, "Desconhecido", "desconhecido@teste.com","N/A")

                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        listaTestesSalvos.sortByDescending { it.arquivo.lastModified() }
        adapter.notifyDataSetChanged()
    }

    private fun apagarArquivosSelecionados() {
        val selecionados = adapter.getItensSelecionados()
        if (selecionados.isEmpty()) {
            Toast.makeText(this, "Nenhum teste selecionado", Toast.LENGTH_SHORT).show()
            return
        }

        selecionados.forEach { it.arquivo.delete() }
        Toast.makeText(this, "${selecionados.size} testes apagados", Toast.LENGTH_SHORT).show()
        carregarTestesSalvos()
    }

    private fun sincronizarArquivosSelecionados() {
        val selecionados = adapter.getItensSelecionados()

        if (selecionados.isEmpty()) {
            Toast.makeText(this, "Selecione testes para sincronizar", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Enviando ${selecionados.size} testes...", Toast.LENGTH_LONG).show()

        CoroutineScope(Dispatchers.IO).launch {
            val gson = Gson()

            selecionados.forEach { testeSalvo ->
                try {
                    val json = testeSalvo.arquivo.readText()

                    val request = gson.fromJson(json, TesteRequest::class.java)
                    println(testeSalvo.emailPaciente)


                    RetrofitClient.instance.enviarTeste(
                        email = testeSalvo.emailPaciente,
                        body = request
                    )

                    testeSalvo.arquivo.delete()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@SincroniaActivity, "Sincronização concluída!", Toast.LENGTH_SHORT).show()
                carregarTestesSalvos()
            }
        }
    }
}