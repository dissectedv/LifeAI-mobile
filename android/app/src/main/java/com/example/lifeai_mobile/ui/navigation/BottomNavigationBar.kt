package com.example.lifeai_mobile.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Inicio,
        BottomNavItem.Saude,
        BottomNavItem.ChatIA,
        BottomNavItem.Usuario
    )

    val backgroundColor = Color(0xFF0F151B)
    val selectedColor = Color(0xFF00C89C)
    val unselectedColor = Color(0xFF6E868F)
    val chatIASelectedColor = Color(0xFF5E43F3)
    val chatIAUnselectedColor = Color(0xFF4C6A6C)

    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .height(80.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        containerColor = backgroundColor
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val isSelected = currentRoute == item.route

            val iconSize by animateDpAsState(
                targetValue = if (isSelected) 32.dp else 24.dp,
                animationSpec = tween(durationMillis = 200),
                label = "iconSize"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) selectedColor else unselectedColor,
                animationSpec = tween(durationMillis = 200),
                label = "textColor"
            )

            val iconTint = when {
                item.route == BottomNavItem.ChatIA.route && isSelected -> chatIASelectedColor
                item.route == BottomNavItem.ChatIA.route && !isSelected -> chatIAUnselectedColor
                isSelected -> selectedColor
                else -> unselectedColor
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) { saveState = true }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = { Text(text = item.title, fontSize = 12.sp, color = textColor) },
                icon = {
                    val iconModifier = if (item.route == BottomNavItem.Usuario.route && isSelected) {
                        Modifier
                            .size(iconSize)
                            .border(width = 2.dp, color = Color(0xFF007BFF), shape = CircleShape)
                            .clip(CircleShape)
                    } else {
                        Modifier.size(iconSize)
                    }

                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = iconModifier,
                        tint = iconTint
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}