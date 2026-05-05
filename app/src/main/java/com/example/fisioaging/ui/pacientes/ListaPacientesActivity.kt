package com.example.fisioaging.ui.pacientes

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fisioaging.R
import com.example.fisioaging.network.RetrofitClient
import com.example.fisioaging.ui.historico.HistoricoTestesActivity
import com.example.fisioaging.ui.login.LoginActivity
import com.example.fisioaging.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListaPacientesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PacienteAdapter
    private lateinit var loadingBar: ProgressBar
    private lateinit var searchView: SearchView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_pacientes)

        supportActionBar?.title = "Selecionar Paciente"

        sessionManager = SessionManager(this)
        loadingBar = findViewById(R.id.progress_loading)
        recyclerView = findViewById(R.id.recycler_pacientes)
        searchView = findViewById(R.id.searchViewPacientes)

        recyclerView.layoutManager = LinearLayoutManager(this)

        searchView.onActionViewExpanded()

        searchView.setOnSearchClickListener {
            searchView.requestFocus()
        }

        val searchEditText = searchView.findViewById<android.widget.EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(android.graphics.Color.BLACK)
        searchEditText.setHintTextColor(android.graphics.Color.GRAY)

        val searchIcon = searchView.findViewById<android.widget.ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setColorFilter(android.graphics.Color.BLACK)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (::adapter.isInitialized) {
                    adapter.filtrar(newText ?: "")
                }
                return true
            }
        })

        buscarPacientes()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_logout -> {
                deslogar()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deslogar() {
        sessionManager.clearSession()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun buscarPacientes() {
        loadingBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sessionManager.fetchAuthToken()
                val service = RetrofitClient.create(token)
                val todosUsuarios = service.getUsuarios()

                val pacientes = todosUsuarios.filter { it.profile == "Paciente" }

                withContext(Dispatchers.Main) {
                    loadingBar.visibility = View.GONE

                    if (pacientes.isEmpty()) {
                        Toast.makeText(this@ListaPacientesActivity, "Nenhum paciente encontrado.", Toast.LENGTH_SHORT).show()
                    }

                    adapter = PacienteAdapter(pacientes) { paciente ->
                        val intent = Intent(this@ListaPacientesActivity, HistoricoTestesActivity::class.java)
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
