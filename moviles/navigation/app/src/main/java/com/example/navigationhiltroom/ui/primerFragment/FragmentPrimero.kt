package com.example.navigationhiltroom.ui.primerFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navigationhiltroom.databinding.FragmentPrimeroBinding
import com.example.navigationhiltroom.domain.model.Alumno
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentPrimero : Fragment() {

    private var _binding: FragmentPrimeroBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlumnosViewModel by viewModels ()

    private lateinit var alumnosAdapter: AlumnosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrimeroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeAlumnos()
    }

    private fun setupRecyclerView() {
        alumnosAdapter = AlumnosAdapter()
        binding.recyclerViewAlumnos.apply {
            adapter = alumnosAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeAlumnos() {
        viewModel.alumnos.observe(viewLifecycleOwner) { alumnos ->
            alumnosAdapter.setAlumnos(alumnos)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
