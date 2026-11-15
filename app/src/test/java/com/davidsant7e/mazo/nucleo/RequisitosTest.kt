package com.davidsant7e.mazo.nucleo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RequisitosTest {
    private val P = "android.permission."

    @Test fun `sacerdotisa sin accesibilidad ni superposicion`() {
        val r = Requisitos.de(Hechos(
            permisos = mapOf("${P}SYSTEM_ALERT_WINDOW" to false, "${P}WRITE_SECURE_SETTINGS" to false, "${P}INTERNET" to true),
            usaAccesibilidad = true, accesibilidadActiva = false, superposicion = false,
        ))
        assertEquals(listOf("Accesibilidad", "Mostrar sobre otras apps", "Permiso dado por el PC (adb)"), r.map { it.nombre })
        assertTrue(r.all { it.estado == EstadoReq.FALTA })
        // El de adb es opcional: no cuenta para la marca.
        assertEquals(2, Requisitos.faltan(r))
    }

    @Test fun `permisos comunes y ubicacion aproximada`() {
        val r = Requisitos.de(Hechos(
            permisos = mapOf("${P}ACCESS_FINE_LOCATION" to false, "${P}ACCESS_COARSE_LOCATION" to true, "${P}POST_NOTIFICATIONS" to false, "${P}RECORD_AUDIO" to true),
        ))
        assertEquals(mapOf("Notificaciones" to EstadoReq.FALTA, "Ubicación" to EstadoReq.OK, "Micrófono" to EstadoReq.OK), r.associate { it.nombre to it.estado })
        assertEquals(1, Requisitos.faltan(r))
    }

    @Test fun `bateria y superposicion desconocidas`() {
        val r = Requisitos.de(Hechos(permisos = mapOf("${P}SYSTEM_ALERT_WINDOW" to true), superposicion = null, necesitaBateria = true, bateriaSinRestricciones = true))
        assertEquals(listOf(EstadoReq.DESCONOCIDO, EstadoReq.OK), r.map { it.estado })
        assertEquals(0, Requisitos.faltan(r))
        assertTrue(Requisitos.de(Hechos(permisos = emptyMap())).isEmpty())
    }

    @Test fun `aviso de depuracion`() {
        assertTrue(Depuracion(true, true, false).aviso.startsWith("Depuración USB activa"))
        assertTrue(Depuracion(true, false, false).aviso.contains("apagada"))
        assertTrue(Depuracion(false, false, false).aviso.contains("7 veces"))
    }

    @Test fun `versiones contra github`() {
        val json = """{"sha":"6546dc0abcdef","commit":{"message":"0 · El Mazo 0.1\n\ndetalle","committer":{"date":"2026-11-15T22:37:00Z"},"author":{"date":"2026-11-15T22:30:00Z"}}}"""
        val u = Versiones.leer(json)!!
        assertEquals("6546dc0", u.sha); assertEquals("0 · El Mazo 0.1", u.mensaje)
        val t = java.time.Instant.parse("2026-11-15T22:37:00Z").toEpochMilli()
        assertEquals(t, u.fechaMs)
        assertEquals(Comparacion.AlDia, Versiones.comparar(t + 1, u))
        assertEquals(Comparacion.AlDia, Versiones.comparar(t - 5 * 60_000, u)) // margen de 10 min
        assertTrue(Versiones.comparar(t - 3_600_000, u) is Comparacion.HayCambios)
        assertTrue(Versiones.comparar(t, null, "privado") is Comparacion.SinDato)
        assertNull(Versiones.leer("{}"))
        assertEquals("https://api.github.com/repos/davidsant7e/el-carro-android/commits/main", Versiones.url("https://github.com/davidsant7e/el-carro-android"))
    }
}
