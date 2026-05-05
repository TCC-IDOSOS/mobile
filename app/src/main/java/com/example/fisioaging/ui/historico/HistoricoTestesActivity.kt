package com.example.fisioaging.ui.historico

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fisioaging.R
import com.example.fisioaging.model.Usuario
import com.example.fisioaging.network.RetrofitClient
import com.example.fisioaging.ui.selecao.SelecaoTesteActivity
import com.example.fisioaging.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoricoTestesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingBar: ProgressBar
    private lateinit var sessionManager: SessionManager
    private var paciente: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_testes)

        paciente = intent.getSerializableExtra("PACIENTE_SELECIONADO") as? Usuario
        sessionManager = SessionManager(this)

        supportActionBar?.title = "Histórico de Testes"
        paciente?.let {
            supportActionBar?.subtitle = it.name
        }

        recyclerView = findViewById(R.id.recycler_historico)
        loadingBar = findViewById(R.id.progress_loading)
        val btnNovoTeste = findViewById<Button>(R.id.btn_novo_teste)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnNovoTeste.setOnClickListener {
            val intent = Intent(this, SelecaoTesteActivity::class.java)
            intent.putExtra("PACIENTE_SELECIONADO", paciente)
            startActivity(intent)
        }

        buscarHistorico()
    }

    private fun buscarHistorico() {
        val email = paciente?.email ?: return
        loadingBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sessionManager.fetchAuthToken()
                val service = RetrofitClient.create(token)
                val testes = service.getTestesPaciente(email)

                withContext(Dispatchers.Main) {
                    loadingBar.visibility = View.GONE
                    if (testes.isEmpty()) {
                        Toast.makeText(this@HistoricoTestesActivity, "Nenhum teste encontrado.", Toast.LENGTH_SHORT).show()
                    }
                    recyclerView.adapter = HistoricoAdapter(testes) { teste ->
                        val intent = Intent(this@HistoricoTestesActivity, ResultadoTesteActivity::class.java)
                        intent.putExtra("TESTE_SELECIONADO", teste)
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingBar.visibility = View.GONE
                    Toast.makeText(this@HistoricoTestesActivity, "Erro ao carregar histórico", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }
}
