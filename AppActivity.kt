package com.github.jetbrains.rssreader.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.github.jetbrains.rssreader.androidApp.ui.theme.PeluqueriaAppTheme

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PeluqueriaAppTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}