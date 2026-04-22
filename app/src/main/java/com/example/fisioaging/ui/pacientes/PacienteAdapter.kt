package com.example.fisioaging.ui.pacientes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fisioaging.R
import com.example.fisioaging.model.Usuario

class PacienteAdapter(
    private val pacientes: List<Usuario>,
    private val onClick: (Usuario) -> Unit
) : RecyclerView.Adapter<PacienteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txt_nome_paciente)
        val txtCpf: TextView = view.findViewById(R.id.txt_cpf_paciente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_paciente, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paciente = pacientes[position]
        holder.txtNome.text = paciente.name
        holder.txtCpf.text = "CPF: ${paciente.cpf}"
        holder.itemView.setOnClickListener { onClick(paciente) }
    }

    override fun getItemCount() = pacientes.size
}