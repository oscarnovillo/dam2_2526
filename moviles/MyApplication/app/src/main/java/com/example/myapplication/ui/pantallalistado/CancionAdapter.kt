package com.example.myapplication.ui.pantallalistado

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.domain.modelo.Cancion

class CancionAdapter : ListAdapter<Cancion, CancionAdapter.CancionViewHolder>(CancionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cancion, parent, false)
        return CancionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CancionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CancionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitulo: TextView = itemView.findViewById(R.id.textViewTitulo)
        private val textViewAutor: TextView = itemView.findViewById(R.id.textViewAutor)

        fun bind(cancion: Cancion) {
            textViewTitulo.text = cancion.titulo
            textViewAutor.text = cancion.interprete
        }
    }

    class CancionDiffCallback : DiffUtil.ItemCallback<Cancion>() {
        override fun areItemsTheSame(oldItem: Cancion, newItem: Cancion): Boolean {
            // Suponiendo que el título y el intérprete identifican de forma única una canción
            return oldItem.titulo == newItem.titulo && oldItem.interprete == newItem.interprete
        }
        override fun areContentsTheSame(oldItem: Cancion, newItem: Cancion): Boolean {
            return oldItem == newItem
        }
    }
}
