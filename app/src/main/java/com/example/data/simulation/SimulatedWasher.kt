package com.example.data.simulation

import com.example.data.model.MachineStatus
import com.example.data.model.PresostatoStatus
import com.example.data.model.WashingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SimulatedWasher(private val scope: CoroutineScope) {

    private val _simState = MutableStateFlow(
        WashingState(
            estadoRaw = "IDLE",
            estado = MachineStatus.IDLE,
            presostatoRaw = "VACIO",
            presostato = PresostatoStatus.VACIO,
            nivelAgua = 0,
            tiempoRestanteSeg = 0,
            cicloActual = null,
            rawJson = """{"estado":"IDLE","presostato":"VACIO","nivel":0}"""
        )
    )
    val simState: StateFlow<WashingState> = _simState.asStateFlow()

    private var activeJob: Job? = null

    fun iniciarCicloCompleto() {
        activeJob?.cancel()
        activeJob = scope.launch(Dispatchers.Default) {
            updateState(MachineStatus.INICIANDO, "INICIANDO", 0, PresostatoStatus.VACIO, "VACIO", 180, "Ciclo Completo")
            delay(1500)

            // Fase 1: Llenado
            updateState(MachineStatus.LLENANDO, "LLENANDO", 5, PresostatoStatus.VACIO, "VACIO", 175, "Ciclo Completo - Llenado")
            for (level in 10..100 step 15) {
                if (!isActive) return@launch
                delay(800)
                val presostato = if (level >= 90) PresostatoStatus.LLENO else PresostatoStatus.VACIO
                val presostatoRaw = if (level >= 90) "LLENO" else "VACIO"
                updateState(MachineStatus.LLENANDO, "LLENANDO", level, presostato, presostatoRaw, 175 - (level / 2), "Ciclo Completo - Llenado")
            }

            // Fase 2: Agitación / Lavado
            updateState(MachineStatus.AGITANDO, "AGITANDO", 100, PresostatoStatus.LLENO, "LLENO", 120, "Ciclo Completo - Lavado")
            for (sec in 10 downTo 1) {
                if (!isActive) return@launch
                delay(1000)
                updateState(MachineStatus.AGITANDO, "AGITANDO", 100, PresostatoStatus.LLENO, "LLENO", 110 + sec, "Ciclo Completo - Lavado")
            }

            // Fase 3: Drenado
            updateState(MachineStatus.DRENANDO, "DRENANDO", 95, PresostatoStatus.LLENO, "LLENO", 90, "Ciclo Completo - Desagüe")
            for (level in 80 downTo 0 step 20) {
                if (!isActive) return@launch
                delay(800)
                val presostato = if (level <= 10) PresostatoStatus.VACIO else PresostatoStatus.LLENO
                val presostatoRaw = if (level <= 10) "VACIO" else "LLENO"
                updateState(MachineStatus.DRENANDO, "DRENANDO", level, presostato, presostatoRaw, 70 - (level / 3), "Ciclo Completo - Desagüe")
            }

            // Fase 4: Centrifugado
            updateState(MachineStatus.CENTRIFUGANDO, "CENTRIFUGANDO", 0, PresostatoStatus.VACIO, "VACIO", 40, "Ciclo Completo - Centrifugado")
            delay(4000)

            // Fin
            updateState(MachineStatus.FINALIZADO, "FINALIZADO", 0, PresostatoStatus.VACIO, "VACIO", 0, "Completado")
            delay(3000)
            updateState(MachineStatus.IDLE, "IDLE", 0, PresostatoStatus.VACIO, "VACIO", 0, null)
        }
    }

    fun llenar() {
        activeJob?.cancel()
        activeJob = scope.launch(Dispatchers.Default) {
            updateState(MachineStatus.LLENANDO, "LLENANDO", _simState.value.nivelAgua, _simState.value.presostato, _simState.value.presostatoRaw, 60, "Llenar")
            var currentLevel = _simState.value.nivelAgua
            while (currentLevel < 100 && isActive) {
                delay(700)
                currentLevel = (currentLevel + 20).coerceAtMost(100)
                val presostato = if (currentLevel >= 85) PresostatoStatus.LLENO else PresostatoStatus.VACIO
                val presostatoRaw = if (currentLevel >= 85) "LLENO" else "VACIO"
                updateState(MachineStatus.LLENANDO, "LLENANDO", currentLevel, presostato, presostatoRaw, (100 - currentLevel), "Llenar")
            }
            if (isActive) {
                updateState(MachineStatus.IDLE, "IDLE", 100, PresostatoStatus.LLENO, "LLENO", 0, "Llenado Completo")
            }
        }
    }

    fun agitar() {
        activeJob?.cancel()
        activeJob = scope.launch(Dispatchers.Default) {
            val level = if (_simState.value.nivelAgua > 0) _simState.value.nivelAgua else 80
            val presostato = if (level >= 80) PresostatoStatus.LLENO else PresostatoStatus.VACIO
            updateState(MachineStatus.AGITANDO, "AGITANDO", level, presostato, presostato.name, 90, "Agitación")
        }
    }

    fun drenar() {
        activeJob?.cancel()
        activeJob = scope.launch(Dispatchers.Default) {
            updateState(MachineStatus.DRENANDO, "DRENANDO", _simState.value.nivelAgua, _simState.value.presostato, _simState.value.presostatoRaw, 45, "Drenar")
            var currentLevel = _simState.value.nivelAgua
            while (currentLevel > 0 && isActive) {
                delay(700)
                currentLevel = (currentLevel - 25).coerceAtLeast(0)
                val presostato = if (currentLevel <= 10) PresostatoStatus.VACIO else PresostatoStatus.LLENO
                val presostatoRaw = if (currentLevel <= 10) "VACIO" else "LLENO"
                updateState(MachineStatus.DRENANDO, "DRENANDO", currentLevel, presostato, presostatoRaw, currentLevel, "Drenar")
            }
            if (isActive) {
                updateState(MachineStatus.IDLE, "IDLE", 0, PresostatoStatus.VACIO, "VACIO", 0, "Drenado Completo")
            }
        }
    }

    fun paroEmergencia() {
        activeJob?.cancel()
        val currentLevel = _simState.value.nivelAgua
        val currentPresostato = _simState.value.presostato
        updateState(
            estado = MachineStatus.PARO_EMERGENCIA,
            estadoRaw = "PARO_EMERGENCIA",
            nivelAgua = currentLevel,
            presostato = currentPresostato,
            presostatoRaw = currentPresostato.name,
            tiempoRestante = 0,
            ciclo = "DETENIDO POR PARO DE EMERGENCIA"
        )
    }

    private fun updateState(
        estado: MachineStatus,
        estadoRaw: String,
        nivelAgua: Int,
        presostato: PresostatoStatus,
        presostatoRaw: String,
        tiempoRestante: Int?,
        ciclo: String?
    ) {
        val json = """{"estado":"$estadoRaw","presostato":"$presostatoRaw","nivel":$nivelAgua,"tiempo":${tiempoRestante ?: 0},"ciclo":"${ciclo ?: ""}"}"""
        _simState.value = WashingState(
            estadoRaw = estadoRaw,
            estado = estado,
            presostatoRaw = presostatoRaw,
            presostato = presostato,
            nivelAgua = nivelAgua,
            tiempoRestanteSeg = tiempoRestante,
            cicloActual = ciclo,
            valvulaLlenadoActiva = estado == MachineStatus.LLENANDO,
            motorAgitadorActivo = estado == MachineStatus.AGITANDO || estado == MachineStatus.CENTRIFUGANDO,
            bombaDrenadoActiva = estado == MachineStatus.DRENANDO,
            rawJson = json
        )
    }
}
