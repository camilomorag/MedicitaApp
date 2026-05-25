package com.example.medicitaapp.admin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    var allMedicines by remember { mutableStateOf<List<MedicineEntity>>(emptyList()) }
    var filteredMedicines by remember { mutableStateOf<List<MedicineEntity>>(emptyList()) }

    // Estados de búsqueda y filtros
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var filterEstado by remember { mutableStateOf("") } // "", "Vigente", "Inactivo"
    var filterDisponible by remember { mutableStateOf<Boolean?>(null) } // null, true, false

    LaunchedEffect(Unit) {
        scope.launch {
            Log.d("PharmacistMedicines", "🔍 Cargando medicamentos...")
            allMedicines = authViewModel.getAllMedicines()
            filteredMedicines = allMedicines
            Log.d("PharmacistMedicines", "✅ Medicamentos obtenidos: ${allMedicines.size}")

            if (allMedicines.isEmpty()) {
                Log.e("PharmacistMedicines", "⚠️ No hay medicamentos en la base de datos")
            } else {
                Log.d("PharmacistMedicines", "📋 Primer medicamento: ${allMedicines[0].producto}")
            }
        }
    }

    // Aplicar filtros cuando cambian
    LaunchedEffect(searchQuery, filterEstado, filterDisponible, allMedicines) {
        filteredMedicines = allMedicines.filter { medicine ->
            val matchesSearch = searchQuery.isBlank() ||
                    medicine.producto.contains(searchQuery, ignoreCase = true) ||
                    medicine.expediente.contains(searchQuery, ignoreCase = true) ||
                    medicine.registroSanitario.contains(searchQuery, ignoreCase = true) ||
                    medicine.titular.contains(searchQuery, ignoreCase = true)

            val matchesEstado = filterEstado.isBlank() ||
                    medicine.estadoRegistro.equals(filterEstado, ignoreCase = true)

            val matchesDisponible = filterDisponible == null ||
                    medicine.disponible == filterDisponible

            matchesSearch && matchesEstado && matchesDisponible
        }
        Log.d("PharmacistMedicines", "🔎 Filtrado: ${filteredMedicines.size} de ${allMedicines.size} medicamentos")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Administrar Medicamentos",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D2433)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Busque y consulte los medicamentos registrados en el INVIMA",
            fontSize = 14.sp,
            color = Color(0xFF6E7786)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar por nombre, expediente o registro...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    TextButton(onClick = { searchQuery = "" }) {
                        Text("Limpiar")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2F80ED),
                unfocusedBorderColor = Color(0xFFD9DEE7)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de filtros
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilterChip(
                selected = showFilters,
                onClick = { showFilters = !showFilters },
                label = { Text("Filtros") },
                leadingIcon = { Icon(Icons.Default.FilterList, null) },
                modifier = Modifier
            )

            // Contador de resultados
            Text(
                text = "${filteredMedicines.size} medicamentos",
                fontSize = 12.sp,
                color = Color(0xFF6E7786),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        // Panel de filtros expandible
        if (showFilters) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Estado registro", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = filterEstado == "",
                            onClick = { filterEstado = "" },
                            label = { Text("Todos") }
                        )
                        FilterChip(
                            selected = filterEstado == "Vigente",
                            onClick = { filterEstado = "Vigente" },
                            label = { Text("Vigente") }
                        )
                        FilterChip(
                            selected = filterEstado == "Inactivo",
                            onClick = { filterEstado = "Inactivo" },
                            label = { Text("Inactivo") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Disponibilidad", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = filterDisponible == null,
                            onClick = { filterDisponible = null },
                            label = { Text("Todos") }
                        )
                        FilterChip(
                            selected = filterDisponible == true,
                            onClick = { filterDisponible = true },
                            label = { Text("Disponible") }
                        )
                        FilterChip(
                            selected = filterDisponible == false,
                            onClick = { filterDisponible = false },
                            label = { Text("No disponible") }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de medicamentos (LazyColumn como RecyclerView)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredMedicines) { medicine ->
                MedicineItemCard(
                    medicine = medicine,
                    onClick = {
                        Log.d("PharmacistMedicines", "🖱️ Abriendo medicamento: ${medicine.producto} (ID: ${medicine.id})")
                        onOpenMedicine(medicine.id)
                    }
                )
            }

            if (filteredMedicines.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (allMedicines.isEmpty()) "No hay medicamentos cargados" else "No se encontraron medicamentos",
                                color = Color(0xFF6E7786)
                            )
                            if (allMedicines.isEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Verifica que el archivo medicamentos.csv esté en la carpeta assets",
                                    fontSize = 12.sp,
                                    color = Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón volver
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2F80ED))
        ) {
            Text("Volver", color = Color(0xFF2F80ED), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MedicineItemCard(
    medicine: MedicineEntity,
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
                    text = medicine.producto,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D2433),
                    modifier = Modifier.weight(1f)
                )

                // Badge de estado
                Box(
                    modifier = Modifier
                        .background(
                            color = if (medicine.estadoRegistro == "Vigente")
                                Color(0xFFDFF5E3) else Color(0xFFFDE2E2),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = medicine.estadoRegistro,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (medicine.estadoRegistro == "Vigente")
                            Color(0xFF1B7F3B) else Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Expediente: ${medicine.expediente}",
                fontSize = 13.sp,
                color = Color(0xFF6E7786)
            )

            Text(
                text = "Registro: ${medicine.registroSanitario}",
                fontSize = 13.sp,
                color = Color(0xFF6E7786)
            )

            Text(
                text = "Titular: ${medicine.titular}",
                fontSize = 13.sp,
                color = Color(0xFF6E7786)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (medicine.disponible) "✓ Disponible" else "✗ No disponible",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (medicine.disponible) Color(0xFF1B7F3B) else Color(0xFFC62828)
                )

                Text(
                    text = "▶ Ver detalle",
                    fontSize = 12.sp,
                    color = Color(0xFF2F80ED)
                )
            }
        }
    }
}