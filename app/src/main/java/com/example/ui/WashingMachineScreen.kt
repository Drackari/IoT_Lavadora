package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ConnectionStatus
import com.example.ui.components.ActionButtonsGrid
import com.example.ui.components.ConfigBottomSheet
import com.example.ui.components.ConnectionStatusBar
import com.example.ui.components.ConsoleLogsSheet
import com.example.ui.components.PresostatoCard
import com.example.ui.components.WashingDrumView
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate850
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.WashingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WashingMachineScreen(
    viewModel: WashingViewModel,
    modifier: Modifier = Modifier
) {
    val ipAddress by viewModel.ipAddress.collectAsStateWithLifecycle()
    val port by viewModel.port.collectAsStateWithLifecycle()
    val httpMethod by viewModel.httpMethod.collectAsStateWithLifecycle()
    val pollIntervalMs by viewModel.pollIntervalMs.collectAsStateWithLifecycle()
    val isSimulationMode by viewModel.isSimulationMode.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val washingState by viewModel.washingState.collectAsStateWithLifecycle()
    val lastLatencyMs by viewModel.lastLatencyMs.collectAsStateWithLifecycle()
    val isSendingAction by viewModel.isSendingAction.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    var showConfigSheet by remember { mutableStateOf(false) }
    var showLogsSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("washing_machine_screen"),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Slate950,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(ElectricBlue.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalLaundryService,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Lavadora IoT",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Text(
                                text = "Controlador HTTP REST",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showConfigSheet = true },
                        modifier = Modifier.testTag("top_bar_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = Slate100
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = Slate100
                )
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTabletOrLandscape = maxWidth > 650.dp

            if (isTabletOrLandscape) {
                // Adaptive 2-Column layout for tablets & landscape
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Column: Drum and Presostato
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WashingDrumView(state = washingState)
                        PresostatoCard(state = washingState)
                    }

                    // Right Column: Connection and Controls
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ConnectionStatusBar(
                            ipAddress = ipAddress,
                            port = port,
                            connectionStatus = connectionStatus,
                            latencyMs = lastLatencyMs,
                            isSimulationMode = isSimulationMode,
                            onOpenSettings = { showConfigSheet = true },
                            onOpenLogs = { showLogsSheet = true },
                            onToggleSimulation = {
                                viewModel.setSimulationMode(!isSimulationMode)
                            }
                        )

                        ErrorMessageBanner(
                            errorMessage = errorMessage,
                            onDismiss = { viewModel.dismissError() }
                        )

                        ActionButtonsGrid(
                            washingState = washingState,
                            isSendingAction = isSendingAction,
                            onActionClicked = { endpoint, name ->
                                viewModel.executeAction(endpoint, name)
                            }
                        )
                    }
                }
            } else {
                // Mobile Portrait Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ConnectionStatusBar(
                        ipAddress = ipAddress,
                        port = port,
                        connectionStatus = connectionStatus,
                        latencyMs = lastLatencyMs,
                        isSimulationMode = isSimulationMode,
                        onOpenSettings = { showConfigSheet = true },
                        onOpenLogs = { showLogsSheet = true },
                        onToggleSimulation = {
                            viewModel.setSimulationMode(!isSimulationMode)
                        }
                    )

                    ErrorMessageBanner(
                        errorMessage = errorMessage,
                        onDismiss = { viewModel.dismissError() }
                    )

                    WashingDrumView(state = washingState)

                    PresostatoCard(state = washingState)

                    ActionButtonsGrid(
                        washingState = washingState,
                        isSendingAction = isSendingAction,
                        onActionClicked = { endpoint, name ->
                            viewModel.executeAction(endpoint, name)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Bottom Sheets
    if (showConfigSheet) {
        ConfigBottomSheet(
            currentIp = ipAddress,
            currentPort = port,
            currentMethod = httpMethod,
            currentPollIntervalMs = pollIntervalMs,
            onDismiss = { showConfigSheet = false },
            onSave = { newIp, newPort, newMethod, newPollMs ->
                viewModel.updateConfig(newIp, newPort, newMethod, newPollMs)
            }
        )
    }

    if (showLogsSheet) {
        ConsoleLogsSheet(
            logs = logs,
            onClearLogs = { viewModel.clearLogs() },
            onDismiss = { showLogsSheet = false }
        )
    }
}

@Composable
fun ErrorMessageBanner(
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = errorMessage != null,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("error_message_banner"),
                colors = CardDefaults.cardColors(containerColor = EmergencyRed.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = EmergencyRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = Slate100,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
