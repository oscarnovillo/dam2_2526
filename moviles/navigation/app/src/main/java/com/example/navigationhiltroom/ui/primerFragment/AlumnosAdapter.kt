package com.example.navigationhiltroom.ui.primerFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navigationhiltroom.databinding.ItemAlumnoBinding
import com.example.navigationhiltroom.domain.model.Alumno

class AlumnosAdapter : RecyclerView.Adapter<AlumnosAdapter.AlumnoViewHolder>() {

    private var alumnos: List<Alumno> = emptyList()

    fun setAlumnos(nuevosAlumnos: List<Alumno>) {
        alumnos = nuevosAlumnos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlumnoViewHolder {
        val binding = ItemAlumnoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlumnoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlumnoViewHolder, position: Int) {
        holder.bind(alumnos[position])
    }

    override fun getItemCount(): Int = alumnos.size

    class AlumnoViewHolder(private val binding: ItemAlumnoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alumno: Alumno) {
            binding.tvNombre.text = "${alumno.nombre} ${alumno.apellidos}"
            binding.tvEdad.text = "${alumno.edad}"
            binding.tvId.text = "ID: ${alumno.id}"
        }
    }
}

