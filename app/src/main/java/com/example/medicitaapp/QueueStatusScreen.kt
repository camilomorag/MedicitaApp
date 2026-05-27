package com.example.medicitaapp

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueStatusScreen(
    onBack: () -> Unit,
    turno: String = "A-42",  // Recibir turno específico
    ubicacion: String = "Farmatodo - Calle 100 # 15-20, Bogotá",
    tiempoEspera: Int = 15,
    posicionCola: Int = 3
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFFF3F5F9),
        topBar = {
            TopAppBar(
                title = { Text("Mi Turno", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F5F9))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta del turno
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SU TURNO", fontSize = 14.sp, color = Color(0xFF8993A3), fontWeight = FontWeight.Bold)
                    Text(turno, fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2D6BEB))
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFFDDF6E5))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text("✅ ACTIVO", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF23964E))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información de tiempo y posición
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Timer, null, tint = Color(0xFF2D6BEB), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("TIEMPO ESPERA", fontSize = 11.sp, color = Color(0xFF8993A3), fontWeight = FontWeight.Bold)
                        Text("$tiempoEspera MIN", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D2433))
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.People, null, tint = Color(0xFF2D6BEB), modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("POSICIÓN", fontSize = 11.sp, color = Color(0xFF8993A3), fontWeight = FontWeight.Bold)
                        Text("$posicionCola°", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D2433))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ubicación
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$ubicacion"))
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📍 UBICACIÓN", fontSize = 12.sp, color = Color(0xFF8993A3), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(ubicacion, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D2433))
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8F0FE))
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$ubicacion"))
                                context.startActivity(intent)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOn, null, tint = Color(0xFF2D6BEB), modifier = Modifier.size(48.dp))
                            Text("Tap para ver en Google Maps", fontSize = 12.sp, color = Color(0xFF2D6BEB))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // QR Code
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    Toast.makeText(context, "Mostrando código QR", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E2))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.QrCode, null, tint = Color(0xFFB56722), modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Muestre este código QR al farmaceuta",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFB56722),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de ayuda
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:018000123456"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D6BEB))
            ) {
                Icon(Icons.Default.Phone, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Llamar a la farmacia", color = Color.White)
            }
        }
    }
}