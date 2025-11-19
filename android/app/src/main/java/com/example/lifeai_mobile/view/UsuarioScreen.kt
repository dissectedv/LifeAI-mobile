package com.example.lifeai_mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lifeai_mobile.viewmodel.AuthViewModel

@Composable
fun UsuarioScreen(
    mainNavController: NavController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val githubUrl = "https://github.com/dissectedv/LifeAI-mobile"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1A26))
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Conta e Preferências",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            UsuarioCard(
                title = "Seja Premium!",
                icon = Icons.Default.WorkspacePremium,
                titleColor = Color(0xFFFFC107),
                onClick = {
                    mainNavController.navigate("premium")
                }
            )

            UsuarioCard(
                title = "Personalizar Dados",
                icon = Icons.Default.Psychology,
                onClick = {
                    mainNavController.navigate("ai_personalizacao")
                }
            )

            UsuarioCard("Avalie-nos", Icons.Default.StarRate) {}

            UsuarioCard("Sobre o App", Icons.Default.Info) {
                showAboutDialog = true
            }

            UsuarioCard(
                title = "Sair da Conta",
                icon = Icons.AutoMirrored.Filled.Logout,
                titleColor = Color(0xFFFF8A80),
                showChevron = false
            ) {
                showLogoutDialog = true
            }
        }
    }

    if (showLogoutDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF12212F))
                    .padding(24.dp)
                    .width(280.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Sair da conta?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Tem certeza que deseja encerrar sua sessão?",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                showLogoutDialog = false
                                authViewModel.logout {
                                    mainNavController.navigate("welcome") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9A7))
                        ) {
                            Text("Confirmar", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showLogoutDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Color(0xFF1B2A3D),
            title = {
                Text("Sobre o LifeAI", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Versão 0.4.0 Alpha", color = Color.White.copy(alpha = 0.8f))
                    Text(
                        "LifeAI é um projeto focado em ajudar a monitorar sua saúde e bem-estar.",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    TextButton(onClick = { uriHandler.openUri(githubUrl) }) {
                        Text("Ver código no GitHub", color = Color(0xFF4A90E2), fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9A7))
                ) {
                    Text("Fechar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsuarioCard(
    title: String,
    icon: ImageVector,
    titleColor: Color = Color.White,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    val cardBackgroundColor = Color(0xFF1B2A3D)
    val gradientOverlay = Brush.verticalGradient(
        listOf(Color(0x224A90E2), Color.Transparent)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientOverlay)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = titleColor,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(18.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )

                Spacer(modifier = Modifier.weight(1f))

                if (showChevron) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Acessar",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}