package com.example.medicitaapp.admin

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.medicitaapp.data.FormulaRequestEntity
import com.example.medicitaapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ReviewFormulaScreen(
    authViewModel: AuthViewModel,
    requestId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var request by remember { mutableStateOf<FormulaRequestEntity?>(null) }

    LaunchedEffect(requestId) {
        scope.launch {
            request = authViewModel.getRequestById(requestId)
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
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Revisión de fórmula",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2433)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Valide la información del paciente y autorice la entrega del medicamento.",
                fontSize = 14.sp,
                color = Color(0xFF6E7786),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            request?.let { formula ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Paciente", fontSize = 14.sp, color = Color(0xFF6E7786))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formula.userNombre,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1D2433)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Documento: ${formula.userDocumento}", fontSize = 14.sp, color = Color(0xFF4E596B))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Medicamento solicitado: ${formula.medicamento}", fontSize = 14.sp, color = Color(0xFF4E596B))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Archivo: ${formula.formulaType}", fontSize = 14.sp, color = Color(0xFF4E596B))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("URI: ${formula.formulaUri}", fontSize = 12.sp, color = Color(0xFF6E7786))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF2F80ED)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Vista de fórmula médica",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D2433)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Aquí puedes usar la URI guardada para abrir o mostrar el archivo enviado.",
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF6E7786)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .border(1.5.dp, Color(0xFFDDE3EC), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBFE)),
                            shape = RoundedCornerShape(16.dp)
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            authViewModel.updateRequestAsPharmacist(
                                requestId = formula.id,
                                estado = "aceptada"
                            )
                            Toast.makeText(context, "Fórmula aprobada", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.TaskAlt, null, tint = Color.White)
                    Text(" Aprobar fórmula", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        scope.launch {
                            authViewModel.updateRequestAsPharmacist(
                                requestId = formula.id,
                                estado = "aceptada",
                                turno = "A-42",
                                ubicacion = "Central Pharmacy - Piso 1"
                            )
                            Toast.makeText(context, "Turno A-42 asignado", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED))
                ) {
                    Icon(Icons.Default.QrCode, null, tint = Color.White)
                    Text(" Asignar turno", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        scope.launch {
                            authViewModel.updateRequestAsPharmacist(
                                requestId = formula.id,
                                estado = "lista"
                            )
                            Toast.makeText(context, "Medicamento marcado como listo", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA))
                ) {
                    Icon(Icons.Default.LocalPharmacy, null, tint = Color.White)
                    Text(" Marcar medicamento listo", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        scope.launch {
                            authViewModel.updateRequestAsPharmacist(
                                requestId = formula.id,
                                estado = "rechazada"
                            )
                            Toast.makeText(context, "Fórmula rechazada", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, Color(0xFFD32F2F))
                ) {
                    Text("Rechazar fórmula", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        scope.launch {
                            authViewModel.updateRequestAsPharmacist(
                                requestId = formula.id,
                                estado = "aplazada"
                            )
                            Toast.makeText(context, "Fórmula aplazada", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                ) {
                    Text("Aplazar solicitud", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, Color(0xFF2F80ED))
            ) {
                Text("Volver al panel", color = Color(0xFF2F80ED), fontWeight = FontWeight.Bold)
            }
        }
    }
}