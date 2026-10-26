package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ConnectionStatus
import com.example.data.model.HttpLogEntry
import com.example.data.model.MachineStatus
import com.example.data.model.WashingState
import com.example.data.network.ApiResult
import com.example.data.network.WashingApiClient
import com.example.data.simulation.SimulatedWasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WashingViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("washing_iot_prefs", Context.MODE_PRIVATE)
    private val apiClient = WashingApiClient()
    private val simulatedWasher = SimulatedWasher(viewModelScope)

    // User settings
    private val _ipAddress = MutableStateFlow(prefs.getString("ip_address", "192.168.1.4") ?: "192.168.1.4")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    private val _port = MutableStateFlow(prefs.getInt("port", 80))
    val port: StateFlow<Int> = _port.asStateFlow()

    private val _httpMethod = MutableStateFlow(prefs.getString("http_method", "POST") ?: "POST")
    val httpMethod: StateFlow<String> = _httpMethod.asStateFlow()

    private val _pollIntervalMs = MutableStateFlow(prefs.getLong("poll_interval_ms", 2000L))
    val pollIntervalMs: StateFlow<Long> = _pollIntervalMs.asStateFlow()

    private val _isSimulationMode = MutableStateFlow(prefs.getBoolean("simulation_mode", false))
    val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    // Machine and Network State
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.CONECTANDO)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _washingState = MutableStateFlow(WashingState())
    val washingState: StateFlow<WashingState> = _washingState.asStateFlow()

    private val _lastLatencyMs = MutableStateFlow<Long?>(null)
    val lastLatencyMs: StateFlow<Long?> = _lastLatencyMs.asStateFlow()

    private val _isSendingAction = MutableStateFlow(false)
    val isSendingAction: StateFlow<Boolean> = _isSendingAction.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _logs = MutableStateFlow<List<HttpLogEntry>>(emptyList())
    val logs: StateFlow<List<HttpLogEntry>> = _logs.asStateFlow()

    private var pollingJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        startPolling()
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                if (_isSimulationMode.value) {
                    _connectionStatus.value = ConnectionStatus.MODO_SIMULACION
                    _washingState.value = simulatedWasher.simState.value
                    _lastLatencyMs.value = 5L
                } else {
                    pollRealHardware()
                }
                delay(_pollIntervalMs.value)
            }
        }
    }

    private suspend fun pollRealHardware() {
        val result = apiClient.getEstado(
            ip = _ipAddress.value,
            port = _port.value,
            preferGet = true
        )

        when (result) {
            is ApiResult.Success -> {
                _connectionStatus.value = ConnectionStatus.CONECTADO
                _washingState.value = result.data
                _lastLatencyMs.value = result.latencyMs
                _errorMessage.value = null
            }
            is ApiResult.Error -> {
                _connectionStatus.value = ConnectionStatus.DESCONECTADO
                _lastLatencyMs.value = result.latencyMs
                // Do not spam logs on background poll failures; only log if error changes
            }
        }
    }

    fun executeAction(endpoint: String, actionName: String) {
        viewModelScope.launch {
            _isSendingAction.value = true
            val timestamp = timeFormat.format(Date())

            if (_isSimulationMode.value) {
                when (endpoint) {
                    "/api/iniciar" -> simulatedWasher.iniciarCicloCompleto()
                    "/api/llenar" -> simulatedWasher.llenar()
                    "/api/agitar" -> simulatedWasher.agitar()
                    "/api/drenar" -> simulatedWasher.drenar()
                    "/api/detener" -> simulatedWasher.paroEmergencia()
                }
                val msg = "Simulación: $actionName ($endpoint)"
                _statusMessage.value = msg
                appendLog(
                    HttpLogEntry(
                        timestamp = timestamp,
                        method = _httpMethod.value,
                        endpoint = endpoint,
                        statusCode = 200,
                        message = "OK (Simulado): $actionName",
                        latencyMs = 8L,
                        isError = false
                    )
                )
                _isSendingAction.value = false
                return@launch
            }

            // Real HTTP call
            val result = apiClient.sendAction(
                ip = _ipAddress.value,
                port = _port.value,
                endpoint = endpoint,
                method = _httpMethod.value
            )

            when (result) {
                is ApiResult.Success -> {
                    _statusMessage.value = "Éxito: $actionName enviado"
                    _errorMessage.value = null
                    appendLog(
                        HttpLogEntry(
                            timestamp = timestamp,
                            method = _httpMethod.value,
                            endpoint = endpoint,
                            statusCode = result.code,
                            message = result.data,
                            latencyMs = result.latencyMs,
                            isError = false
                        )
                    )
                    // Trigger immediate status refresh
                    pollRealHardware()
                }
                is ApiResult.Error -> {
                    val errMsg = "Error en $actionName ($endpoint): ${result.message}"
                    _errorMessage.value = errMsg
                    appendLog(
                        HttpLogEntry(
                            timestamp = timestamp,
                            method = _httpMethod.value,
                            endpoint = endpoint,
                            statusCode = result.code,
                            message = result.message,
                            latencyMs = result.latencyMs,
                            isError = true
                        )
                    )
                }
            }
            _isSendingAction.value = false
        }
    }

    fun setSimulationMode(enabled: Boolean) {
        _isSimulationMode.value = enabled
        prefs.edit().putBoolean("simulation_mode", enabled).apply()
        if (enabled) {
            _connectionStatus.value = ConnectionStatus.MODO_SIMULACION
            _washingState.value = simulatedWasher.simState.value
            _errorMessage.value = null
            appendLog(
                HttpLogEntry(
                    timestamp = timeFormat.format(Date()),
                    method = "SYSTEM",
                    endpoint = "LOCAL_SIM",
                    statusCode = 200,
                    message = "Modo simulación activado para pruebas locales",
                    latencyMs = 0L
                )
            )
        } else {
            _connectionStatus.value = ConnectionStatus.CONECTANDO
        }
    }

    fun updateConfig(ip: String, port: Int, method: String, pollIntervalMs: Long) {
        val cleanIp = ip.trim()
        _ipAddress.value = cleanIp
        _port.value = port
        _httpMethod.value = method
        _pollIntervalMs.value = pollIntervalMs

        prefs.edit()
            .putString("ip_address", cleanIp)
            .putInt("port", port)
            .putString("http_method", method)
            .putLong("poll_interval_ms", pollIntervalMs)
            .apply()

        startPolling()
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    fun dismissStatusMessage() {
        _statusMessage.value = null
    }

    private fun appendLog(entry: HttpLogEntry) {
        _logs.update { current ->
            (listOf(entry) + current).take(100) // Keep latest 100 entries
        }
    }
}
