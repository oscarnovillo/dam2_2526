package com.example.myapplication.ui.pantallamain

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.domain.modelo.Cancion
import kotlin.getValue

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private val viewModel: MainViewModel by viewModels(){
        MainViewModelFactory()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        eventos()
        observacion()


    }

    private fun observacion() {
        viewModel.state.observe(this) { state ->
            binding.textTitulo.setText(state.cancion.titulo)
            binding.textInterprete?.setText(state.cancion.interprete)
            binding.button.isEnabled = !state.isDisable

            state.mensaje?.let { error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                viewModel.limpiarMensaje()
            }

    //            txt.text = state.textoLabel
    //
    //            editText.setText(state.textoCaja)

        }
    }


    private fun eventos() {
        binding.button.setOnClickListener {
            //Toast.makeText(this,"mensaje",Toast.LENGTH_LONG).show()

            // Obtener el tipo seleccionado del RadioGroup usando ViewBinding
            val tipoSeleccionado = when(binding.radioGroupTipo?.checkedRadioButtonId) {
                R.id.radioSolista -> "solista"
                R.id.radioGrupo -> "grupo"
                else -> "solista" // valor por defecto
            }

            var cancion = Cancion(
                binding.textTitulo.text.toString(),
                binding.textInterprete?.text.toString() ?: "",
                tipoSeleccionado
            )
            viewModel.clickButtonGuardar(cancion)

        }
       binding.buttonPrimero.setOnClickListener {
            //Toast.makeText(this,"mensaje",Toast.LENGTH_LONG).show()
            viewModel.pasarCancion()
        }

        binding.buttonVerListado?.setOnClickListener {
            val intent = android.content.Intent(this, com.example.myapplication.ui.pantallalistado.ListadoCancionesActivity::class.java)
            startActivity(intent)
        }
    }
}