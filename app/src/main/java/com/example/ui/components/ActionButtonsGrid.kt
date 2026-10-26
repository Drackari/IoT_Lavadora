package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MachineStatus
import com.example.data.model.WashingState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueVariant
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.EmergencyRedDark
import com.example.ui.theme.EmergencyRedGlow
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueDark

@Composable
fun ActionButtonsGrid(
    washingState: WashingState,
    isSendingAction: Boolean,
    onActionClicked: (endpoint: String, actionName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "COMANDOS DE CONTROL REST",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Slate400,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // 2x2 Grid of Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Button 1: Ciclo Completo (/api/iniciar)
            val isCicloActive = washingState.estado == MachineStatus.INICIANDO ||
                    washingState.cicloActual?.contains("Ciclo Completo") == true
            ActionButtonItem(
                title = "Ciclo Completo",
                endpoint = "/api/iniciar",
                icon = Icons.Default.PlayArrow,
                accentColor = ElectricBlue,
                isActive = isCicloActive,
                isDisabled = isSendingAction,
                testTag = "btn_ciclo_completo",
                modifier = Modifier.weight(1f),
                onClick = { onActionClicked("/api/iniciar", "Ciclo Completo") }
            )

            // Button 2: Llenar (/api/llenar)
            val isLlenarActive = washingState.estado == MachineStatus.LLENANDO
            ActionButtonItem(
                title = "Llenar",
                endpoint = "/api/llenar",
                icon = Icons.Default.WaterDrop,
                accentColor = CyanAccent,
                isActive = isLlenarActive,
                isDisabled = isSendingAction,
                testTag = "btn_llenar",
                modifier = Modifier.weight(1f),
                onClick = { onActionClicked("/api/llenar", "Llenar") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Button 3: Agitar (/api/agitar)
            val isAgitarActive = washingState.estado == MachineStatus.AGITANDO
            ActionButtonItem(
                title = "Agitar",
                endpoint = "/api/agitar",
                icon = Icons.Default.Sync,
                accentColor = ElectricBlueVariant,
                isActive = isAgitarActive,
                isDisabled = isSendingAction,
                testTag = "btn_agitar",
                modifier = Modifier.weight(1f),
                onClick = { onActionClicked("/api/agitar", "Agitar") }
            )

            // Button 4: Drenar (/api/drenar)
            val isDrenarActive = washingState.estado == MachineStatus.DRENANDO
            ActionButtonItem(
                title = "Drenar",
                endpoint = "/api/drenar",
                icon = Icons.Default.WaterDamage,
                accentColor = WaterBlue,
                isActive = isDrenarActive,
                isDisabled = isSendingAction,
                testTag = "btn_drenar",
                modifier = Modifier.weight(1f),
                onClick = { onActionClicked("/api/drenar", "Drenar") }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Button 5: Paro de Emergencia (/api/detener)
        EmergencyStopButton(
            isEmergencyActive = washingState.estado == MachineStatus.PARO_EMERGENCIA,
            isDisabled = isSendingAction,
            onClick = { onActionClicked("/api/detener", "Paro de Emergencia") }
        )
    }
}

@Composable
fun ActionButtonItem(
    title: String,
    endpoint: String,
    icon: ImageVector,
    accentColor: Color,
    isActive: Boolean,
    isDisabled: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .heightIn(min = 82.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isDisabled, onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) accentColor.copy(alpha = 0.18f) else Slate850
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) accentColor else Slate700
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isActive) accentColor.copy(alpha = 0.25f) else Slate800,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isActive) accentColor else Slate100,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(accentColor, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate100
            )

            Text(
                text = endpoint,
                fontSize = 11.sp,
                color = if (isActive) accentColor else Slate400,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun EmergencyStopButton(
    isEmergencyActive: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emergency_pulse")
    val pulseBorder by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_border"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = !isDisabled, onClick = onClick)
            .testTag("btn_paro_emergencia"),
        colors = CardDefaults.cardColors(
            containerColor = if (isEmergencyActive) EmergencyRedContainer else Slate850
        ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (isEmergencyActive) pulseBorder.dp else 1.5.dp,
            color = EmergencyRed
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            EmergencyRedDark.copy(alpha = 0.25f),
                            Slate850,
                            EmergencyRedDark.copy(alpha = 0.25f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(EmergencyRed, CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = "Paro de Emergencia",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Paro de Emergencia",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.3.sp
                    )
                    Text(
                        text = "/api/detener • Detención Inmediata",
                        fontSize = 11.sp,
                        color = EmergencyRed.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(EmergencyRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .border(1.dp, EmergencyRed, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ALTO",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}
