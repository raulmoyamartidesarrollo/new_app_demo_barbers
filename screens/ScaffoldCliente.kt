package com.github.jetbrains.rssreader.androidApp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController

@Composable
fun ScaffoldCliente(
    navController: NavHostController,
    content: @Composable (paddingValues: androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.navigationBarsPadding()) {
                BottomBarCliente(navController = navController)
            }
        },
        backgroundColor = Color.Transparent
    ) { paddingValues ->
        content(paddingValues)
    }
}