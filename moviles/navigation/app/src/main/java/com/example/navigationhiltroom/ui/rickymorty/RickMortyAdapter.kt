package com.example.navigationhiltroom.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.navigationhiltroom.R
import com.example.navigationhiltroom.data.remote.entity.RickMortyCharacter

import com.example.navigationhiltroom.databinding.ItemRickMortyCharacterBinding

class RickMortyAdapter(
    private val onCharacterClick: (RickMortyCharacter) -> Unit
) : ListAdapter<RickMortyCharacter, RickMortyAdapter.CharacterViewHolder>(CharacterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val binding = ItemRickMortyCharacterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CharacterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CharacterViewHolder(
        private val binding: ItemRickMortyCharacterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(character: RickMortyCharacter) {
            binding.apply {
                tvCharacterName.text = character.name
                tvCharacterStatus.text = "${character.status} - ${character.species}"
                tvCharacterLocation.text = character.location.name

                // Cargar imagen con Coil
                ivCharacterImage.load(character.image) {
                    // crossfade: Animación suave de transición (true = 300ms por defecto)
                    crossfade(true)

                    // placeholder: Imagen que se muestra MIENTRAS se está cargando
                    placeholder(R.drawable.ic_launcher_foreground)

                    // error: Imagen que se muestra SI falla la carga
                    error(R.drawable.ic_launcher_foreground)

                    // Callbacks opcionales para controlar el estado de carga
                    listener(
                        onStart = {
                            // Se ejecuta cuando EMPIEZA a cargar la imagen
                            // Aquí podrías mostrar un ProgressBar
                        },
                        onSuccess = { _, _ ->
                            // Se ejecuta cuando la imagen se carga EXITOSAMENTE
                            // Aquí podrías ocultar el ProgressBar
                        },
                        onError = { _, throwable ->
                            // Se ejecuta si hay un ERROR al cargar
                            // Aquí podrías mostrar un mensaje de error
                        }
                    )

                    // Otras opciones útiles:
                    // transformations(CircleCropTransformation()) // Para imagen circular
                    // size(100, 100) // Tamaño específico
                    // scale(Scale.FILL) // Cómo escalar la imagen
                }

                root.setOnClickListener {
                    onCharacterClick(character)
                }
            }
        }
    }

    private class CharacterDiffCallback : DiffUtil.ItemCallback<RickMortyCharacter>() {
        override fun areItemsTheSame(
            oldItem: RickMortyCharacter,
            newItem: RickMortyCharacter
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: RickMortyCharacter,
            newItem: RickMortyCharacter
        ): Boolean = oldItem == newItem
    }
}
