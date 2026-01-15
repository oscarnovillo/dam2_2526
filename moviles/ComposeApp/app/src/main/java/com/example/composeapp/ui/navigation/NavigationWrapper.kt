package com.aristidevs.navigationguide.core.navigation

import androidx.compose.runtime.Composable
import com.example.composeapp.ui.navigation.MainScaffold

/**
 * NavigationWrapper - Punto de entrada principal de la navegación
 * Ahora usa MainScaffold que incluye el BottomNavigationBar
 */
@Composable
fun NavigationWrapper() {
    MainScaffold()
}