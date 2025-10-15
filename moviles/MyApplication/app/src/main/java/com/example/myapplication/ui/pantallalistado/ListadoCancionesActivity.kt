package com.example.myapplication.ui.pantallalistado

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityListadoCancionesBinding
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.domain.modelo.Cancion

class ListadoCancionesActivity : ComponentActivity() {
    private val viewModel: ListadoCancionesViewModel by viewModels()
    private lateinit var adapter: CancionAdapter
    private lateinit var binding: ActivityListadoCancionesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListadoCancionesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        adapter = CancionAdapter ( this::sacarCancion)
        binding.recyclerViewCanciones.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCanciones.adapter = adapter

        viewModel.state.observe(this) { state ->
            adapter.submitList(state.canciones)
        }

        binding.mezclar.setOnClickListener {
            viewModel.mezclarCanciones()
        }
    }

    fun sacarCancion(cancion: Cancion) {
        viewModel.borrarCancion(cancion)
    }
}
