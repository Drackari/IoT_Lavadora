package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MachineStatus
import com.example.data.model.WashingState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WaterBlue
import com.example.ui.theme.WaterBlueDark
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WashingDrumView(
    state: WashingState,
    modifier: Modifier = Modifier
) {
    // Drum animation transitions
    val isSpinning = state.estado == MachineStatus.AGITANDO || state.estado == MachineStatus.CENTRIFUGANDO
    val isEmergency = state.estado == MachineStatus.PARO_EMERGENCIA

    val infiniteTransition = rememberInfiniteTransition(label = "washer_transition")

    // Agitator rotation: fast smooth spin for centrifugando, oscillation for agitando
    val agitatorAngle by if (state.estado == MachineStatus.CENTRIFUGANDO) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spin_angle"
        )
    } else if (state.estado == MachineStatus.AGITANDO) {
        infiniteTransition.animateFloat(
            initialValue = -70f,
            targetValue = 70f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "agitate_angle"
        )
    } else {
        animateFloatAsState(targetValue = 0f, label = "idle_angle")
    }

    // Glow pulse for emergency or active operation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isEmergency) 400 else 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Animated water height
    val animatedWaterPercent by animateFloatAsState(
        targetValue = state.nivelAgua.toFloat(),
        animationSpec = tween(durationMillis = 600),
        label = "water_level_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("washing_drum_card"),
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isEmergency) 2.dp else 1.dp,
            color = if (isEmergency) EmergencyRed.copy(alpha = glowAlpha) else Slate700
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Status badge & Remaining time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Chip
                val (badgeBg, badgeText, badgeColor) = when (state.estado) {
                    MachineStatus.PARO_EMERGENCIA -> Triple(EmergencyRed.copy(alpha = 0.2f), "PARO DE EMERGENCIA", EmergencyRed)
                    MachineStatus.LLENANDO -> Triple(WaterBlue.copy(alpha = 0.2f), "LLENANDO AGUA", CyanAccent)
                    MachineStatus.AGITANDO -> Triple(ElectricBlue.copy(alpha = 0.2f), "AGITANDO / LAVANDO", ElectricBlue)
                    MachineStatus.DRENANDO -> Triple(WaterBlueDark.copy(alpha = 0.2f), "DRENANDO AGUA", WaterBlue)
                    MachineStatus.CENTRIFUGANDO -> Triple(ElectricBlue.copy(alpha = 0.25f), "CENTRIFUGANDO", ElectricBlue)
                    MachineStatus.INICIANDO -> Triple(ElectricBlue.copy(alpha = 0.2f), "INICIANDO...", ElectricBlue)
                    MachineStatus.FINALIZADO -> Triple(SuccessGreen.copy(alpha = 0.2f), "CICLO FINALIZADO", SuccessGreen)
                    MachineStatus.ERROR -> Triple(EmergencyRed.copy(alpha = 0.2f), "ERROR DE SISTEMA", EmergencyRed)
                    MachineStatus.IDLE -> Triple(Slate800, "EN ESPERA (LISTO)", Slate400)
                }

                Row(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(badgeColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                if (state.tiempoRestanteSeg != null && state.tiempoRestanteSeg > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Slate800, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassBottom,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val minutes = state.tiempoRestanteSeg / 60
                        val seconds = state.tiempoRestanteSeg % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            color = Slate100,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metallic Washing Drum with Porous Pattern, Water Level & Agitator
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Slate800, Slate900, Slate950)
                        )
                    )
                    .border(
                        width = 4.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                Slate700,
                                if (isEmergency) EmergencyRed else ElectricBlue,
                                Slate700,
                                if (isEmergency) EmergencyRed else CyanAccent,
                                Slate700
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Water Level Simulation
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Draw internal drum texture (perforations)
                    val center = Offset(width / 2f, height / 2f)
                    val radius = width / 2f - 12.dp.toPx()

                    // Water Fill
                    if (animatedWaterPercent > 0f) {
                        val waterHeight = (height * (animatedWaterPercent / 100f))
                        val waterTop = height - waterHeight

                        val waterPath = Path().apply {
                            moveTo(0f, waterTop)
                            // Draw slight wave
                            val waveAmp = if (isSpinning) 12f else 4f
                            val waveSegments = 4
                            for (i in 1..waveSegments) {
                                val x = width * (i.toFloat() / waveSegments)
                                val y = waterTop + (if (i % 2 == 0) waveAmp else -waveAmp)
                                lineTo(x, y)
                            }
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }

                        drawPath(
                            path = waterPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    CyanAccent.copy(alpha = 0.5f),
                                    WaterBlue.copy(alpha = 0.7f),
                                    WaterBlueDark.copy(alpha = 0.85f)
                                ),
                                startY = waterTop,
                                endY = height
                            )
                        )
                    }

                    // Drum Ring Guide
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw small drum holes around
                    for (angle in 0 until 360 step 30) {
                        val rad = Math.toRadians(angle.toDouble())
                        val r = radius - 16.dp.toPx()
                        val hx = center.x + (r * cos(rad)).toFloat()
                        val hy = center.y + (r * sin(rad)).toFloat()
                        drawCircle(
                            color = Color.White.copy(alpha = 0.15f),
                            radius = 2.5.dp.toPx(),
                            center = Offset(hx, hy)
                        )
                    }
                }

                // Agitator blades (Rotating canvas)
                Canvas(
                    modifier = Modifier
                        .size(110.dp)
                        .rotate(agitatorAngle)
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val bladeRadius = size.width / 2f

                    // Agitator 4-fin star
                    for (i in 0 until 4) {
                        val angle = i * 90.0
                        val rad = Math.toRadians(angle)
                        val endX = center.x + (bladeRadius * cos(rad)).toFloat()
                        val endY = center.y + (bladeRadius * sin(rad)).toFloat()

                        drawLine(
                            brush = Brush.linearGradient(
                                listOf(
                                    Slate400,
                                    if (isEmergency) EmergencyRed else ElectricBlue,
                                    Color.White
                                )
                            ),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 6.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }

                    // Center pulsator cap
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                Slate100,
                                if (isEmergency) EmergencyRedDark else Slate700,
                                Slate950
                            )
                        ),
                        radius = 20.dp.toPx(),
                        center = center
                    )
                }

                // Center Icon / Status Alert
                if (isEmergency) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(EmergencyRed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Emergencia",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else if (state.estado == MachineStatus.IDLE) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Slate800.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalLaundryService,
                            contentDescription = "Lavadora",
                            tint = Slate400,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-status summary: Phase name and Water %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ESTADO MOTOR / VÁLVULAS",
                        fontSize = 11.sp,
                        color = Slate400,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val actuatorText = when {
                        state.estado == MachineStatus.PARO_EMERGENCIA -> "CORTADAS (PARO)"
                        state.valvulaLlenadoActiva -> "Válvula Entrada: ABIERTA"
                        state.motorAgitadorActivo -> "Motor Agitador: ACTIVO"
                        state.bombaDrenadoActiva -> "Bomba Desagüe: ACTIVA"
                        else -> "Reposo (Standby)"
                    }
                    Text(
                        text = actuatorText,
                        fontSize = 13.sp,
                        color = if (isEmergency) EmergencyRed else Slate100,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NIVEL DE AGUA",
                        fontSize = 11.sp,
                        color = Slate400,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = if (state.nivelAgua > 0) CyanAccent else Slate400,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${state.nivelAgua}%",
                            fontSize = 13.sp,
                            color = if (state.nivelAgua > 0) CyanAccent else Slate400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
