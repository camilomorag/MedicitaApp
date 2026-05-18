package com.example.medicitaapp.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun PharmacistMedicinesScreen(
    authViewModel: AuthViewModel,
    onOpenMedicine: (Int) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var medicines by remember { mutableStateOf<List<MedicineEntity>>(emptyList()) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            medicines = authViewModel.getAllMedicines()
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
            text = "Medicamentos",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D2433)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                scope.launch {
                    medicines = authViewModel.searchMedicines(query)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar medicamento") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        medicines.forEach { medicine ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenMedicine(medicine.id) },
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = medicine.producto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1D2433)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Expediente: ${medicine.expediente}",
                        color = Color(0xFF6E7786)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Estado: ${medicine.estadoRegistro}",
                        color = Color(0xFF6E7786)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (medicine.disponible) "Disponible" else "No disponible",
                        color = if (medicine.disponible) Color(0xFF1B7F3B) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}