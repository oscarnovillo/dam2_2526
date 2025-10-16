package com.example.myapplication.ui.pantallalistado

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemCancionBinding
import com.example.myapplication.domain.modelo.Cancion

class CancionAdapter(
    val actions : CancionesActions,
    val onClickView : (Cancion) -> Unit,
) : ListAdapter<Cancion, CancionAdapter.CancionViewHolder>(
    CancionDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CancionViewHolder {
        val binding = ItemCancionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CancionViewHolder(binding,onClickView,actions)
    }

    override fun onBindViewHolder(holder: CancionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class CancionViewHolder(private val binding: ItemCancionBinding,
                            val onClickView : (Cancion) -> Unit,
                            val actions : CancionesActions,
        ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cancion: Cancion) {
            binding.textViewTitulo.text = cancion.titulo
            binding.textViewAutor.text = cancion.interprete
            binding.root.setOnClickListener {
                onClickView(cancion)
                actions.onItemClick(cancion)
            }
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

    interface CancionesActions {
        fun onItemClick(cancion: Cancion)

    }
}
