package com.example.composeapp.ui.screens.user

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices.TABLET
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.example.composeapp.domain.model.Usuario
import com.example.composeapp.ui.common.UiEvent

import com.example.composeapp.ui.componentes.BotonesActtion
import com.example.composeapp.ui.theme.ComposeAppTheme
import com.example.composeapp.ui.theme.Dimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar


@Composable
fun UserFormScreenViewModel(
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Observar eventos de un solo uso con lifecycle awareness
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        is UiEvent.ShowSnackbar -> {
                            snackbarHostState.showSnackbar(
                                message = event.message,
                                duration = SnackbarDuration.Short
                            )
                        }

                        is UiEvent.Navigate -> {
                            // Aquí puedes manejar navegación si es necesario
                        }
                    }
                }
            }
        }
    }

    UserFormScreen(uiState = uiState,
        snackbarHostState = snackbarHostState,
        onChangeUsusario = { usuario -> viewModel.updateUsuario(usuario) },
        onLimpiarFormulario = { viewModel.updateUsuario(Usuario()) },
        onNavegarSiguiente = { viewModel.cargarUsuario(uiState.indiceActual)},
        onNavegarAnterior = {
            viewModel.cargarUsuario(uiState.indiceActual - 1)
        },
        onGuardar = {
            viewModel.guardarUsuario()
        },
        onBorrar = { viewModel.borrarUsuario() },
        onActualizar = { viewModel.actualizarUsuario() },
    )






}


@Composable
fun UserFormScreen(modifier: Modifier = Modifier,
                   uiState : UserFormState,
                   snackbarHostState : SnackbarHostState = remember { SnackbarHostState() },
                   onChangeUsusario: (Usuario) -> Unit = {},
                   onLimpiarFormulario: () -> Unit = {},
                   onNavegarSiguiente: () -> Unit = {},
                   onNavegarAnterior: () -> Unit = {},
                   onGuardar: () -> Unit = {},
                   onBorrar: () -> Unit = {},
                   onActualizar: () -> Unit = {},
) {
    var context = LocalContext.current

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass




    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(Dimens.paddingMedium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingLarge)
        ) {
            if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND))
            {
                Text(text="TABLET")
            }
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
                    onValueChange = { onChangeUsusario(uiState.usuarioActual.copy(nombre = it)) },
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
                        onCheckedChange = { onChangeUsusario(uiState.usuarioActual.copy(tieneTV = it)) }
                    )
                    Text("¿TV?", fontSize = Dimens.textSizeMedium)
                }
            }


            // Fila: Apellidos + Teléfono
            ApellidosTelefono(
                apellidos = uiState.usuarioActual.apellidos,
                telefono = uiState.usuarioActual.telefono,
                onApellidosChange = { onChangeUsusario(Usuario(apellidos = it)) },
                onTelefonoChange = { onChangeUsusario(Usuario(telefono = it)) },
            )


            // Fila: Email + Fecha Nacimiento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
            ) {
                OutlinedTextField(
                    value = uiState.usuarioActual.email,
                    onValueChange = { onChangeUsusario(uiState.usuarioActual.copy(email = it)) },
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
                                    onChangeUsusario(uiState.usuarioActual.copy(fechaNacimiento = "$day/${month + 1}/$year"))
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
                            onClick = { onChangeUsusario(uiState.usuarioActual.copy(genero = "M")) }
                        )
                        Text("M", modifier = Modifier.padding(end = Dimens.paddingSmall))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = uiState.usuarioActual.genero == "F",
                            onClick = { onChangeUsusario(uiState.usuarioActual.copy(genero = "F")) }
                        )
                        Text("F", modifier = Modifier.padding(end = Dimens.paddingSmall))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = uiState.usuarioActual.genero == "Otro",
                            onClick = { onChangeUsusario(uiState.usuarioActual.copy(genero = "Otro")) }
                        )
                        Text("Otro")
                    }
                }
            }


            // Comentarios
            OutlinedTextField(
                value = uiState.usuarioActual.comentarios,
                onValueChange = { onChangeUsusario(uiState.usuarioActual.copy(comentarios = it)) },
                label = { Text("Comentarios") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.textAreaHeight),
                maxLines = 4
            )
            botonera(indiceActual = uiState.indiceActual,
                size = uiState.usuarios.size,
                isEmpty = uiState.usuarios.isEmpty(),)
        }
    }

}

@Composable
fun botonera(modifier: Modifier = Modifier,
             indiceActual : Int,
             size : Int,
             isEmpty : Boolean,
             onLimpiarFormulario: () -> Unit = {},
             onNavegarSiguiente: () -> Unit = {},
             onNavegarAnterior: () -> Unit = {},
             onGuardar: () -> Unit = {},
             onBorrar: () -> Unit = {},
             onActualizar: () -> Unit = {},
){


    // Botones de navegación
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onNavegarAnterior() },
            enabled = indiceActual > 0,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0099CC)),
            modifier = Modifier
                .weight(1f)
                .height(Dimens.buttonHeightSmall)
                .padding(end = Dimens.paddingExtraSmall)
        ) {
            Text("← Ant.", fontSize = Dimens.textSizeSmall)
        }

        Text(
            text = if (isEmpty) "0/0" else "${indiceActual + 1}/${size}",
            fontSize = Dimens.textSizeMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = Dimens.paddingSmall)
                .widthIn(min = 40.dp)
        )

        Button(
            onClick = { onNavegarSiguiente() },
            enabled = indiceActual < size - 1,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0099CC)),
            modifier = Modifier
                .weight(1f)
                .height(Dimens.buttonHeightSmall)
                .padding(start = Dimens.paddingExtraSmall)
        ) {
            Text("Sig. →", fontSize = Dimens.textSizeSmall)
        }
    }
    BotonesActtion(
        enableBorrar = !isEmpty,
        enableActualizar = !isEmpty,
        onGuardar = onGuardar,
        onLimpiarFormulario = onLimpiarFormulario,
        onBorrar = onBorrar,
        onActualizar = onActualizar,
    )



}


@Composable
private fun ApellidosTelefono(
    apellidos: String,
    telefono: String,
    onApellidosChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,

    ) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
    ) {
        OutlinedTextField(
            value = apellidos,
            onValueChange = onApellidosChange,
            label = { Text("Apellidos") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = onTelefonoChange,
            label = { Text("Teléfono") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserFormScreenPreview() {
    ComposeAppTheme {
        UserFormScreen(uiState = UserFormState(listOf(
            Usuario()
        ),1,Usuario(nombre="Juan")

        ))

    }
}
@Preview(showBackground = true,device = TABLET)
@Composable
fun UserFormScreenPreviewTablet() {
    ComposeAppTheme {
        UserFormScreen(uiState = UserFormState(listOf(
            Usuario()
        ),1,Usuario(nombre="Juan")

        ))

    }
}

