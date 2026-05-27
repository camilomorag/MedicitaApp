package com.example.medicitaapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicitaapp.data.FormulaRequestEntity
import com.example.medicitaapp.ui.components.NotificationBadge
import com.example.medicitaapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLocale

@Composable
fun HomeScreen(
    userName: String,
    onSubirFormula: () -> Unit,
    onVerTurno: () -> Unit,
    onVerNotificaciones: () -> Unit,
    onVerPerfil: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    var ultimaSolicitud by remember { mutableStateOf<FormulaRequestEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar la última solicitud del usuario
    LaunchedEffect(Unit) {
        scope.launch {
            val solicitudes = authViewModel.getUserRequests()
            ultimaSolicitud = solicitudes.firstOrNull()
            isLoading = false
        }
    }

    Scaffold(
        containerColor = Color(0xFFF4F6F8),
        bottomBar = {
            HomeBottomBar(
                onInicio = { /* Ya estamos en home */ },
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

            // Header: Saludo + campanita + perfil
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
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2430)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NotificationBadge(onClick = onVerNotificaciones)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEAF2FF))
                            .clickable { onVerPerfil() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Color(0xFF2F80ED),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta de Estado Actual (con última solicitud)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF2F80ED),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Estado actual",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D2433)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    } else if (ultimaSolicitud != null) {
                        // Mostrar última solicitud
                        SolicitudCard(solicitud = ultimaSolicitud!!)
                    } else {
                        // No hay solicitudes
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inbox,
                                contentDescription = null,
                                tint = Color(0xFFC5CAE0),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No hay solicitudes activas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6E7786)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Suba su primera fórmula médica",
                                fontSize = 13.sp,
                                color = Color(0xFF9AA2B0)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onSubirFormula,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED))
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Subir fórmula", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Acciones rápidas
            Text(
                text = "Acciones rápidas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2433),
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            HomeActionCard(
                title = "Subir fórmula médica",
                subtitle = "Tome una foto o cargue un archivo PDF",
                icon = Icons.Default.Description,
                iconBgColor = Color(0xFFEAF2FF),
                iconTint = Color(0xFF2F80ED),
                onClick = onSubirFormula
            )

            Spacer(modifier = Modifier.height(12.dp))

            HomeActionCard(
                title = "Mi turno digital",
                subtitle = "Consulte su número de turno y tiempo estimado",
                icon = Icons.Default.QrCode,
                iconBgColor = Color(0xFFE8F5E9),
                iconTint = Color(0xFF4CAF50),
                onClick = onVerTurno
            )

            Spacer(modifier = Modifier.height(12.dp))

            HomeActionCard(
                title = "Notificaciones",
                subtitle = "Revise el estado de su fórmula, turno y medicamento",
                icon = Icons.Outlined.Notifications,
                iconBgColor = Color(0xFFFFF3E0),
                iconTint = Color(0xFFFF9800),
                onClick = onVerNotificaciones
            )

            Spacer(modifier = Modifier.height(12.dp))

            HomeActionCard(
                title = "Ayuda y soporte",
                subtitle = "Encuentre ayuda o contacte a soporte",
                icon = Icons.Default.Help,
                iconBgColor = Color(0xFFF3E5F5),
                iconTint = Color(0xFF9C27B0),
                onClick = { }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun SolicitudCard(solicitud: FormulaRequestEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (solicitud.estado) {
                "aceptada" -> Color(0xFFE8F5E9)
                "rechazada" -> Color(0xFFFFEBEE)
                "aplazada" -> Color(0xFFFFF3E0)
                "lista" -> Color(0xFFE3F2FD)
                else -> Color(0xFFF5F5F5)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (solicitud.estado) {
                            "aceptada" -> Icons.Default.CheckCircle
                            "rechazada" -> Icons.Default.Cancel
                            "aplazada" -> Icons.Default.Schedule
                            "lista" -> Icons.Default.LocalPharmacy
                            else -> Icons.Default.Pending
                        },
                        contentDescription = null,
                        tint = when (solicitud.estado) {
                            "aceptada" -> Color(0xFF4CAF50)
                            "rechazada" -> Color(0xFFF44336)
                            "aplazada" -> Color(0xFFFF9800)
                            "lista" -> Color(0xFF2196F3)
                            else -> Color(0xFF9E9E9E)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (solicitud.estado) {
                            "aceptada" -> "✅ Aceptada"
                            "rechazada" -> "❌ Rechazada"
                            "aplazada" -> "⏰ Aplazada"
                            "lista" -> "🎉 Lista para entregar"
                            else -> "📄 Pendiente"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (solicitud.estado) {
                            "aceptada" -> Color(0xFF2E7D32)
                            "rechazada" -> Color(0xFFC62828)
                            "aplazada" -> Color(0xFFE65100)
                            "lista" -> Color(0xFF1565C0)
                            else -> Color(0xFF5D6B82)
                        }
                    )
                }

                if (solicitud.turno.isNotBlank()) {
                    Text(
                        text = "🎫 ${solicitud.turno}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D6BEB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = solicitud.medicamento,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2433)
            )

            if (solicitud.comentarioFarmaceuta.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "💬 ${solicitud.comentarioFarmaceuta}",
                    fontSize = 11.sp,
                    color = Color(0xFF6E7786)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📅 ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", LocalLocale.current.platformLocale).format(solicitud.fechaCreacion)}",
                fontSize = 10.sp,
                color = Color(0xFF9AA2B0)
            )
        }
    }
     }

@Composable
fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(iconBgColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D2433)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF6E7786)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFC5CAE0),
                modifier = Modifier.size(24.dp)
            )
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
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = onInicio,
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onTurnos,
            icon = { Icon(Icons.Default.QrCode, contentDescription = "Turnos") },
            label = { Text("Turnos", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onAvisos,
            icon = { Icon(Icons.Outlined.Notifications, contentDescription = "Avisos") },
            label = { Text("Avisos", fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onPerfil,
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil", fontSize = 11.sp) }
        )
    }
}