package com.example.composeapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aristidevs.navigationguide.core.navigation.NavigationWrapper
import com.example.composeapp.ui.screens.user.UserFormScreenViewModel
import com.example.composeapp.ui.theme.ComposeAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeAppTheme {

                NavigationWrapper()

            }
        }
    }
}