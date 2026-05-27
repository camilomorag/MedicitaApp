package com.example.medicitaapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.medicitaapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadFormulaScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileType by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("") }

    // Estado para permisos
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher para pedir permiso de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "✅ Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "❌ Necesitas el permiso de cámara para tomar fotos", Toast.LENGTH_LONG).show()
        }
    }

    // Archivo temporal para la foto de la cámara
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            selectedImageUri = photoUri
            selectedFileType = "image"
            fileName = "Foto tomada"
            Toast.makeText(context, "✅ Foto tomada correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "❌ No se pudo tomar la foto", Toast.LENGTH_SHORT).show()
        }
        photoUri = null
    }

    // Launcher para seleccionar imagen de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedFileType = "image"
            fileName = uri.toString().takeLast(30)
            Toast.makeText(context, "✅ Imagen seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para seleccionar PDF
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            selectedFileType = "pdf"
            fileName = uri.toString().takeLast(30)
            Toast.makeText(context, "✅ PDF seleccionado", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para tomar foto
    fun takePhoto() {
        // Verificar permiso primero
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = context.cacheDir
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

            photoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
            } else {
                Uri.fromFile(imageFile)
            }

            cameraLauncher.launch(photoUri!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "❌ Error al preparar la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para eliminar archivo seleccionado
    fun clearSelectedFile() {
        selectedImageUri = null
        selectedFileType = ""
        fileName = ""
        Toast.makeText(context, "Archivo eliminado", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        containerColor = Color(0xFFF3F5F9),
        topBar = {
            TopAppBar(
                title = { Text("Subir fórmula médica", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
            Text(
                text = "📸 Subir fórmula médica",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2433),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tome una foto o suba un archivo de su fórmula",
                fontSize = 14.sp,
                color = Color(0xFF6E7786),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Consejos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📋 Consejos para una buena foto:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Busque un lugar bien iluminado", fontSize = 13.sp)
                    Text("2. Coloque el papel sobre una superficie plana", fontSize = 13.sp)
                    Text("3. Mantenga el teléfono firme", fontSize = 13.sp)
                    Text("4. Asegúrese de que toda la fórmula sea visible", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Marco indicador
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(2.dp, Color(0xFF2F80ED), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBFE))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (selectedFileType == "pdf") Icons.Default.PictureAsPdf else Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF2F80ED),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedImageUri != null) "📄 ${fileName.take(20)}" else "Coloque la fórmula dentro de este marco",
                            fontSize = if (selectedImageUri != null) 12.sp else 14.sp,
                            color = Color(0xFF6E7786),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Seleccione una opción",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2433),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { takePhoto() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED))
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("📷 Tomar foto", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFF2F80ED))
            ) {
                Icon(Icons.Default.Image, null, tint = Color(0xFF2F80ED))
                Spacer(modifier = Modifier.width(8.dp))
                Text("🖼️ Elegir imagen de galería", color = Color(0xFF2F80ED), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { pdfLauncher.launch(arrayOf("application/pdf")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFF9C27B0))
            ) {
                Icon(Icons.Default.PictureAsPdf, null, tint = Color(0xFF9C27B0))
                Spacer(modifier = Modifier.width(8.dp))
                Text("📄 Subir archivo PDF", color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold)
            }

            // Archivo seleccionado
            if (selectedImageUri != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (selectedFileType == "pdf") Icons.Default.PictureAsPdf else Icons.Default.Image,
                                    null,
                                    tint = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedFileType == "pdf") "PDF seleccionado" else "Imagen seleccionada",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            IconButton(
                                onClick = { clearSelectedFile() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar archivo",
                                    tint = Color(0xFFD32F2F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (selectedImageUri == null) {
                                    Toast.makeText(context, "❌ Seleccione una imagen o PDF primero", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isLoading = true
                                scope.launch {
                                    val result = authViewModel.submitFormulaRequest(
                                        formulaUri = selectedImageUri.toString(),
                                        formulaType = selectedFileType
                                    )
                                    isLoading = false
                                    result.onSuccess {
                                        Toast.makeText(context, "✅ Fórmula enviada correctamente", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    }
                                    result.onFailure {
                                        Toast.makeText(context, "❌ Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.Send, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enviar fórmula", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF182235))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "❓ ¿Necesita ayuda?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Si tiene dificultades para tomar la foto, pida ayuda a alguien cercano o llámenos.",
                        fontSize = 13.sp,
                        color = Color(0xFFE3EAF7)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:018000123456"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED))
                    ) {
                        Icon(Icons.Default.Call, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LLAMAR A SOPORTE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}