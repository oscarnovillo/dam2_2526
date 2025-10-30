package com.example.navigationhiltroom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.navigationhiltroom.databinding.FragmentBlankBinding
import com.example.navigationhiltroom.databinding.FragmentCuartoBinding
import kotlin.getValue


class BlankFragment : Fragment() {


    private var _binding: FragmentBlankBinding? = null
    private val binding get() = _binding!!

    private val args: BlankFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBlankBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {

            text.text = "El valor es: ${args.id}"
        }


    }

}