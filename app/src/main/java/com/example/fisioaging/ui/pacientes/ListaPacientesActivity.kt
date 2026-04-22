package com.example.fisioaging.ui.pacientes

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fisioaging.R
import com.example.fisioaging.network.RetrofitClient
import com.example.fisioaging.ui.selecao.SelecaoTesteActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListaPacientesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PacienteAdapter
    private lateinit var loadingBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_pacientes)

        supportActionBar?.title = "Selecionar Paciente"

        loadingBar = findViewById(R.id.progress_loading)
        recyclerView = findViewById(R.id.recycler_pacientes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        buscarPacientes()
    }

    private fun buscarPacientes() {
        loadingBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val todosUsuarios = RetrofitClient.instance.getUsuarios()

                val pacientes = todosUsuarios.filter { it.profile == "Paciente" }

                withContext(Dispatchers.Main) {
                    loadingBar.visibility = View.GONE

                    if (pacientes.isEmpty()) {
                        Toast.makeText(this@ListaPacientesActivity, "Nenhum paciente encontrado.", Toast.LENGTH_SHORT).show()
                    }

                    adapter = PacienteAdapter(pacientes) { paciente ->
                        val intent = Intent(this@ListaPacientesActivity, SelecaoTesteActivity::class.java)
                        intent.putExtra("PACIENTE_SELECIONADO", paciente)
                        startActivity(intent)
                    }
                    recyclerView.adapter = adapter
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingBar.visibility = View.GONE
                    Toast.makeText(this@ListaPacientesActivity, "Erro ao conectar com o servidor AWS", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }
}