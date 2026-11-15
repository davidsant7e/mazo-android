package com.davidsant7e.mazo.nucleo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogoTest {

    @Test fun `los 22 arcanos en orden y sin paquetes repetidos`() {
        assertEquals((0..21).toList(), Catalogo.cartas.map { it.numero })
        assertEquals(Catalogo.paquetes.size, Catalogo.paquetes.toSet().size)
        assertTrue(Catalogo.cartas.filter { it.plataforma == Plataforma.ANDROID }.all { it.paquete != null })
        assertTrue(Catalogo.cartas.filter { it.plataforma != Plataforma.ANDROID }.all { it.paquete == null })
    }

    @Test fun romanos() {
        assertEquals(listOf("0", "I", "IV", "IX", "XIV", "XVIII", "XIX", "XXI"), listOf(0, 1, 4, 9, 14, 18, 19, 21).map { Romanos.de(it) })
        assertEquals("VII · El Carro", Catalogo.cartas[7].titulo)
    }

    @Test fun `vista segun lo instalado`() {
        val v = Catalogo.vista(
            mapOf(
                "com.davidsant7e.carro" to Instalada("0.3.0", 1L),
                "com.audionovel.app" to Instalada("0.2.0", 2L),
                "com.davidsant7e.mazo" to Instalada("0.1.0", 3L),
                "com.otra.app" to Instalada("1", 4L),
            ),
        )
        assertEquals(listOf(7, 17), v.enEsteCelular.map { it.numero })
        // El Mazo no se lista a sí mismo.
        assertTrue(v.sinInstalar.none { it.numero == 0 })
        assertEquals(listOf(1, 2, 3, 4, 5, 14, 20), v.sinInstalar.map { it.numero })
        assertEquals(listOf(9, 10, 11, 19, 21), v.otrasPlataformas.map { it.numero })
        assertEquals(listOf(6, 8, 12, 13, 15, 16, 18), v.libres.map { it.numero })
    }

    @Test fun `apk solo en repos publicos con CI`() {
        val conApk = Catalogo.cartas.filter { it.apk != null }.map { it.numero }
        assertEquals(listOf(3, 7), conApk)
        assertTrue(Catalogo.cartas.mapNotNull { it.apk }.all { it.startsWith("https://github.com/davidsant7e/") && it.endsWith(".apk") })
    }
}
