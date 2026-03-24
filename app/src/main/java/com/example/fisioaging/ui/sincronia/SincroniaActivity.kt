package com.example.fisioaging.ui.sincronia

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fisioaging.R
import com.example.fisioaging.model.TesteSalvo
import kotlin.collections.forEach

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
        // O Adapter vai gerenciar a exibição baseada no que colocarmos no TesteSalvo
        adapter = SincroniaAdapter(listaTestesSalvos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun configurarBotoes() {
        val btnSincronizar: Button = findViewById(R.id.btn_sincronizar)
        val btnApagar: Button = findViewById(R.id.btn_apagar)

        btnApagar.setOnClickListener {
            apagarArquivosSelecionados()
        }

        btnSincronizar.setOnClickListener {
            sincronizarArquivosSelecionados()
        }
    }

    override fun onResume() {
        super.onResume()
        carregarTestesSalvos()
    }

    private fun carregarTestesSalvos() {
        listaTestesSalvos.clear()
        val diretorioArquivos = filesDir

        // Filtra arquivos JSON que começam com MARCHA_ ou UTT_
        val arquivos = diretorioArquivos.listFiles { _, nome ->
            nome.endsWith(".json") && (nome.startsWith("MARCHA_") || nome.startsWith("UTT_"))
        }


        arquivos?.forEach { arquivo ->
            val infoFormatada = formatarNomeDoTeste(arquivo.name)

            listaTestesSalvos.add(TesteSalvo(arquivo, infoFormatada))
        }

        // Ordena por data (mais recente primeiro)
        listaTestesSalvos.sortByDescending { it.arquivo.lastModified() }

        adapter.notifyDataSetChanged()
    }

    // Função auxiliar para transformar "MARCHA_20260320_210055.json" em algo legível
    private fun formatarNomeDoTeste(nomeArquivo: String): String {
        return try {
            val partes = nomeArquivo.split("_")
            val tipo = if (partes[0] == "MARCHA") "Marcha Estacionária" else "Ponta dos Pés (UTT)"

            // Extrai a data (Ex: 20260320)
            val dataBruta = partes[1]
            val ano = dataBruta.substring(0, 4)
            val mes = dataBruta.substring(4, 6)
            val dia = dataBruta.substring(6, 8)

            // Extrai a hora (Ex: 210055)
            val horaBruta = partes[2]
            val hora = horaBruta.substring(0, 2)
            val min = horaBruta.substring(2, 4)

            "$tipo\nRealizado em: $dia/$mes/$ano às $hora:$min"
        } catch (e: Exception) {
            "Teste: $nomeArquivo" // Fallback caso o nome esteja fora do padrão
        }
    }

    private fun apagarArquivosSelecionados() {
        val selecionados = adapter.getItensSelecionados()
        if (selecionados.isEmpty()) {
            Toast.makeText(this, "Nenhum teste selecionado", Toast.LENGTH_SHORT).show()
            return
        }

        var contagemApagados = 0
        selecionados.forEach { testeSalvo ->
            if (testeSalvo.arquivo.delete()) {
                contagemApagados++
            }
        }

        Toast.makeText(this, "$contagemApagados testes apagados", Toast.LENGTH_SHORT).show()
        carregarTestesSalvos()
    }

    private fun sincronizarArquivosSelecionados() {
        val selecionados = adapter.getItensSelecionados()
        if (selecionados.isEmpty()) {
            Toast.makeText(this, "Selecione testes para sincronizar", Toast.LENGTH_SHORT).show()
            return
        }

        // --- Chamada para a API (QUANDO ESTIVER PRONTA)

        Toast.makeText(this, "Sincronizando ${selecionados.size} arquivos com o servidor...", Toast.LENGTH_LONG).show()

        // Simulação: Após o envio bem-sucedido, removemos do celular para liberar espaço
        selecionados.forEach { testeSalvo ->

            testeSalvo.arquivo.delete()
        }

        Toast.makeText(this, "Sincronização concluída!", Toast.LENGTH_SHORT).show()
        carregarTestesSalvos()
    }
}