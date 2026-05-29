package com.example.fisioaging.ui.pacientes

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
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
    private lateinit var loadingText: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var searchView: SearchView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_pacientes)

        supportActionBar?.title = "Pacientes"

        sessionManager = SessionManager(this)

        loadingBar = findViewById(R.id.progress_loading)
        loadingText = findViewById(R.id.txt_loading)
        emptyMessage = findViewById(R.id.txt_empty)
        recyclerView = findViewById(R.id.recycler_pacientes)
        searchView = findViewById(R.id.searchViewPacientes)

        recyclerView.layoutManager = LinearLayoutManager(this)

        configurarBusca()

        buscarPacientes()
    }

    private fun configurarBusca() {
        searchView.queryHint = "Buscar por nome ou CPF"

        searchView.onActionViewExpanded()

        val searchEditText =
            searchView.findViewById<android.widget.EditText>(
                androidx.appcompat.R.id.search_src_text
            )

        searchEditText.setTextColor(android.graphics.Color.BLACK)
        searchEditText.setHintTextColor(android.graphics.Color.GRAY)

        val searchIcon =
            searchView.findViewById<android.widget.ImageView>(
                androidx.appcompat.R.id.search_mag_icon
            )

        searchIcon.setColorFilter(android.graphics.Color.BLACK)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (::adapter.isInitialized) {
                    adapter.filtrar(newText ?: "")
                }
                return true
            }
        })
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
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    private fun buscarPacientes() {
        mostrarLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = sessionManager.fetchAuthToken()
                val service = RetrofitClient.create(token)

                val todosUsuarios = service.getUsuarios()

                val pacientes = todosUsuarios.filter {
                    it.profile == "Paciente"
                }

                withContext(Dispatchers.Main) {

                    mostrarLoading(false)

                    if (pacientes.isEmpty()) {
                        emptyMessage.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyMessage.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }

                    adapter = PacienteAdapter(pacientes) { paciente ->
                        val intent = Intent(
                            this@ListaPacientesActivity,
                            HistoricoTestesActivity::class.java
                        )

                        intent.putExtra(
                            "PACIENTE_SELECIONADO",
                            paciente
                        )

                        startActivity(intent)
                    }

                    recyclerView.adapter = adapter
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {
                    mostrarLoading(false)

                    Toast.makeText(
                        this@ListaPacientesActivity,
                        "Erro ao conectar com o servidor AWS",
                        Toast.LENGTH_LONG
                    ).show()

                    emptyMessage.visibility = View.VISIBLE
                    emptyMessage.text =
                        "Não foi possível carregar os pacientes"

                    recyclerView.visibility = View.GONE
                }
            }
        }
    }

    private fun mostrarLoading(loading: Boolean) {
        loadingBar.visibility =
            if (loading) View.VISIBLE else View.GONE

        loadingText.visibility =
            if (loading) View.VISIBLE else View.GONE
    }
}