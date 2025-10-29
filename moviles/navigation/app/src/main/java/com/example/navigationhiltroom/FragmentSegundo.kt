package com.example.navigationhiltroom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigationhiltroom.databinding.FragmentCuartoBinding
import com.example.navigationhiltroom.databinding.FragmentSegundoBinding

class FragmentSegundo : Fragment() {
    private var _binding: FragmentSegundoBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSegundoBinding.inflate(inflater, container, false)
        return binding.root
    }
}