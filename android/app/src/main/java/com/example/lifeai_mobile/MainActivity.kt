package com.example.lifeai_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lifeai_mobile.ui.theme.LifeAImobileTheme
import com.example.lifeai_mobile.viewmodel.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeAImobileTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "createAccount"){
                    composable("createAccount") {RegisterScreen(navController)}
                }
            }
        }
    }
}
