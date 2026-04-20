package com.example.medicitaapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    userName: String,
    onSubirFormula: () -> Unit,
    onVerTurno: () -> Unit,
    onVerNotificaciones: () -> Unit,
    onVerPerfil: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFFF4F6F8),
        bottomBar = {
            HomeBottomBar(
                onInicio = { /* Ya estamos en home, no hace nada */ },
                onTurnos = onVerTurno,
                onAvisos = onVerNotificaciones,
                onPerfil = onVerPerfil
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F6F8))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Fila con saludo y foto de perfil
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Buen día,",
                        fontSize = 14.sp,
                        color = Color(0xFF7B8494)
                    )

                    Text(
                        text = userName,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2430)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAF2FF))
                        .clickable { onVerPerfil() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = Color(0xFF2F80ED),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            AppCard {
                SectionTitle("Estado actual")

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "No hay solicitudes activas por el momento.",
                    fontSize = 21.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                BodyText("Suba su fórmula médica para iniciar el proceso.")

                Spacer(modifier = Modifier.height(14.dp))

                PrimaryButton(
                    text = "Subir fórmula",
                    onClick = onSubirFormula
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HomeActionCard(
                title = "Subir fórmula médica",
                subtitle = "Tome una foto o cargue un archivo PDF.",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        tint = AppColors.Primary
                    )
                },
                onClick = onSubirFormula
            )

            Spacer(modifier = Modifier.height(12.dp))

            HomeActionCard(
                title = "Mi turno digital",
                subtitle = "Consulte su número de turno y tiempo estimado.",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.QrCode,
                        contentDescription = null,
                        tint = AppColors.Primary
                    )
                },
                onClick = onVerTurno
            )

            Spacer(modifier = Modifier.height(12.dp))

            HomeActionCard(
                title = "Notificaciones",
                subtitle = "Revise el estado de su fórmula, turno y medicamento.",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = AppColors.Primary
                    )
                },
                onClick = onVerNotificaciones
            )

            Spacer(modifier = Modifier.height(12.dp))

            HomeActionCard(
                title = "Ayuda",
                subtitle = "Encuentre soporte para usar la aplicación.",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Help,
                        contentDescription = null,
                        tint = AppColors.Primary
                    )
                },
                onClick = { }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    AppCard(onClick = onClick) {
        Row {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = AppColors.SoftBlue,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun HomeBottomBar(
    onInicio: () -> Unit,
    onTurnos: () -> Unit,
    onAvisos: () -> Unit,
    onPerfil: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeBottomItem("Inicio", onInicio)
        HomeBottomItem("Turnos", onTurnos)
        HomeBottomItem("Avisos", onAvisos)
        HomeBottomItem("Perfil", onPerfil)
    }
}

@Composable
fun HomeBottomItem(
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color(0xFF6E7786)
        )
    }
}