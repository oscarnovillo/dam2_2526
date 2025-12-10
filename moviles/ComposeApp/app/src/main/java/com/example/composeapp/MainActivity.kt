package com.example.composeapp

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composeapp.ui.theme.ComposeAppTheme
import com.example.composeapp.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UserFormScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class Usuario(
    var nombre: String = "",
    var apellidos: String = "",
    var telefono: String = "",
    var email: String = "",
    var fechaNacimiento: String = "",
    var genero: String = "",
    var comentarios: String = "",
    var tieneTV: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Título
        Text(
            text = "Añadir Nuevo Usuario",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
        )

        // Fila: Campo Nombre + CheckBox TV
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = { viewModel.updateNombre(it) },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Checkbox(
                    checked = uiState.tieneTV,
                    onCheckedChange = { viewModel.updateTieneTV(it) }
                )
                Text("¿TV?", fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fila: Apellidos + Teléfono
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.apellidos,
                onValueChange = { viewModel.updateApellidos(it) },
                label = { Text("Apellidos") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = uiState.telefono,
                onValueChange = { viewModel.updateTelefono(it) },
                label = { Text("Teléfono") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fila: Email + Fecha Nacimiento
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = uiState.fechaNacimiento,
                onValueChange = { },
                label = { Text("Fecha Nac.") },
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                readOnly = true,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                viewModel.updateFechaNacimiento("$day/${month + 1}/$year")
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Género
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Género:",
                fontSize = 14.sp,
                color = Color.Black,
                modifier = Modifier.padding(end = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.generoSeleccionado == "M",
                        onClick = { viewModel.updateGenero("M") }
                    )
                    Text("M", modifier = Modifier.padding(end = 8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.generoSeleccionado == "F",
                        onClick = { viewModel.updateGenero("F") }
                    )
                    Text("F", modifier = Modifier.padding(end = 8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.generoSeleccionado == "Otro",
                        onClick = { viewModel.updateGenero("Otro") }
                    )
                    Text("Otro")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Comentarios
        OutlinedTextField(
            value = uiState.comentarios,
            onValueChange = { viewModel.updateComentarios(it) },
            label = { Text("Comentarios") },
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botones de navegación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.navegarAnterior() },
                enabled = uiState.indiceActual > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0099CC)),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .padding(end = 4.dp)
            ) {
                Text("← Ant.", fontSize = 12.sp)
            }

            Text(
                text = if (uiState.usuarios.isEmpty()) "0/0" else "${uiState.indiceActual + 1}/${uiState.usuarios.size}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .widthIn(min = 40.dp)
            )

            Button(
                onClick = { viewModel.navegarSiguiente() },
                enabled = uiState.indiceActual < uiState.usuarios.size - 1,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0099CC)),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .padding(start = 4.dp)
            ) {
                Text("Sig. →", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Limpiar
            Button(
                onClick = { viewModel.limpiarFormulario() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Limpiar", fontSize = 12.sp)
            }

            // Actualizar
            Button(
                onClick = { viewModel.actualizarUsuario() },
                enabled = uiState.indiceActual >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8800)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Actualizar", fontSize = 12.sp)
            }

            // Borrar
            Button(
                onClick = { viewModel.borrarUsuario() },
                enabled = uiState.indiceActual >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Borrar", fontSize = 12.sp)
            }

            // Guardar
            Button(
                onClick = { viewModel.guardarUsuario() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF669900)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Guardar", fontSize = 12.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserFormScreenPreview() {
    ComposeAppTheme {
        UserFormScreen()
    }
}

