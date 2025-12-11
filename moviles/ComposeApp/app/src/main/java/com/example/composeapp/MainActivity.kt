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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.composeapp.ui.theme.ComposeAppTheme
import com.example.composeapp.ui.theme.Dimens
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

@Composable
fun UserFormScreen(
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(Dimens.paddingMedium)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge)
    ) {
        // Título
        Text(
            text = "Añadir Nuevo Usuario",
            fontSize = Dimens.textSizeTitle,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Fila: Campo Nombre + CheckBox TV
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.usuarioActual.nombre,
                onValueChange = { viewModel.updateUsuario(uiState.usuarioActual.copy(nombre = it)) },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = Dimens.paddingSmall)
            ) {
                Checkbox(
                    checked = uiState.usuarioActual.tieneTV,
                    onCheckedChange = { viewModel.updateUsuario(uiState.usuarioActual.copy(tieneTV = it)) }
                )
                Text("¿TV?", fontSize = Dimens.textSizeMedium)
            }
        }


        // Fila: Apellidos + Teléfono
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
        ) {
            OutlinedTextField(
                value = uiState.usuarioActual.apellidos,
                onValueChange = { viewModel.updateUsuario(uiState.usuarioActual.copy(apellidos = it)) },
                label = { Text("Apellidos") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = uiState.usuarioActual.telefono,
                onValueChange = { viewModel.updateUsuario(uiState.usuarioActual.copy(telefono = it)) },
                label = { Text("Teléfono") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }


        // Fila: Email + Fecha Nacimiento
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
        ) {
            OutlinedTextField(
                value = uiState.usuarioActual.email,
                onValueChange = { viewModel.updateUsuario(uiState.usuarioActual.copy(email = it)) },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = uiState.usuarioActual.fechaNacimiento,
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
                                viewModel.updateUsuario(uiState.usuarioActual.copy(fechaNacimiento = "$day/${month + 1}/$year"))
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
            )
        }


        // Género
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Género:",
                fontSize = Dimens.textSizeMedium,
                color = Color.Black,
                modifier = Modifier.padding(end = Dimens.paddingSmall)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.usuarioActual.genero == "M",
                        onClick = { viewModel.updateUsuario(uiState.usuarioActual.copy(genero = "M")) }
                    )
                    Text("M", modifier = Modifier.padding(end = Dimens.paddingSmall))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.usuarioActual.genero == "F",
                        onClick = { viewModel.updateUsuario(uiState.usuarioActual.copy(genero = "F")) }
                    )
                    Text("F", modifier = Modifier.padding(end = Dimens.paddingSmall))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.usuarioActual.genero == "Otro",
                        onClick = { viewModel.updateUsuario(uiState.usuarioActual.copy(genero = "Otro")) }
                    )
                    Text("Otro")
                }
            }
        }


        // Comentarios
        OutlinedTextField(
            value = uiState.usuarioActual.comentarios,
            onValueChange = { viewModel.updateUsuario(uiState.usuarioActual.copy(comentarios = it)) },
            label = { Text("Comentarios") },
            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.textAreaHeight),
            maxLines = 4
        )


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
                    .height(Dimens.buttonHeightSmall)
                    .padding(end = Dimens.paddingExtraSmall)
            ) {
                Text("← Ant.", fontSize = Dimens.textSizeSmall)
            }

            Text(
                text = if (uiState.usuarios.isEmpty()) "0/0" else "${uiState.indiceActual + 1}/${uiState.usuarios.size}",
                fontSize = Dimens.textSizeMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = Dimens.paddingSmall)
                    .widthIn(min = 40.dp)
            )

            Button(
                onClick = { viewModel.navegarSiguiente() },
                enabled = uiState.indiceActual < uiState.usuarios.size - 1,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0099CC)),
                modifier = Modifier
                    .weight(1f)
                    .height(Dimens.buttonHeightSmall)
                    .padding(start = Dimens.paddingExtraSmall)
            ) {
                Text("Sig. →", fontSize = Dimens.textSizeSmall)
            }
        }


        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
        ) {
            // Limpiar
            Button(
                onClick = { viewModel.limpiarFormulario() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier
                    .weight(1f)
                    .height(Dimens.buttonHeightMedium)
            ) {
                Text("Limpiar", fontSize = Dimens.textSizeSmall)
            }

            // Actualizar
            Button(
                onClick = { viewModel.actualizarUsuario() },
                enabled = uiState.indiceActual >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8800)),
                modifier = Modifier
                    .weight(1f)
                    .height(Dimens.buttonHeightMedium)
            ) {
                Text("Actualizar", fontSize = Dimens.textSizeSmall)
            }

            // Borrar
            Button(
                onClick = { viewModel.borrarUsuario() },
                enabled = uiState.indiceActual >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000)),
                modifier = Modifier
                    .weight(1f)
                    .height(Dimens.buttonHeightMedium)
            ) {
                Text("Borrar", fontSize = Dimens.textSizeSmall)
            }

            // Guardar
            Button(
                onClick = { viewModel.guardarUsuario() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF669900)),
                modifier = Modifier
                    .weight(1f)
                    .height(Dimens.buttonHeightMedium)
            ) {
                Text("Guardar", fontSize = Dimens.textSizeSmall)
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

