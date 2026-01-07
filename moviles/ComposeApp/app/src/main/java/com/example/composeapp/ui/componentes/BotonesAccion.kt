package com.example.composeapp.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.composeapp.ui.theme.Dimens

@Composable
fun BotonesActtion(modifier: Modifier = Modifier,
                   enableBorrar : Boolean,
                   enableActualizar : Boolean,
                   onGuardar: () -> Unit = {},
                   onLimpiarFormulario: () -> Unit = {},
                   onBorrar: () -> Unit = {},
                   onActualizar: () -> Unit = {},     ) {
    // Botones de acción
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
    ) {
        // Limpiar
        Button(
            onClick = { onLimpiarFormulario() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            modifier = Modifier
                .weight(1f)
                .height(Dimens.buttonHeightMedium)
        ) {
            Text("Limpiar", fontSize = Dimens.textSizeSmall)
        }

        // Actualizar
        Button(
            onClick = { onActualizar() },
            enabled = enableActualizar,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8800)),
            modifier = Modifier
                .weight(1f)
                .height(Dimens.buttonHeightMedium)
        ) {
            Text("Actualizar", fontSize = Dimens.textSizeSmall)
        }

        // Borrar
        Button(
            onClick = { onBorrar() },
            enabled = enableBorrar,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000)),
            modifier = Modifier
                .weight(1f)
                .height(Dimens.buttonHeightMedium)
        ) {
            Text("Borrar", fontSize = Dimens.textSizeSmall)
        }

        // Guardar
        Button(
            onClick = { onGuardar() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF669900)),
            modifier = Modifier
                .weight(1f)
                .height(Dimens.buttonHeightMedium)
        ) {
            Text("Guardar", fontSize = Dimens.textSizeSmall)
        }
    }


}
