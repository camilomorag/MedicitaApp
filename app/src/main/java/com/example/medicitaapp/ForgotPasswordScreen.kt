package com.example.medicitaapp

import android.widget.Toast
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
import com.example.medicitaapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var documento by remember { mutableStateOf("") }
    var nuevaPassword by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var usuarioEncontrado by remember { mutableStateOf(false) }
    var userData by remember { mutableStateOf<com.example.medicitaapp.data.UserEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F8))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFEAF2FF), RoundedCornerShape(36.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LockReset,
                        contentDescription = null,
                        tint = Color(0xFF2F80ED),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Restablecer contraseña",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2430)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ingrese sus datos para verificar su identidad",
                    fontSize = 14.sp,
                    color = Color(0xFF7B8494),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Campos de verificación
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre completo") },
                    placeholder = { Text("Ingrese su nombre") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    enabled = !usuarioEncontrado,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = documento,
                    onValueChange = { documento = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Número de documento") },
                    placeholder = { Text("Ingrese su documento") },
                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                    enabled = !usuarioEncontrado,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón verificar usuario
                if (!usuarioEncontrado) {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val user = authViewModel.getUserByDocumento(documento)
                                isLoading = false

                                if (user != null && user.nombre.equals(nombre, ignoreCase = true)) {
                                    usuarioEncontrado = true
                                    userData = user
                                    Toast.makeText(context, "Usuario verificado", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Usuario no encontrado. Verifique sus datos.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Verificar usuario", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Campos para nueva contraseña (solo si usuario encontrado)
                if (usuarioEncontrado) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Usuario: ${userData?.nombre}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B7F3B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nuevaPassword,
                        onValueChange = { nuevaPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nueva contraseña") },
                        placeholder = { Text("Ingrese nueva contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmarPassword,
                        onValueChange = { confirmarPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirmar contraseña") },
                        placeholder = { Text("Repita la nueva contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            when {
                                nuevaPassword.isBlank() -> {
                                    Toast.makeText(context, "Ingrese una contraseña", Toast.LENGTH_SHORT).show()
                                }
                                nuevaPassword != confirmarPassword -> {
                                    Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                                }
                                nuevaPassword.length < 4 -> {
                                    Toast.makeText(context, "La contraseña debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    scope.launch {
                                        authViewModel.updatePassword(documento, nuevaPassword)
                                        Toast.makeText(context, "Contraseña actualizada. Inicie sesión.", Toast.LENGTH_LONG).show()
                                        onSuccess()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Actualizar contraseña", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón volver
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al inicio de sesión", color = Color(0xFF2F80ED))
                }
            }
        }
    }
}