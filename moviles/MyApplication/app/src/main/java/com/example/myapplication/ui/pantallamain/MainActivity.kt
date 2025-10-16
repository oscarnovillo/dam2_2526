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
import com.example.myapplication.ui.common.UiEvent
import com.google.android.material.snackbar.Snackbar
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
            with(binding) {
                textTitulo.setText(state.cancion.titulo)
                textInterprete?.setText(state.cancion.interprete)
                button.isEnabled = !state.isDisable

                state.uiEvent?.let { event ->
                    when (event) {
                        is UiEvent.ShowSnackbar -> {
                            val snackbar = Snackbar.make(
                                root,
                                event.message,
                                Snackbar.LENGTH_LONG
                            )
                            event.action?.let {
                                snackbar.setAction(event.action ?: "UNDO") {
                                    // Aquí puedes manejar la acción de deshacer si lo necesitas
                                }
                            }
                            snackbar.show()
                            viewModel.limpiarMensaje()
                        }
                        // Puedes manejar otros eventos aquí si los usas
                        is UiEvent.Navigate -> TODO()
                        else -> {}
                            // No hacer nada
                    }
                }
            }
        }
    }


    private fun eventos() {
        with(binding) {
            button.setOnClickListener {
                //Toast.makeText(this,"mensaje",Toast.LENGTH_LONG).show()

                // Obtener el tipo seleccionado del RadioGroup usando ViewBinding
                val tipoSeleccionado = when (radioGroupTipo?.checkedRadioButtonId) {
                    R.id.radioSolista -> "solista"
                    R.id.radioGrupo -> "grupo"
                    else -> "solista" // valor por defecto
                }

                var cancion = Cancion(
                    textTitulo.text.toString(),
                    textInterprete?.text.toString() ?: "",
                    tipoSeleccionado
                )


                viewModel.clickButtonGuardar(cancion)

            }
            buttonPrimero.setOnClickListener {
                //Toast.makeText(this,"mensaje",Toast.LENGTH_LONG).show()
                viewModel.pasarCancion()
            }

            buttonVerListado?.setOnClickListener {
                val intent = android.content.Intent(
                    this@MainActivity,
                    com.example.myapplication.ui.pantallalistado.ListadoCancionesActivity::class.java
                )
                startActivity(intent)
            }
        }
    }
}