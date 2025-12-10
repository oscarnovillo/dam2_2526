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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composeapp.ui.theme.ComposeAppTheme
import java.util.*

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
fun UserFormScreen(modifier: Modifier = Modifier) {
    var usuarios by remember { mutableStateOf(mutableListOf<Usuario>()) }
    var usuarioActual by remember { mutableStateOf(Usuario()) }
    var indiceActual by remember { mutableStateOf(-1) }

    // Estados para los campos del formulario
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var generoSeleccionado by remember { mutableStateOf("") }
    var comentarios by remember { mutableStateOf("") }
    var tieneTV by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Función para cargar un usuario en el formulario
    fun cargarUsuario(usuario: Usuario) {
        nombre = usuario.nombre
        apellidos = usuario.apellidos
        telefono = usuario.telefono
        email = usuario.email
        fechaNacimiento = usuario.fechaNacimiento
        generoSeleccionado = usuario.genero
        comentarios = usuario.comentarios
        tieneTV = usuario.tieneTV
    }

    // Función para limpiar el formulario
    fun limpiarFormulario() {
        nombre = ""
        apellidos = ""
        telefono = ""
        email = ""
        fechaNacimiento = ""
        generoSeleccionado = ""
        comentarios = ""
        tieneTV = false
        indiceActual = -1
    }

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
                value = nombre,
                onValueChange = { nombre = it },
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
                    checked = tieneTV,
                    onCheckedChange = { tieneTV = it }
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
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
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
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = fechaNacimiento,
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
                                fechaNacimiento = "$day/${month + 1}/$year"
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
                        selected = generoSeleccionado == "M",
                        onClick = { generoSeleccionado = "M" }
                    )
                    Text("M", modifier = Modifier.padding(end = 8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = generoSeleccionado == "F",
                        onClick = { generoSeleccionado = "F" }
                    )
                    Text("F", modifier = Modifier.padding(end = 8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = generoSeleccionado == "Otro",
                        onClick = { generoSeleccionado = "Otro" }
                    )
                    Text("Otro")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Comentarios
        OutlinedTextField(
            value = comentarios,
            onValueChange = { comentarios = it },
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
                onClick = {
                    if (indiceActual > 0) {
                        indiceActual--
                        cargarUsuario(usuarios[indiceActual])
                    }
                },
                enabled = indiceActual > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0099CC)),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .padding(end = 4.dp)
            ) {
                Text("← Ant.", fontSize = 12.sp)
            }

            Text(
                text = if (usuarios.isEmpty()) "0/0" else "${indiceActual + 1}/${usuarios.size}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .widthIn(min = 40.dp)
            )

            Button(
                onClick = {
                    if (indiceActual < usuarios.size - 1) {
                        indiceActual++
                        cargarUsuario(usuarios[indiceActual])
                    }
                },
                enabled = indiceActual < usuarios.size - 1,
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
                onClick = { limpiarFormulario() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Limpiar", fontSize = 12.sp)
            }

            // Actualizar
            Button(
                onClick = {
                    if (indiceActual >= 0 && indiceActual < usuarios.size) {
                        usuarios[indiceActual] = Usuario(
                            nombre, apellidos, telefono, email,
                            fechaNacimiento, generoSeleccionado, comentarios, tieneTV
                        )
                    }
                },
                enabled = indiceActual >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8800)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Actualizar", fontSize = 12.sp)
            }

            // Borrar
            Button(
                onClick = {
                    if (indiceActual >= 0 && indiceActual < usuarios.size) {
                        usuarios.removeAt(indiceActual)
                        if (usuarios.isEmpty()) {
                            limpiarFormulario()
                        } else {
                            if (indiceActual >= usuarios.size) {
                                indiceActual = usuarios.size - 1
                            }
                            if (indiceActual >= 0) {
                                cargarUsuario(usuarios[indiceActual])
                            }
                        }
                    }
                },
                enabled = indiceActual >= 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Borrar", fontSize = 12.sp)
            }

            // Guardar
            Button(
                onClick = {
                    val nuevoUsuario = Usuario(
                        nombre, apellidos, telefono, email,
                        fechaNacimiento, generoSeleccionado, comentarios, tieneTV
                    )
                    usuarios.add(nuevoUsuario)
                    indiceActual = usuarios.size - 1
                    limpiarFormulario()
                },
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