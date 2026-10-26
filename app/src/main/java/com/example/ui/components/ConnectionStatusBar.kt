package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionStatus
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun ConnectionStatusBar(
    ipAddress: String,
    port: Int,
    connectionStatus: ConnectionStatus,
    latencyMs: Long?,
    isSimulationMode: Boolean,
    onOpenSettings: () -> Unit,
    onOpenLogs: () -> Unit,
    onToggleSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("connection_status_bar"),
        colors = CardDefaults.cardColors(containerColor = Slate850),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // IP & Port Badge (Clickable to open configuration)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenSettings)
                        .background(Slate800)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (connectionStatus == ConnectionStatus.DESCONECTADO) Icons.Default.WifiOff else Icons.Default.Wifi,
                        contentDescription = null,
                        tint = when (connectionStatus) {
                            ConnectionStatus.CONECTADO -> SuccessGreen
                            ConnectionStatus.MODO_SIMULACION -> CyanAccent
                            ConnectionStatus.CONECTANDO -> WarningAmber
                            ConnectionStatus.DESCONECTADO -> EmergencyRed
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (port == 80) ipAddress else "$ipAddress:$port",
                        color = Slate100,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configurar IP",
                        tint = Slate400,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Actions: Toggle Simulation & Logs
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Simulation toggle badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onToggleSimulation)
                            .background(if (isSimulationMode) CyanAccent.copy(alpha = 0.2f) else Slate800)
                            .border(
                                1.dp,
                                if (isSimulationMode) CyanAccent else Slate700,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("toggle_simulation_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isSimulationMode) CyanAccent else Slate400, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isSimulationMode) "SIMULACIÓN ON" else "PROBAR DEMO",
                            color = if (isSimulationMode) CyanAccent else Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Open Logs button
                    IconButton(
                        onClick = onOpenLogs,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Slate800, CircleShape)
                            .testTag("open_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DataObject,
                            contentDescription = "Ver Logs REST",
                            tint = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Connection state subtitle
            if (connectionStatus == ConnectionStatus.DESCONECTADO && !isSimulationMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmergencyRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sin respuesta de $ipAddress (/api/estado)",
                        color = EmergencyRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Toca 'PROBAR DEMO'",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onToggleSimulation)
                    )
                }
            } else if (latencyMs != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Polling en tiempo real activo (/api/estado)",
                        color = Slate400,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Latencia: ${latencyMs}ms",
                        color = if (latencyMs < 200) SuccessGreen else WarningAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
