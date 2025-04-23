package com.github.jetbrains.rssreader.androidApp.screens

import androidx.compose.foundation.background
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ScaffoldCliente(
    navController: NavHostController,
    content: @Composable (paddingValues: androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomBarCliente(navController = navController)
        },
        backgroundColor = Color.Transparent
    ) { paddingValues ->
        content(paddingValues)
    }
}