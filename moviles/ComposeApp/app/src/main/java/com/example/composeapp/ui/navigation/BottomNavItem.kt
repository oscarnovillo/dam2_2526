package com.example.composeapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.aristidevs.navigationguide.core.navigation.Home
import com.aristidevs.navigationguide.core.navigation.Login
import com.aristidevs.navigationguide.core.navigation.User

/**
 * Sealed class que representa los items del BottomNavigationBar
 */
sealed class BottomNavItem(
    val route: Any,
    val title: String,
    val icon: ImageVector
) {
    data object LoginTab : BottomNavItem(
        route = Login,
        title = "Login",
        icon = Icons.AutoMirrored.Filled.Login
    )

    data object UserTab : BottomNavItem(
        route = User,
        title = "Usuario",
        icon = Icons.Default.Person
    )

    data object HomeTab : BottomNavItem(
        route = Home,
        title = "Home",
        icon = Icons.Default.Home
    )

    companion object {
        val items = listOf(LoginTab, UserTab, HomeTab)
    }
}

