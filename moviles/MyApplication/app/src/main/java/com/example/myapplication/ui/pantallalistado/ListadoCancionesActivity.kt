package com.example.myapplication.ui.pantallalistado

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityListadoCancionesBinding
import com.example.myapplication.databinding.ActivityMainBinding

class ListadoCancionesActivity : ComponentActivity() {
    private val viewModel: ListadoCancionesViewModel by viewModels()
    private lateinit var adapter: CancionAdapter
    private lateinit var binding: ActivityListadoCancionesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListadoCancionesBinding.inflate(layoutInflater)
        setContentView(binding.root)


        adapter = CancionAdapter()
        binding.recyclerViewCanciones.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCanciones.adapter = adapter

        viewModel.state.observe(this) { state ->
            adapter.submitList(state.canciones)
        }
    }
}
