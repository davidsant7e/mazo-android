package com.davidsant7e.mazo.datos

import com.davidsant7e.mazo.nucleo.UltimoCambio
import com.davidsant7e.mazo.nucleo.Versiones
import java.net.HttpURLConnection
import java.net.URL

/** Último cambio de un repo público (API de GitHub sin cuenta: 60 consultas por hora). */
object GitHub {
    class Respuesta(val cambio: UltimoCambio?, val motivo: String)

    fun ultimoCambio(repo: String): Respuesta = try {
        val c = URL(Versiones.url(repo)).openConnection() as HttpURLConnection
        c.connectTimeout = 10_000
        c.readTimeout = 15_000
        c.setRequestProperty("Accept", "application/vnd.github+json")
        c.setRequestProperty("User-Agent", "ElMazo")
        val codigo = c.responseCode
        if (codigo == 200) {
            val texto = c.inputStream.bufferedReader().use { it.readText() }
            Respuesta(Versiones.leer(texto), "No se entendió la respuesta de GitHub")
        } else {
            Respuesta(null, when (codigo) {
                404 -> "Repo privado o sin acceso"
                403, 429 -> "Cupo de GitHub agotado; intenta en una hora"
                else -> "GitHub respondió $codigo"
            })
        }
    } catch (e: Exception) {
        Respuesta(null, "Sin conexión con GitHub")
    }
}
