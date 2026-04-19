package com.example.medicitaapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    userName: String,
    onSubirFormula: () -> Unit,
    onVerTurno: () -> Unit
) {
    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = { HomeBottomBar() }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Buen día,",
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )

            Text(
                text = userName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )

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
                title = "Recordatorios",
                subtitle = "Revise avisos de entrega y próximos pasos.",
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = AppColors.Primary
                    )
                },
                onClick = { }
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
                contentAlignment = androidx.compose.ui.Alignment.Center
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