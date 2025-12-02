package com.example.navigationhiltroom.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navigationhiltroom.databinding.FragmentRickMortyListBinding
import com.example.navigationhiltroom.ui.adapter.RickMortyAdapter
import com.example.navigationhiltroom.ui.common.UiEvent
import com.example.navigationhiltroom.ui.rickymorty.RickMortyIntent
import com.example.navigationhiltroom.ui.rickymorty.RickMortyIntent.SearchCharacters
import com.example.navigationhiltroom.ui.viewmodel.RickMortyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RickMortyListFragment : Fragment() {

    private var _binding: FragmentRickMortyListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RickMortyViewModel by viewModels()
    private lateinit var adapter: RickMortyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRickMortyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeCharacters()
        observeEvents()
    }

    private fun setupRecyclerView() {
        adapter = RickMortyAdapter { character ->
            // Aquí podrías navegar a un detalle del personaje si lo deseas
            // findNavController().navigate(action)
        }

        binding.recyclerViewCharacters.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@RickMortyListFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { v, _, _ ->
            val query = v.text.toString()
            viewModel.handleIntent(SearchCharacters(query))
            true
        }
    }

    private fun observeCharacters() {
        viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.uiState.collect { state ->
                        adapter.submitList(state.characters)
                        binding.tvEmptyState.isVisible =
                            state.characters.isEmpty() && !state.isLoading
                        binding.progressBar.isVisible = state.isLoading
                        binding.recyclerViewCharacters.isVisible = !state.isLoading
                    }
                }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {

                viewModel.events
                    .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                    .collect { events ->
                    when (events) {
                        // Maneja otros eventos si es necesario
                        UiEvent.NavigateBack -> TODO()
                        is UiEvent.ShowError -> TODO()
                        is UiEvent.ShowSnackbar -> TODO()

                    }
                }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
