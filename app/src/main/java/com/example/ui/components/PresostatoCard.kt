package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PresostatoStatus
import com.example.data.model.WashingState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenGlow
import com.example.ui.theme.WarningAmber

@Composable
fun PresostatoCard(
    state: WashingState,
    modifier: Modifier = Modifier
) {
    val isFull = state.presostato == PresostatoStatus.LLENO

    val statusColor by animateColorAsState(
        targetValue = when (state.presostato) {
            PresostatoStatus.LLENO -> SuccessGreen
            PresostatoStatus.LLENANDO -> CyanAccent
            PresostatoStatus.VACIO -> Slate400
            PresostatoStatus.DESCONOCIDO -> WarningAmber
        },
        animationSpec = tween(400),
        label = "presostato_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("presostato_card"),
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isFull) SuccessGreen.copy(alpha = 0.5f) else Slate700
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Presostato Sensor Title & Live indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Slate800, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Presostato",
                            tint = ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PRESOSTATO (NIVEL DE AGUA)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Sensor de Presión Hidrostática",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate100
                        )
                    }
                }

                // Status Pill
                Row(
                    modifier = Modifier
                        .background(
                            if (isFull) SuccessGreenGlow else Slate800,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isFull) SuccessGreen else Slate700,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFull) "LLENO" else "VACÍO",
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Presostato Status Details: Contact state & Telemetry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate800.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Contacto Eléctrico",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                    Text(
                        text = if (isFull) "CERRADO (Corte de Válvula)" else "ABIERTO (Habilita Llenado)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFull) SuccessGreen else Slate300Color
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Valor /api/estado",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                    Text(
                        text = state.presostatoRaw.ifBlank { "Sin dato" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Water Level Progress Bar matching the presostato
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nivel Detectado por Presión",
                        fontSize = 11.sp,
                        color = Slate400
                    )
                    Text(
                        text = "${state.nivelAgua}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFull) SuccessGreen else CyanAccent
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { state.nivelAgua / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isFull) SuccessGreen else CyanAccent,
                    trackColor = Slate800,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

private val Slate300Color = androidx.compose.ui.graphics.Color(0xFFCBD5E1)
