package com.example.medicitaapp.admin

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicitaapp.data.MedicineEntity
import com.example.medicitaapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random

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
    var showAdvancedFilters by remember { mutableStateOf(false) }

    // Filtros básicos
    var filterEstado by remember { mutableStateOf("") }
    var filterDisponible by remember { mutableStateOf<Boolean?>(null) }

    // Filtros avanzados nuevos
    var selectedYear by remember { mutableStateOf("") }
    var selectedVia by remember { mutableStateOf("") }
    var selectedForma by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf("") }
    var selectedCategoria by remember { mutableStateOf("") }

    var isGridView by remember { mutableStateOf(true) }

    // Extraer valores únicos para los filtros
    val years = remember(allMedicines) {
        allMedicines.mapNotNull { it.fechaExpedicion.take(4).takeIf { y -> y.isNotBlank() && y.toIntOrNull() != null } }
            .distinct().sortedDescending()
    }

    val vias = remember(allMedicines) {
        allMedicines.map { it.viaAdministracion }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val formas = remember(allMedicines) {
        allMedicines.map { it.formaFarmaceutica }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val letras = ('A'..'Z').map { it.toString() }

    val categorias = listOf(
        "💊 Analgésicos" to listOf("PARACETAMOL", "ACETAMINOFEN", "IBUPROFENO", "DIPIRONA"),
        "🦷 Antibióticos" to listOf("AZITROMICINA", "AMOXICILINA", "CEFTRIAXONA", "ENOXAPARINA"),
        "❤️ Cardiovasculares" to listOf("LOSARTAN", "ATORVASTATINA", "BISOPROLOL", "ENALAPRIL"),
        "🫁 Respiratorios" to listOf("SALBUTAMOL", "FLUTICASONA", "BRONCHO", "ALERCET"),
        "🧠 Sistema nervioso" to listOf("TRAMADOL", "CLONAZEPAM", "DEXAMETASONA", "MIDAZOLAM"),
        "💉 Hormonales" to listOf("INSULINA", "DEXAMETASONA", "HIDROCORTISONA", "BETAMETASONA")
    )

    LaunchedEffect(Unit) {
        scope.launch {
            allMedicines = authViewModel.getAllMedicines()
            filteredMedicines = allMedicines
            Log.d("PharmacistMedicines", "✅ Medicamentos cargados: ${allMedicines.size}")
        }
    }

    // Aplicar todos los filtros
    LaunchedEffect(searchQuery, filterEstado, filterDisponible, selectedYear, selectedVia, selectedForma, selectedLetter, selectedCategoria, allMedicines) {
        filteredMedicines = allMedicines.filter { medicine ->
            // Búsqueda por texto
            val matchesSearch = searchQuery.isBlank() ||
                    medicine.producto.contains(searchQuery, ignoreCase = true) ||
                    medicine.expediente.contains(searchQuery, ignoreCase = true) ||
                    medicine.registroSanitario.contains(searchQuery, ignoreCase = true) ||
                    medicine.principioActivo.contains(searchQuery, ignoreCase = true)

            // Filtros básicos
            val matchesEstado = filterEstado.isBlank() ||
                    medicine.estadoRegistro.equals(filterEstado, ignoreCase = true)
            val matchesDisponible = filterDisponible == null || medicine.disponible == filterDisponible

            // Filtro por año
            val matchesYear = selectedYear.isBlank() ||
                    medicine.fechaExpedicion.take(4) == selectedYear

            // Filtro por vía de administración
            val matchesVia = selectedVia.isBlank() ||
                    medicine.viaAdministracion.equals(selectedVia, ignoreCase = true)

            // Filtro por forma farmacéutica
            val matchesForma = selectedForma.isBlank() ||
                    medicine.formaFarmaceutica.equals(selectedForma, ignoreCase = true)

            // Filtro por letra inicial
            val matchesLetter = selectedLetter.isBlank() ||
                    medicine.producto.uppercase().startsWith(selectedLetter)

            // Filtro por categoría
            val matchesCategoria = if (selectedCategoria.isNotBlank()) {
                val categoriaPalabras = categorias.find { it.first == selectedCategoria }?.second ?: emptyList()
                categoriaPalabras.any { palabra ->
                    medicine.producto.contains(palabra, ignoreCase = true) ||
                            medicine.principioActivo.contains(palabra, ignoreCase = true)
                }
            } else true

            matchesSearch && matchesEstado && matchesDisponible &&
                    matchesYear && matchesVia && matchesForma && matchesLetter && matchesCategoria
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "💊 Mis Medicamentos",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D2433)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${filteredMedicines.size} de ${allMedicines.size} medicamentos",
            fontSize = 14.sp,
            color = Color(0xFF6E7786)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Barra de búsqueda
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar por nombre, principio activo...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2F80ED),
                    unfocusedBorderColor = Color(0xFFD9DEE7)
                ),
                singleLine = true
            )

            IconButton(
                onClick = { isGridView = !isGridView },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
            ) {
                Icon(
                    if (isGridView) Icons.Default.List else Icons.Default.GridView,
                    null,
                    tint = Color(0xFF2F80ED)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filtros rápidos
        ScrollableRow {
            FilterChip(
                selected = showAdvancedFilters,
                onClick = { showAdvancedFilters = !showAdvancedFilters },
                label = { Text("🔍 Avanzado") },
                leadingIcon = { Icon(Icons.Default.FilterAlt, null) }
            )

            FilterChip(
                selected = filterEstado == "Vigente",
                onClick = { filterEstado = if (filterEstado == "Vigente") "" else "Vigente" },
                label = { Text("✅ Vigente") }
            )

            FilterChip(
                selected = selectedYear.isNotBlank(),
                onClick = { if (selectedYear.isNotBlank()) selectedYear = "" },
                label = { Text("📅 Año: ${selectedYear.ifEmpty { "Todos" }}") }
            )
        }

        // Panel de filtros avanzados
        if (showAdvancedFilters) {
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Filtro por año
                    Text("📅 Año de expedición", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    ScrollableRow {
                        FilterChip(
                            selected = selectedYear == "",
                            onClick = { selectedYear = "" },
                            label = { Text("Todos") }
                        )
                        years.take(10).forEach { year ->
                            FilterChip(
                                selected = selectedYear == year,
                                onClick = { selectedYear = year },
                                label = { Text(year) }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Filtro por vía de administración
                    Text("💉 Vía de administración", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    ScrollableRow {
                        FilterChip(
                            selected = selectedVia == "",
                            onClick = { selectedVia = "" },
                            label = { Text("Todos") }
                        )
                        vias.forEach { via ->
                            FilterChip(
                                selected = selectedVia == via,
                                onClick = { selectedVia = via },
                                label = { Text(via.take(15)) }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Filtro por forma farmacéutica
                    Text("💊 Forma farmacéutica", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    ScrollableRow {
                        FilterChip(
                            selected = selectedForma == "",
                            onClick = { selectedForma = "" },
                            label = { Text("Todos") }
                        )
                        formas.take(8).forEach { forma ->
                            FilterChip(
                                selected = selectedForma == forma,
                                onClick = { selectedForma = forma },
                                label = { Text(forma.take(20)) }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Filtro por categoría terapéutica
                    Text("🏥 Categoría terapéutica", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    ScrollableRow {
                        FilterChip(
                            selected = selectedCategoria == "",
                            onClick = { selectedCategoria = "" },
                            label = { Text("Todos") }
                        )
                        categorias.forEach { categoria ->
                            FilterChip(
                                selected = selectedCategoria == categoria.first,
                                onClick = { selectedCategoria = categoria.first },
                                label = { Text(categoria.first) }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Filtro por letra inicial
                    Text("🔤 Letra inicial", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    ScrollableRow {
                        FilterChip(
                            selected = selectedLetter == "",
                            onClick = { selectedLetter = "" },
                            label = { Text("Todos") }
                        )
                        letras.forEach { letter ->
                            FilterChip(
                                selected = selectedLetter == letter,
                                onClick = { selectedLetter = letter },
                                label = { Text(letter) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón limpiar filtros
                    Button(
                        onClick = {
                            filterEstado = ""
                            filterDisponible = null
                            selectedYear = ""
                            selectedVia = ""
                            selectedForma = ""
                            selectedLetter = ""
                            selectedCategoria = ""
                            searchQuery = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                    ) {
                        Icon(Icons.Default.Clear, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Limpiar todos los filtros", color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vista de resultados
        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMedicines) { medicine ->
                    MedicineGridCard(
                        medicine = medicine,
                        onClick = { onOpenMedicine(medicine.id) }
                    )
                }

                if (filteredMedicines.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SearchOff, null, tint = Color(0xFF6E7786), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No se encontraron medicamentos", color = Color(0xFF6E7786))
                                Text("Prueba con otros filtros", fontSize = 12.sp, color = Color(0xFF6E7786))
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMedicines) { medicine ->
                    MedicineListItemCard(
                        medicine = medicine,
                        onClick = { onOpenMedicine(medicine.id) }
                    )
                }

                if (filteredMedicines.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SearchOff, null, tint = Color(0xFF6E7786), modifier = Modifier.size(48.dp))
                                Text("No se encontraron medicamentos", color = Color(0xFF6E7786))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2F80ED))
        ) {
            Icon(Icons.Default.ArrowBack, null, tint = Color(0xFF2F80ED))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver", color = Color(0xFF2F80ED), fontWeight = FontWeight.Bold)
        }
    }
}

// Componente para scroll horizontal
@Composable
fun ScrollableRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

// (Mantén las funciones MedicineGridCard, MedicineListItemCard, MedicineImageIcon igual que antes)
// ... copia las funciones de la versión anterior ...

// Tarjeta para vista GRID (tipo Drive)
@Composable
fun MedicineGridCard(
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
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MedicineImageIcon(
                productName = medicine.producto,
                estado = medicine.estadoRegistro
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = medicine.producto,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2433),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Exp: ${medicine.expediente.take(15)}",
                fontSize = 11.sp,
                color = Color(0xFF6E7786),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                    text = if (medicine.estadoRegistro == "Vigente") "✅ Vigente" else "❌ ${medicine.estadoRegistro.take(8)}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (medicine.estadoRegistro == "Vigente")
                        Color(0xFF1B7F3B) else Color(0xFFC62828)
                )
            }
        }
    }
}

// Tarjeta para vista LISTA
@Composable
fun MedicineListItemCard(
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
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MedicineImageIcon(
                productName = medicine.producto,
                estado = medicine.estadoRegistro,
                size = 48
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.producto,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D2433),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "📋 ${medicine.expediente}",
                    fontSize = 12.sp,
                    color = Color(0xFF6E7786)
                )

                Text(
                    text = "🏢 ${medicine.titular.take(30)}",
                    fontSize = 11.sp,
                    color = Color(0xFF6E7786),
                    maxLines = 1
                )
            }

            Icon(
                if (medicine.disponible) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (medicine.disponible) Color(0xFF1B7F3B) else Color(0xFFC62828),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Componente de imagen/icono para medicamento
@Composable
fun MedicineImageIcon(
    productName: String,
    estado: String,
    size: Int = 70
) {
    val colors = listOf(
        Color(0xFFE3F2FD),
        Color(0xFFE8F5E9),
        Color(0xFFFEF3C7),
        Color(0xFFFCE4EC),
        Color(0xFFF3E5F5),
        Color(0xFFFFE0B2)
    )

    val colorIndex = Random(productName.hashCode()).nextInt(colors.size)
    val backgroundColor = colors[colorIndex]

    val icon = when {
        productName.contains("IBUPROFEN", ignoreCase = true) -> Icons.Default.MedicalServices
        productName.contains("PARACETAMOL", ignoreCase = true) || productName.contains("ACETAMINOFEN", ignoreCase = true) -> Icons.Default.Medication
        productName.contains("ANTIBIOTIC", ignoreCase = true) -> Icons.Default.Coronavirus
        productName.contains("VITAMIN", ignoreCase = true) -> Icons.Default.Egg
        productName.contains("INSULIN", ignoreCase = true) -> Icons.Default.WaterDrop
        else -> Icons.Default.Medication
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2F80ED),
            modifier = Modifier.size((size * 0.5).dp)
        )
    }
}

// Componente para cuando no hay medicamentos
@Composable
fun EmptyMedicinesContent(isEmpty: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Default.Medication,
            contentDescription = null,
            tint = Color(0xFF6E7786),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isEmpty) "No hay medicamentos cargados" else "No se encontraron medicamentos",
            fontSize = 14.sp,
            color = Color(0xFF6E7786)
        )
        if (isEmpty) {
            Text(
                text = "Verifica la conexión a Internet",
                fontSize = 11.sp,
                color = Color(0xFFC62828)
            )
        }
    }
}