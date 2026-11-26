package com.davidsant7e.mazo.nucleo

import java.time.OffsetDateTime

/** Último cambio de un repo en GitHub. */
data class UltimoCambio(val sha: String, val fechaMs: Long, val mensaje: String)

/** Resultado de comparar lo instalado con GitHub. */
sealed interface Comparacion {
    /** Lo instalado es posterior al último cambio del repo. */
    data object AlDia : Comparacion

    /** Hay cambios en GitHub después de la instalación. */
    data class HayCambios(val cambio: UltimoCambio) : Comparacion

    /** No se pudo saber (repo privado, sin internet, cupo de GitHub agotado). */
    data class SinDato(val motivo: String) : Comparacion
}

object Versiones {
    /** URL del último commit de la rama principal (API pública de GitHub). */
    fun url(repo: String): String {
        // «https://github.com/<dueño>/<repo>»: el dueño sale de la URL (26 nov: el usuario pasó a el-ermitanho).
        val partes = repo.trimEnd('/').split('/')
        val nombre = partes.last()
        val duenio = partes.getOrNull(partes.size - 2)?.takeIf { it.isNotBlank() && !it.contains('.') } ?: "el-ermitanho"
        return "https://api.github.com/repos/$duenio/$nombre/commits/main"
    }

    /** Lee la respuesta de GitHub (`GET /repos/{dueño}/{repo}/commits/main`). */
    fun leer(json: String): UltimoCambio? = runCatching {
        val r = Json.parse(json)
        val commit = r.obj("commit")
        val fecha = commit.obj("committer").texto("date") ?: commit.obj("author").texto("date") ?: return null
        UltimoCambio(
            sha = (r.texto("sha") ?: "").take(7),
            fechaMs = OffsetDateTime.parse(fecha).toInstant().toEpochMilli(),
            mensaje = commit.texto("message")?.lineSequence()?.firstOrNull().orEmpty(),
        )
    }.getOrNull()

    /**
     * Compara la fecha de instalación con el último cambio. Se da un margen de 10 minutos:
     * el APK de GitHub se publica unos minutos después del commit.
     */
    fun comparar(instaladaMs: Long, ultimo: UltimoCambio?, motivoSinDato: String = "Sin datos de GitHub"): Comparacion = when {
        ultimo == null -> Comparacion.SinDato(motivoSinDato)
        ultimo.fechaMs > instaladaMs + 10 * 60_000L -> Comparacion.HayCambios(ultimo)
        else -> Comparacion.AlDia
    }
}
