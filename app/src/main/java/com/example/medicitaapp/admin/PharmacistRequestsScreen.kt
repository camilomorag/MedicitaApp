package com.example.medicitaapp.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicitaapp.data.FormulaRequestEntity
import com.example.medicitaapp.ui.components.StatusBadge
import com.example.medicitaapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun PharmacistRequestsScreen(
    authViewModel: AuthViewModel,
    onOpenRequest: (Int) -> Unit,
    onManageMedicines: () -> Unit,  // ✅ Parámetro agregado
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var requests by remember { mutableStateOf<List<FormulaRequestEntity>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("Pendientes", "Aceptadas", "Rechazadas", "Aplazadas")

    LaunchedEffect(Unit) {
        scope.launch {
            requests = authViewModel.getAllRequests()
        }
    }

    val filteredRequests = when (selectedTab) {
        0 -> requests.filter { it.estado == "pendiente" || it.estado == "validacion_fallida" }
        1 -> requests.filter { it.estado == "aceptada" }
        2 -> requests.filter { it.estado == "rechazada" }
        3 -> requests.filter { it.estado == "aplazada" }
        else -> emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
            .padding(16.dp)
    ) {
        Text(
            text = "Solicitudes de fórmulas",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D2433)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Revise y gestione las fórmulas subidas por los usuarios.",
            fontSize = 14.sp,
            color = Color(0xFF6E7786)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pestañas
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${filteredRequests.size} solicitudes",
            fontSize = 13.sp,
            color = Color(0xFF6E7786)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredRequests) { request ->
                PharmacistRequestCard(
                    request = request,
                    onClick = { onOpenRequest(request.id) }
                )
            }

            if (filteredRequests.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay solicitudes", color = Color(0xFF6E7786))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Botón para administrar medicamentos
        Button(
            onClick = onManageMedicines,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
        ) {
            Icon(Icons.Default.LocalPharmacy, null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Administrar medicamentos", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2F80ED))
            ) {
                Text("Volver", color = Color(0xFF2F80ED), fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onLogout,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PharmacistRequestCard(
    request: FormulaRequestEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = request.userNombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D2433)
                )
                StatusBadge(request.estado)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Documento: ${request.userDocumento}",
                fontSize = 13.sp,
                color = Color(0xFF6E7786)
            )

            Text(
                text = "Medicamento: ${request.medicamento}",
                fontSize = 13.sp,
                color = Color(0xFF6E7786)
            )

            // Mostrar resultado de validación IA
            if (request.validacionIA) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF1B7F3B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("✅ Validación IA exitosa", fontSize = 12.sp, color = Color(0xFF1B7F3B))
                }
            } else if (request.mensajeValidacion.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("⚠️ ${request.mensajeValidacion.take(50)}", fontSize = 11.sp, color = Color(0xFFFF9800))
                }
            }

            if (request.comentarioFarmaceuta.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "💬 ${request.comentarioFarmaceuta}",
                        fontSize = 12.sp,
                        color = Color(0xFF4E596B),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}