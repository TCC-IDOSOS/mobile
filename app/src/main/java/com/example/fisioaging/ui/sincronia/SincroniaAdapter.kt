package com.example.fisioaging.ui.sincronia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fisioaging.R
import com.example.fisioaging.model.TesteSalvo

// REMOVIDO: data class TesteSalvo (Ela deve existir apenas no arquivo TesteSalvo.kt)
// REMOVIDO: import androidx.compose.ui.test.isSelected (Isso é para testes e causa erro)

class SincroniaAdapter(private val testes: MutableList<TesteSalvo>) :
    RecyclerView.Adapter<SincroniaAdapter.TesteViewHolder>() {

    class TesteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkbox_teste)
        val nomeArquivo: TextView = view.findViewById(R.id.text_nome_arquivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TesteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teste_sincronia, parent, false)
        return TesteViewHolder(view)
    }

    override fun onBindViewHolder(holder: TesteViewHolder, position: Int) {
        val teste = testes[position]

        // Usa o nome formatado que a Activity enviou
        holder.nomeArquivo.text = teste.nomeExibicao

        // Evita bugs ao reciclar a lista
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = teste.isSelecionado

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            teste.isSelecionado = isChecked
        }
    }

    override fun getItemCount() = testes.size

    // Filtra os itens que o usuário marcou o Checkbox
    fun getItensSelecionados(): List<TesteSalvo> {
        return testes.filter { it.isSelecionado }
    }
}