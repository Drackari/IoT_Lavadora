package com.example.data.network

import com.example.data.model.MachineStatus
import com.example.data.model.PresostatoStatus
import com.example.data.model.WashingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WashingApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(3000, TimeUnit.MILLISECONDS)
        .readTimeout(3000, TimeUnit.MILLISECONDS)
        .writeTimeout(3000, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(false)
        .build()

    private val emptyJsonBody = "{}".toRequestBody("application/json; charset=utf-8".toMediaType())

    /**
     * Builds standard base URL, e.g. "http://192.168.1.4:80" or "http://192.168.1.4"
     */
    fun buildBaseUrl(ip: String, port: Int): String {
        val cleanIp = ip.trim().removePrefix("http://").removePrefix("https://").trimEnd('/')
        return if (port == 80 || port <= 0) {
            "http://$cleanIp"
        } else {
            "http://$cleanIp:$port"
        }
    }

    /**
     * Polls /api/estado
     */
    suspend fun getEstado(
        ip: String,
        port: Int,
        preferGet: Boolean = true
    ): ApiResult<WashingState> = withContext(Dispatchers.IO) {
        val baseUrl = buildBaseUrl(ip, port)
        val url = "$baseUrl/api/estado"
        val startTime = System.currentTimeMillis()

        try {
            val request = if (preferGet) {
                Request.Builder()
                    .url(url)
                    .get()
                    .build()
            } else {
                Request.Builder()
                    .url(url)
                    .post(emptyJsonBody)
                    .build()
            }

            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                val bodyString = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    val state = parseWashingState(bodyString)
                    ApiResult.Success(state, response.code, bodyString, latency)
                } else {
                    ApiResult.Error(
                        message = "HTTP ${response.code}: ${response.message}",
                        code = response.code,
                        rawResponse = bodyString,
                        latencyMs = latency
                    )
                }
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ApiResult.Error(
                message = e.localizedMessage ?: "Error de conexión",
                code = null,
                rawResponse = null,
                latencyMs = latency,
                exception = e
            )
        }
    }

    /**
     * Sends action command to endpoints: /api/iniciar, /api/llenar, /api/agitar, /api/drenar, /api/detener
     */
    suspend fun sendAction(
        ip: String,
        port: Int,
        endpoint: String,
        method: String = "POST"
    ): ApiResult<String> = withContext(Dispatchers.IO) {
        val baseUrl = buildBaseUrl(ip, port)
        val cleanEndpoint = if (endpoint.startsWith("/")) endpoint else "/$endpoint"
        val url = "$baseUrl$cleanEndpoint"
        val startTime = System.currentTimeMillis()

        try {
            val requestBuilder = Request.Builder().url(url)
            val request = if (method.equals("GET", ignoreCase = true)) {
                requestBuilder.get().build()
            } else {
                requestBuilder.post(emptyJsonBody).build()
            }

            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - startTime
                val bodyString = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    ApiResult.Success(
                        data = bodyString.ifBlank { "Comando ejecutado correctamente" },
                        code = response.code,
                        rawResponse = bodyString,
                        latencyMs = latency
                    )
                } else {
                    ApiResult.Error(
                        message = "HTTP ${response.code}: ${response.message}",
                        code = response.code,
                        rawResponse = bodyString,
                        latencyMs = latency
                    )
                }
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ApiResult.Error(
                message = e.localizedMessage ?: "Fallo al enviar comando",
                code = null,
                rawResponse = null,
                latencyMs = latency,
                exception = e
            )
        }
    }

    private fun parseWashingState(rawText: String): WashingState {
        val trimmed = rawText.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return try {
                val json = JSONObject(trimmed)

                val estadoStr = when {
                    json.has("estado") -> json.optString("estado")
                    json.has("status") -> json.optString("status")
                    json.has("state") -> json.optString("state")
                    json.has("fase") -> json.optString("fase")
                    else -> "IDLE"
                }

                val presostatoVal = when {
                    json.has("presostato") -> json.opt("presostato")
                    json.has("pressure_switch") -> json.opt("pressure_switch")
                    json.has("presion") -> json.opt("presion")
                    json.has("nivel_lleno") -> json.opt("nivel_lleno")
                    else -> null
                }

                val nivelAgua = when {
                    json.has("nivel") -> json.optInt("nivel", 0)
                    json.has("water_level") -> json.optInt("water_level", 0)
                    json.has("nivel_agua") -> json.optInt("nivel_agua", 0)
                    else -> {
                        // Infer from presostato
                        val pStatus = PresostatoStatus.fromAny(presostatoVal)
                        if (pStatus == PresostatoStatus.LLENO) 100 else 0
                    }
                }

                val tiempoRestante = if (json.has("tiempo")) json.optInt("tiempo") else if (json.has("remaining")) json.optInt("remaining") else null
                val cicloActual = if (json.has("ciclo")) json.optString("ciclo") else null

                val estadoEnum = MachineStatus.fromString(estadoStr)
                val presostatoEnum = PresostatoStatus.fromAny(presostatoVal)

                WashingState(
                    estadoRaw = estadoStr,
                    estado = estadoEnum,
                    presostatoRaw = presostatoVal?.toString() ?: presostatoEnum.name,
                    presostato = presostatoEnum,
                    nivelAgua = nivelAgua.coerceIn(0, 100),
                    tiempoRestanteSeg = tiempoRestante,
                    cicloActual = cicloActual,
                    valvulaLlenadoActiva = estadoEnum == MachineStatus.LLENANDO,
                    motorAgitadorActivo = estadoEnum == MachineStatus.AGITANDO || estadoEnum == MachineStatus.CENTRIFUGANDO,
                    bombaDrenadoActiva = estadoEnum == MachineStatus.DRENANDO,
                    rawJson = trimmed
                )
            } catch (_: Exception) {
                parseFromPlainText(trimmed)
            }
        }
        return parseFromPlainText(trimmed)
    }

    private fun parseFromPlainText(text: String): WashingState {
        val upper = text.uppercase()
        val estadoEnum = MachineStatus.fromString(text)
        val presostatoEnum = when {
            upper.contains("LLENO") || upper.contains("FULL") -> PresostatoStatus.LLENO
            upper.contains("VACIO") || upper.contains("EMPTY") -> PresostatoStatus.VACIO
            upper.contains("LLENANDO") -> PresostatoStatus.LLENANDO
            else -> PresostatoStatus.DESCONOCIDO
        }

        return WashingState(
            estadoRaw = text.ifBlank { "IDLE" },
            estado = estadoEnum,
            presostatoRaw = presostatoEnum.name,
            presostato = presostatoEnum,
            nivelAgua = if (presostatoEnum == PresostatoStatus.LLENO) 100 else 0,
            rawJson = text
        )
    }
}

sealed class ApiResult<out T> {
    data class Success<out T>(
        val data: T,
        val code: Int,
        val rawResponse: String,
        val latencyMs: Long
    ) : ApiResult<T>()

    data class Error(
        val message: String,
        val code: Int?,
        val rawResponse: String?,
        val latencyMs: Long,
        val exception: Throwable? = null
    ) : ApiResult<Nothing>()
}
