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
            LoginScreen { navController.navigate(User) }
        }
        composable<User> {
            UserFormScreenViewModel()
        }

        composable<Home> {
            HomeScreen { name -> navController.navigate(Detail(name = name)) }
        }

        composable<Detail> { backStackEntry ->
            val detail: Detail = backStackEntry.toRoute()
            DetailScreen(name = detail.name,
                navigateBack = { navController.popBackStack() },)
                //navigateBack = { navController.navigateUp() },)

        }


    }
}