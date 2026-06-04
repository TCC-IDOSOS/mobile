package com.example.fisioaging.ui.sincronia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fisioaging.R
import com.example.fisioaging.model.TesteSalvo

class SincroniaAdapter(private val testes: MutableList<TesteSalvo>) :
    RecyclerView.Adapter<SincroniaAdapter.TesteViewHolder>() {

    class TesteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkbox_teste)
        val nomeArquivo: TextView = view.findViewById(R.id.text_nome_arquivo)
        val nomePaciente: TextView = view.findViewById(R.id.text_nome_paciente)
        val tipoTeste: TextView = view.findViewById(R.id.text_tipo_teste)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TesteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teste_sincronia, parent, false)
        return TesteViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TesteViewHolder,
        position: Int
    ) {
        val teste = testes[position]

        holder.tipoTeste.text = teste.tipoTeste

        holder.nomePaciente.text =
            teste.nomePaciente.replace("%20", " ")

        holder.nomeArquivo.text =
            teste.nomeExibicao

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = teste.isSelecionado

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            teste.isSelecionado = isChecked
        }
    }

    override fun getItemCount() = testes.size

    // Utilitário para a Activity pegar rapidamente só o que foi marcado para envio/exclusão
    fun getItensSelecionados(): List<TesteSalvo> {
        return testes.filter { it.isSelecionado }
    }
}