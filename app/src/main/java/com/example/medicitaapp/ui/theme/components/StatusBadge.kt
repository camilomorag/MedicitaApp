package com.example.medicitaapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun StatusBadge(status: String) {

    val background = when (status.lowercase()) {
        "aceptada" -> Color(0xFFDFF5E3)
        "rechazada" -> Color(0xFFFDE2E2)
        "aplazada" -> Color(0xFFFFF2CC)
        "pendiente" -> Color(0xFFE8F0FF)
        "lista" -> Color(0xFFE6E0FF)
        else -> Color(0xFFEDEDED)
    }

    val textColor = when (status.lowercase()) {
        "aceptada" -> Color(0xFF1B7F3B)
        "rechazada" -> Color(0xFFC62828)
        "aplazada" -> Color(0xFFB26A00)
        "pendiente" -> Color(0xFF2F80ED)
        "lista" -> Color(0xFF7B1FA2)
        else -> Color(0xFF555555)
    }

    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(50.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}