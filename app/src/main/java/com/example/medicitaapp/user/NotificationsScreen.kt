package com.example.medicitaapp.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.medicitaapp.data.FormulaRequestEntity
import com.example.medicitaapp.data.NotificationEntity
import com.example.medicitaapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import com.example.medicitaapp.ui.components.StatusBadge

@Composable
fun NotificationsScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var notifications by remember { mutableStateOf<List<NotificationEntity>>(emptyList()) }
    var requests by remember { mutableStateOf<List<FormulaRequestEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            notifications = authViewModel.getUserNotifications()
            requests = authViewModel.getUserRequests()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FB)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FB))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Notificaciones",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2433)
            )

            Spacer(modifier = Modifier.height(16.dp))

            notifications.forEach { notification ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF2F80ED)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = notification.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1D2433)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = notification.message,
                            color = Color(0xFF6E7786),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mis solicitudes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2433)
            )

            Spacer(modifier = Modifier.height(12.dp))

            requests.forEach { request ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Archivo: ${request.formulaType}",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D2433)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        StatusBadge(request.estado)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Turno: ${if (request.turno.isBlank()) "Pendiente" else request.turno}",
                            color = Color(0xFF6E7786)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Ubicación: ${if (request.ubicacion.isBlank()) "Pendiente" else request.ubicacion}",
                            color = Color(0xFF6E7786)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Medicamento: ${request.medicamento}",
                            color = Color(0xFF6E7786)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Color(0xFF2F80ED)
                )
            ) {
                Text(
                    text = "Volver",
                    color = Color(0xFF2F80ED),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}