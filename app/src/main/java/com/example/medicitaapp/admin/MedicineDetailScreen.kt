package com.example.medicitaapp.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicitaapp.data.MedicineEntity
import com.example.medicitaapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun MedicineDetailScreen(
    authViewModel: AuthViewModel,
    medicineId: Int,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var medicine by remember { mutableStateOf<MedicineEntity?>(null) }

    LaunchedEffect(medicineId) {
        scope.launch {
            medicine = authViewModel.getMedicineById(medicineId)
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
            text = "Detalle del medicamento",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D2433)
        )

        Spacer(modifier = Modifier.height(16.dp))

        medicine?.let {
            DetailCard("Producto", it.producto)
            DetailCard("Expediente", it.expediente)
            DetailCard("Titular", it.titular)
            DetailCard("Registro sanitario", it.registroSanitario)
            DetailCard("Fecha expedición", it.fechaExpedicion)
            DetailCard("Fecha vencimiento", it.fechaVencimiento)
            DetailCard("Estado registro", it.estadoRegistro)
            DetailCard("Descripción", it.descripcion)
            DetailCard("Estado comercial", it.estadoComercial)
            DetailCard("Unidad", it.unidad)
            DetailCard("Disponibilidad", if (it.disponible) "Disponible" else "No disponible")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun DetailCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color(0xFF6E7786)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2433)
            )
        }
    }
}