package com.example.data.model

enum class MachineStatus(val label: String) {
    IDLE("En Espera"),
    INICIANDO("Iniciando Ciclo"),
    LLENANDO("Llenando Agua"),
    AGITANDO("Agitando / Lavando"),
    DRENANDO("Drenando Agua"),
    CENTRIFUGANDO("Centrifugando"),
    PARO_EMERGENCIA("¡Paro de Emergencia!"),
    FINALIZADO("Ciclo Finalizado"),
    ERROR("Error en Sistema");

    companion object {
        fun fromString(raw: String): MachineStatus {
            val upper = raw.trim().uppercase()
            return when {
                upper.contains("PARO") || upper.contains("DETEN") || upper.contains("STOP") || upper.contains("EMERGENC") -> PARO_EMERGENCIA
                upper.contains("LLEN") || upper.contains("FILL") -> LLENANDO
                upper.contains("AGIT") || upper.contains("LAV") || upper.contains("WASH") -> AGITANDO
                upper.contains("DREN") || upper.contains("DRAIN") || upper.contains("DESAGU") -> DRENANDO
                upper.contains("CENTRIF") || upper.contains("SPIN") -> CENTRIFUGANDO
                upper.contains("INICI") || upper.contains("START") -> INICIANDO
                upper.contains("FIN") || upper.contains("COMPLET") || upper.contains("DONE") -> FINALIZADO
                upper.contains("ERROR") || upper.contains("FALLO") -> ERROR
                upper.contains("IDLE") || upper.contains("ESPERA") || upper.contains("INACTIV") || upper.contains("STANDBY") -> IDLE
                else -> IDLE
            }
        }
    }
}

enum class PresostatoStatus(val label: String, val isFull: Boolean) {
    VACIO("Vacío (Sin Nivel)", false),
    LLENANDO("Llenando...", false),
    LLENO("Lleno (Presión OK)", true),
    DESCONOCIDO("Sin Lectura", false);

    companion object {
        fun fromAny(value: Any?): PresostatoStatus {
            return when (value) {
                is Boolean -> if (value) LLENO else VACIO
                is Number -> if (value.toInt() >= 1) LLENO else VACIO
                is String -> {
                    val s = value.trim().uppercase()
                    when {
                        s == "1" || s == "TRUE" || s.contains("LLENO") || s.contains("FULL") || s.contains("ACTIVO") || s.contains("ALTO") -> LLENO
                        s == "0" || s == "FALSE" || s.contains("VACIO") || s.contains("EMPTY") || s.contains("INACTIVO") || s.contains("BAJO") -> VACIO
                        s.contains("LLENANDO") -> LLENANDO
                        else -> if (s.isNotEmpty()) LLENO else DESCONOCIDO
                    }
                }
                else -> DESCONOCIDO
            }
        }
    }
}

data class WashingState(
    val estadoRaw: String = "IDLE",
    val estado: MachineStatus = MachineStatus.IDLE,
    val presostatoRaw: String = "VACIO",
    val presostato: PresostatoStatus = PresostatoStatus.VACIO,
    val nivelAgua: Int = 0, // 0 - 100%
    val tiempoRestanteSeg: Int? = null,
    val cicloActual: String? = null,
    val valvulaLlenadoActiva: Boolean = false,
    val motorAgitadorActivo: Boolean = false,
    val bombaDrenadoActiva: Boolean = false,
    val timestampMs: Long = System.currentTimeMillis(),
    val rawJson: String = ""
)

enum class ConnectionStatus(val label: String) {
    CONECTADO("Conectado"),
    CONECTANDO("Conectando..."),
    DESCONECTADO("Sin Conexión"),
    MODO_SIMULACION("Modo Simulación")
}

data class HttpLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: String,
    val method: String,
    val endpoint: String,
    val statusCode: Int?,
    val message: String,
    val latencyMs: Long,
    val isError: Boolean = false
)
