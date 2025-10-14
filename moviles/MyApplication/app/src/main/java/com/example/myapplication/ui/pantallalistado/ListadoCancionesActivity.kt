package com.example.myapplication.ui.pantallalistado

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class ListadoCancionesActivity : ComponentActivity() {
    private val viewModel: ListadoCancionesViewModel by viewModels()
    private lateinit var adapter: CancionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado_canciones)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewCanciones)
        adapter = CancionAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.state.observe(this) { state ->
            adapter.updateCanciones(state.canciones)
        }
    }
}

