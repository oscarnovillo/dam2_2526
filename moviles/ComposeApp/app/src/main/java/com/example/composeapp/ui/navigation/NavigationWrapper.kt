package com.aristidevs.navigationguide.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aristidevs.navigationguide.DetailScreen
import com.aristidevs.navigationguide.HomeScreen
import com.example.composeapp.ui.screens.LoginScreen
import com.example.composeapp.ui.screens.user.UserFormScreenViewModel


@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen { navController.navigate(Home) }
        }
        composable<User> {
            UserFormScreenViewModel()
        }

        composable<Home> {
            HomeScreen { name -> navController.navigate(Detail(name = name)) }
        }

        composable<Detail> { backStackEntry ->
            val detail: Detail = backStackEntry.toRoute()
            DetailScreen(
                name = detail.name,
                navigateBack = {
//                    navController.popBackStack()
//                    navController.popBackStack(route = Login,inclusive = true)
//                    navController.navigateUp()
                    // Opción 1: Limpiar toda la pila y volver a Login
                    navController.navigate(Login) {
                        // Elimina todas las pantallas hasta Login (inclusive)
                        popUpTo(Login) {
                            inclusive = true
                        }
                        // Evita crear múltiples instancias de Login
                        launchSingleTop = true
                        // Restaura el estado si vuelves a la misma pantalla
                        restoreState = true
                    }

                    /* Opción 2: Limpiar hasta Home (sin eliminar Home)
                    navController.navigate(Login) {
                        popUpTo(Home) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                    */

                    /* Opción 3: Limpiar toda la pila (empezar desde cero)
                    navController.navigate(Login) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                    */
                }
            )
        }


    }
}