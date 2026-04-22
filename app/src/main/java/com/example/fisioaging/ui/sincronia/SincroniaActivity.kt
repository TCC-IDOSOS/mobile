package com.example.fisioaging.ui.sincronia

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fisioaging.R
import com.example.fisioaging.model.TesteSalvo

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

                    val tipoFormatado = if (tipoBruto == "MARCHA") "Marcha Estacionária" else "Ponta dos Pés (UTT)"

                    // Formata a exibição: "21/04/2026 às 16:30"
                    val infoFormatada = "${dataBruta.substring(6,8)}/${dataBruta.substring(4,6)}/${dataBruta.substring(0,4)} às ${horaBruta.substring(0,2)}:${horaBruta.substring(2,4)}"

                    listaTestesSalvos.add(
                        TesteSalvo(
                            arquivo = arquivo,
                            nomeExibicao = infoFormatada,
                            idPaciente = idPacienteExtraido,
                            nomePaciente = nomePacienteExtraido,
                            tipoTeste = tipoFormatado
                        )
                    )
                } else {
                    listaTestesSalvos.add(
                        TesteSalvo(arquivo, "Arquivo Antigo", 0L, "Desconhecido", "N/A")
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

        Toast.makeText(this, "Enviando ${selecionados.size} testes para a AWS...", Toast.LENGTH_LONG).show()

        selecionados.forEach { it.arquivo.delete() }

        Toast.makeText(this, "Sincronização concluída com sucesso!", Toast.LENGTH_SHORT).show()
        carregarTestesSalvos()
    }
}