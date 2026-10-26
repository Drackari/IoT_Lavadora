package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigBottomSheet(
    currentIp: String,
    currentPort: Int,
    currentMethod: String,
    currentPollIntervalMs: Long,
    onDismiss: () -> Unit,
    onSave: (ip: String, port: Int, method: String, pollMs: Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var ipInput by remember { mutableStateOf(currentIp) }
    var portInput by remember { mutableStateOf(currentPort.toString()) }
    var selectedMethod by remember { mutableStateOf(currentMethod) }
    var selectedPollMs by remember { mutableLongStateOf(currentPollIntervalMs) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate900,
        contentColor = Slate100
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("config_bottom_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Configuración de Red IoT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                }

                OutlinedButton(
                    onClick = {
                        ipInput = "192.168.1.4"
                        portInput = "80"
                        selectedMethod = "POST"
                        selectedPollMs = 2000L
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Defecto", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // IP Address field
            OutlinedTextField(
                value = ipInput,
                onValueChange = { ipInput = it },
                label = { Text("Dirección IP de la Lavadora") },
                placeholder = { Text("192.168.1.4") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_ip_address"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = Slate700,
                    focusedLabelColor = ElectricBlue,
                    unfocusedLabelColor = Slate400,
                    focusedContainerColor = Slate850,
                    unfocusedContainerColor = Slate850
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Port field
            OutlinedTextField(
                value = portInput,
                onValueChange = { portInput = it.filter { ch -> ch.isDigit() } },
                label = { Text("Puerto HTTP (80 por defecto)") },
                placeholder = { Text("80") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_port"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = Slate700,
                    focusedLabelColor = ElectricBlue,
                    unfocusedLabelColor = Slate400,
                    focusedContainerColor = Slate850,
                    unfocusedContainerColor = Slate850
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // HTTP Method selection (POST vs GET)
            Text(
                text = "MÉTODO HTTP PARA COMANDOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("POST", "GET").forEach { method ->
                    val isSelected = selectedMethod.equals(method, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(
                                if (isSelected) ElectricBlue else Slate850,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedMethod = method },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = method,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Slate950 else Slate100,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Polling interval selection
            Text(
                text = "INTERVALO DE MONITOREO (/api/estado)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1000L to "1s", 2000L to "2s", 3000L to "3s", 5000L to "5s").forEach { (ms, label) ->
                    val isSelected = selectedPollMs == ms
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(
                                if (isSelected) CyanAccent else Slate850,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedPollMs = ms },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Slate950 else Slate100,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val port = portInput.toIntOrNull() ?: 80
                    onSave(ipInput.ifBlank { "192.168.1.4" }, port, selectedMethod, selectedPollMs)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_config_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = Slate950
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardar y Reconectar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
