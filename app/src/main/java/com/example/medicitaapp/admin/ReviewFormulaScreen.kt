package com.example.medicitaapp.admin

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medicitaapp.data.FormulaRequestEntity
import com.example.medicitaapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewFormulaScreen(
    authViewModel: AuthViewModel,
    requestId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var request by remember { mutableStateOf<FormulaRequestEntity?>(null) }
    var comentario by remember { mutableStateOf("") }
    var turno by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var showComentarioDialog by remember { mutableStateOf(false) }
    var accionSeleccionada by remember { mutableStateOf("") }

    LaunchedEffect(requestId) {
        scope.launch {
            request = authViewModel.getRequestById(requestId)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FB),
        topBar = {
            TopAppBar(
                title = { Text("Revision de formula", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            request?.let { formula ->
                // Tarjeta de informacion del paciente
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Paciente", fontSize = 14.sp, color = Color(0xFF6E7786))
                        Text(formula.userNombre, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D2433))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Documento: ${formula.userDocumento}", fontSize = 14.sp, color = Color(0xFF4E596B))
                        //Text("Telefono: ${formula.userTelefono}", fontSize = 14.sp, color = Color(0xFF4E596B))
                        Text("Medicamento: ${formula.medicamento}", fontSize = 14.sp, color = Color(0xFF4E596B))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Resultado de validacion IA
                if (formula.mensajeValidacion.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (formula.validacionIA) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (formula.validacionIA) Icons.Default.CheckCircle else Icons.Default.Error,
                                    null,
                                    tint = if (formula.validacionIA) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (formula.validacionIA) "Validacion IA: Exitosa" else "Validacion IA: Fallida",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(formula.mensajeValidacion, fontSize = 13.sp)
                            if (formula.observacionesValidacion.isNotBlank()) {
                                Text("Observaciones: ${formula.observacionesValidacion}", fontSize = 12.sp, color = Color(0xFF6E7786))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Boton para abrir archivo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Description, null, tint = Color(0xFF2F80ED), modifier = Modifier.size(48.dp))
                        Text("Vista de formula medica", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Puede abrir el archivo enviado por el usuario para revisarlo.", fontSize = 13.sp, color = Color(0xFF6E7786))
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                try {
                                    val mimeType = if (formula.formulaType == "pdf") "application/pdf" else "image/*"
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(formula.formulaUri), mimeType)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No se pudo abrir el archivo", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED))
                        ) {
                            Text("Abrir ${if (formula.formulaType == "pdf") "PDF" else "imagen"}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acciones
                Text("Acciones", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                // Aprobar
                Button(
                    onClick = {
                        accionSeleccionada = "aceptada"
                        showComentarioDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aprobar formula", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Asignar turno
                Button(
                    onClick = {
                        accionSeleccionada = "turno"
                        showComentarioDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Icon(Icons.Default.QrCode, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Asignar turno", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Marcar listo
                Button(
                    onClick = {
                        accionSeleccionada = "lista"
                        showComentarioDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                ) {
                    Icon(Icons.Default.LocalPharmacy, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Marcar medicamento listo", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rechazar
                Button(
                    onClick = {
                        accionSeleccionada = "rechazada"
                        showComentarioDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFFF44336))
                ) {
                    Text("Rechazar formula", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Aplazar
                Button(
                    onClick = {
                        accionSeleccionada = "aplazada"
                        showComentarioDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Aplazar solicitud", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Dialog para comentario
    if (showComentarioDialog) {
        AlertDialog(
            onDismissRequest = { showComentarioDialog = false },
            title = {
                Text(
                    when (accionSeleccionada) {
                        "aceptada" -> "Aprobar formula"
                        "rechazada" -> "Rechazar formula"
                        "aplazada" -> "Aplazar solicitud"
                        "turno" -> "Asignar turno"
                        "lista" -> "Marcar como listo"
                        else -> "Comentario"
                    }
                )
            },
            text = {
                Column {
                    if (accionSeleccionada == "turno") {
                        OutlinedTextField(
                            value = turno,
                            onValueChange = { turno = it },
                            label = { Text("Numero de turno") },
                            placeholder = { Text("Ej: A-42") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ubicacion,
                            onValueChange = { ubicacion = it },
                            label = { Text("Ubicacion") },
                            placeholder = { Text("Ej: Consultorio 101") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = comentario,
                            onValueChange = { comentario = it },
                            label = { Text("Motivo / Observacion") },
                            placeholder = { Text("Ingrese el motivo de su decision...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            when (accionSeleccionada) {
                                "aceptada" -> {
                                    authViewModel.updateRequestAsPharmacist(
                                        requestId = requestId,
                                        estado = "aceptada",
                                        comentario = comentario,
                                        turno = turno,
                                        ubicacion = ubicacion
                                    )
                                    Toast.makeText(context, "Formula aceptada", Toast.LENGTH_SHORT).show()
                                }
                                "rechazada" -> {
                                    authViewModel.updateRequestAsPharmacist(
                                        requestId = requestId,
                                        estado = "rechazada",
                                        comentario = comentario
                                    )
                                    Toast.makeText(context, "Formula rechazada", Toast.LENGTH_SHORT).show()
                                }
                                "aplazada" -> {
                                    authViewModel.updateRequestAsPharmacist(
                                        requestId = requestId,
                                        estado = "aplazada",
                                        comentario = comentario
                                    )
                                    Toast.makeText(context, "Solicitud aplazada", Toast.LENGTH_SHORT).show()
                                }
                                "turno" -> {
                                    authViewModel.updateRequestAsPharmacist(
                                        requestId = requestId,
                                        estado = "aceptada",
                                        turno = turno,
                                        ubicacion = ubicacion
                                    )
                                    Toast.makeText(context, "Turno $turno asignado", Toast.LENGTH_SHORT).show()
                                }
                                "lista" -> {
                                    authViewModel.updateRequestAsPharmacist(
                                        requestId = requestId,
                                        estado = "lista",
                                        ubicacion = ubicacion
                                    )
                                    Toast.makeText(context, "Medicamento marcado como listo", Toast.LENGTH_SHORT).show()
                                }
                            }
                            onBack()
                        }
                        showComentarioDialog = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showComentarioDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}