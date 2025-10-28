package com.example.lifeai_mobile.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisclaimerScreen(navController: NavController) {
    var isChecked by remember { mutableStateOf(false) }

    val accentColor = Color(0xFF58C4D3)
    val titleColor = Color(0xFFFACC15)
    val textColorPrimary = Color.White
    val textColorSecondary = Color(0xFF8B949E)

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 28.dp)
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Aviso Importante",
                color = titleColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Uso exclusivamente educacional. Não substitui orientação médica profissional.",
                color = textColorPrimary,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Ao continuar, você declara que:",
                color = textColorPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "• Você é o único responsável pela sua prática.",
                    color = textColorPrimary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
                Text(
                    text = "• Os desenvolvedores não se responsabilizam por acidentes ou lesões.",
                    color = textColorPrimary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Para sua segurança, consulte sempre um profissional de saúde antes de iniciar qualquer atividade.",
                color = textColorSecondary,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(80.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isChecked = !isChecked }
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = accentColor,
                        uncheckedColor = textColorSecondary,
                        checkmarkColor = Color.Black
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Li e entendi as condições acima",
                    color = textColorPrimary,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = {
                        navController.navigate("onboarding") {
                            popUpTo("disclaimer") { inclusive = true }
                        }
                    },
                    enabled = isChecked,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.5.dp, if (isChecked) titleColor else textColorSecondary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isChecked) titleColor else textColorSecondary,
                        disabledContentColor = textColorSecondary
                    )
                ) {
                    Text(
                        "Aceitar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}