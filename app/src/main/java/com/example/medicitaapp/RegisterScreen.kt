package com.example.medicitaapp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by remember { mutableStateOf("") }
    var documento by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F8))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 16.dp),
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
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopIcon()

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Crear cuenta",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2430)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Registre sus datos para ingresar a la aplicación y gestionar sus medicamentos.",
                    fontSize = 15.sp,
                    color = Color(0xFF7B8494),
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                RegisterField(
                    label = "Nombre completo",
                    value = nombre,
                    onValueChange = { nombre = it },
                    icon = Icons.Default.Person,
                    placeholder = "Ingrese su nombre"
                )

                Spacer(modifier = Modifier.height(16.dp))

                RegisterField(
                    label = "Documento",
                    value = documento,
                    onValueChange = { documento = it },
                    icon = Icons.Default.Badge,
                    placeholder = "Ingrese su documento"
                )

                Spacer(modifier = Modifier.height(16.dp))

                RegisterField(
                    label = "Teléfono",
                    value = telefono,
                    onValueChange = { telefono = it },
                    icon = Icons.Default.Phone,
                    placeholder = "Ingrese su teléfono"
                )

                Spacer(modifier = Modifier.height(16.dp))

                RegisterField(
                    label = "Contraseña",
                    value = password,
                    onValueChange = { password = it },
                    icon = Icons.Default.Lock,
                    placeholder = "Cree una contraseña"
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = {
                        when {
                            nombre.isBlank() -> {
                                Toast.makeText(context, "Ingrese su nombre", Toast.LENGTH_SHORT).show()
                            }
                            documento.isBlank() -> {
                                Toast.makeText(context, "Ingrese su documento", Toast.LENGTH_SHORT).show()
                            }
                            telefono.isBlank() -> {
                                Toast.makeText(context, "Ingrese su teléfono", Toast.LENGTH_SHORT).show()
                            }
                            password.isBlank() -> {
                                Toast.makeText(context, "Ingrese una contraseña", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                scope.launch {
                                    val result = authViewModel.register(
                                        nombre,
                                        documento,
                                        telefono,
                                        password
                                    )

                                    result.onSuccess {
                                        Toast.makeText(
                                            context,
                                            "Usuario registrado exitosamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onSuccess()
                                    }

                                    result.onFailure {
                                        Toast.makeText(
                                            context,
                                            it.message ?: "Error al registrar",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F80ED)
                    )
                ) {
                    Text(
                        text = "Registrarme",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        Color(0xFF2F80ED)
                    )
                ) {
                    Text(
                        text = "Volver al login",
                        color = Color(0xFF2F80ED),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFF2F5FA),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Registro seguro",
                        color = Color(0xFF6D7685),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Sus datos serán almacenados localmente para ingresar a la aplicación.",
                        color = Color(0xFF8A93A3),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Complete sus datos para continuar",
                    color = Color(0xFF9AA2AE),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun RegisterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3140)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFFA1A8B3)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFA1A8B3)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = customTextFieldColors()
        )
    }
}