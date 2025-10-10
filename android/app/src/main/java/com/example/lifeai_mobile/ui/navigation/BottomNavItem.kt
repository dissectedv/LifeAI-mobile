package com.example.lifeai_mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Inicio : BottomNavItem(
        route = "inicio",
        title = "Início",
        icon = Icons.Default.Home
    )

    object Saude : BottomNavItem(
        route = "saude",
        title = "Saúde",
        icon = Icons.Default.Favorite
    )

    object ChatIA : BottomNavItem(
        route = "chat_ia",
        title = "Chat IA",
        icon = Icons.AutoMirrored.Filled.Chat
    )

    object Usuario : BottomNavItem(
        route = "usuario",
        title = "Usuário",
        icon = Icons.Default.Person
    )
}