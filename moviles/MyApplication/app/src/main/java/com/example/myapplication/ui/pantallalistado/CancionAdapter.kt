package com.example.myapplication.ui.pantallalistado

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.domain.modelo.Cancion

class CancionAdapter(private var canciones: List<Cancion>) : RecyclerView.Adapter<CancionAdapter.CancionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cancion, parent, false)
        return CancionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CancionViewHolder, position: Int) {
        holder.bind(canciones[position])
    }

    override fun getItemCount(): Int = canciones.size

    fun updateCanciones(nuevasCanciones: List<Cancion>) {
        canciones = nuevasCanciones
        notifyDataSetChanged()
    }

    class CancionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitulo: TextView = itemView.findViewById(R.id.textViewTitulo)
        private val textViewAutor: TextView = itemView.findViewById(R.id.textViewAutor)

        fun bind(cancion: Cancion) {
            textViewTitulo.text = cancion.titulo
            textViewAutor.text = cancion.interprete
        }
    }
}

