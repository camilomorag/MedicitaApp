package com.example.medicitaapp.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onManageMedicines: () -> Unit,  // Nuevo parámetro
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var requests by remember { mutableStateOf<List<FormulaRequestEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            requests = authViewModel.getAllRequests()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
            .verticalScroll(rememberScrollState())
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
            color = Color(0xFF6E7786),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        requests.forEach { request ->
            PharmacistRequestCard(
                patientName = request.userNombre,
                document = request.userDocumento,
                formulaType = request.formulaType,
                status = request.estado,
                onClick = { onOpenRequest(request.id) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ✅ NUEVO BOTÓN: Administrar medicamentos
        Button(
            onClick = onManageMedicines,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
        ) {
            Icon(
                imageVector = Icons.Default.LocalPharmacy,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Administrar medicamentos",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("Cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Color(0xFF2F80ED)
            )
        ) {
            Text("Volver", color = Color(0xFF2F80ED), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PharmacistRequestCard(
    patientName: String,
    document: String,
    formulaType: String,
    status: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = patientName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2433)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Documento: $document",
                fontSize = 13.sp,
                color = Color(0xFF6E7786)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tipo de archivo: $formulaType",
                fontSize = 13.sp,
                color = Color(0xFF6E7786)
            )

            Spacer(modifier = Modifier.height(10.dp))

            StatusBadge(status)
        }
    }
}