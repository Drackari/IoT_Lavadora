package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MachineStatus
import com.example.data.model.PresostatoStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Lavadora IoT", appName)
    }

    @Test
    fun `machine status parsing handles all commands`() {
        assertEquals(MachineStatus.LLENANDO, MachineStatus.fromString("LLENANDO"))
        assertEquals(MachineStatus.AGITANDO, MachineStatus.fromString("AGITANDO"))
        assertEquals(MachineStatus.DRENANDO, MachineStatus.fromString("DRENANDO"))
        assertEquals(MachineStatus.PARO_EMERGENCIA, MachineStatus.fromString("PARO_EMERGENCIA"))
        assertEquals(MachineStatus.PARO_EMERGENCIA, MachineStatus.fromString("DETENIDO"))
    }

    @Test
    fun `presostato status parses booleans numbers and strings`() {
        assertEquals(PresostatoStatus.LLENO, PresostatoStatus.fromAny(true))
        assertEquals(PresostatoStatus.VACIO, PresostatoStatus.fromAny(false))
        assertEquals(PresostatoStatus.LLENO, PresostatoStatus.fromAny(1))
        assertEquals(PresostatoStatus.VACIO, PresostatoStatus.fromAny(0))
        assertEquals(PresostatoStatus.LLENO, PresostatoStatus.fromAny("LLENO"))
        assertEquals(PresostatoStatus.VACIO, PresostatoStatus.fromAny("VACIO"))
        assertTrue(PresostatoStatus.LLENO.isFull)
    }
}
