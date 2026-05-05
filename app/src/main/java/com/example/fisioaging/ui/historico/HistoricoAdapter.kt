package com.example.fisioaging.ui.historico

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fisioaging.R
import com.example.fisioaging.model.TesteResponse
import java.text.SimpleDateFormat
import java.util.*

class HistoricoAdapter(
    private val tests: List<TesteResponse>,
    private val onItemClick: (TesteResponse) -> Unit
) : RecyclerView.Adapter<HistoricoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTipo: TextView = view.findViewById(R.id.txt_tipo_teste)
        val txtData: TextView = view.findViewById(R.id.txt_data_hora)
        val txtRepeticoes: TextView = view.findViewById(R.id.txt_repeticoes)
        val txtStatus: TextView = view.findViewById(R.id.txt_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teste_historico, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val teste = tests[position]
        val context = holder.itemView.context
        
        holder.txtTipo.text = if (teste.testType == "MARCHA") "Marcha Estacionária" else "Ponta dos Pés (UTT)"
        holder.txtData.text = formatarData(teste.testDateTime)
        holder.txtRepeticoes.text = "Repetições: ${teste.totalRepetitionsApp}"
        
        holder.txtStatus.text = traduzirStatus(teste.status)
        val colorRes = when (teste.status.uppercase()) {
            "PENDING" -> R.color.status_pending
            "UPLOADED" -> R.color.status_uploaded
            "PROCESSED", "COMPLETED" -> R.color.status_processed
            else -> R.color.black
        }
        holder.txtStatus.setTextColor(ContextCompat.getColor(context, colorRes))

        holder.itemView.setOnClickListener { onItemClick(teste) }
    }

    override fun getItemCount() = tests.size

    private fun formatarData(dataBruta: String): String {
        val formatos = listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyyMMdd_HHmmss")
        for (formato in formatos) {
            try {
                val inputFormat = SimpleDateFormat(formato, Locale.getDefault())
                if (formato.contains("Z")) inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(dataBruta)
                return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date!!)
            } catch (e: Exception) { }
        }
        return dataBruta
    }

    private fun traduzirStatus(status: String): String {
        return when (status.uppercase()) {
            "UPLOADED" -> "Sincronizado"
            "PENDING" -> "Pendente"
            "PROCESSED" -> "Processado"
            "COMPLETED" -> "Concluído"
            else -> status
        }
    }
}
