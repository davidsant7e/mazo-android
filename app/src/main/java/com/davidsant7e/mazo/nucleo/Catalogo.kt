package com.davidsant7e.mazo.nucleo

/** Dónde vive una carta del mazo. */
enum class Plataforma { ANDROID, OTRA, LIBRE }

/**
 * Una carta de los Arcanos Mayores y lo que hay detrás de ella.
 *
 * @param paquete identificador de la app Android (null si no hay app Android).
 * @param apk enlace directo al APK compilado en GitHub (solo repos públicos con CI).
 */
data class Carta(
    val numero: Int,
    val nombre: String,
    val proyecto: String,
    val descripcion: String,
    val plataforma: Plataforma,
    val paquete: String? = null,
    val repo: String? = null,
    val apk: String? = null,
    /** Dónde corre si no es (solo) Android: «PC con Windows», «VS Code»… */
    val donde: String? = null,
    /** Trabaja en segundo plano: conviene «batería sin restricciones». */
    val bateria: Boolean = false,
) {
    val romano: String get() = Romanos.de(numero)
    val titulo: String get() = "$romano · $nombre"
}

/** Lo que el celular sabe de una app instalada. */
data class Instalada(val version: String?, val actualizada: Long)

/** El mazo ordenado para la pantalla. */
data class Vista(
    /** Apps de los Arcanos instaladas en este celular. */
    val enEsteCelular: List<Carta>,
    /** Tienen app Android, pero no está instalada. */
    val sinInstalar: List<Carta>,
    /** Proyectos del mazo en el PC, VS Code o la web. */
    val otrasPlataformas: List<Carta>,
    /** Números que aún no tienen proyecto. */
    val libres: List<Carta>,
)

object Romanos {
    fun de(n: Int): String {
        if (n == 0) return "0"
        val valores = listOf(10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I")
        var resto = n
        return buildString {
            for ((v, s) in valores) while (resto >= v) { append(s); resto -= v }
        }
    }
}

object Catalogo {
    private const val GH = "https://github.com/davidsant7e/"
    private fun apk(repo: String, archivo: String) = "${GH}$repo/releases/download/ultima/$archivo"

    /** Los 22 Arcanos Mayores. Al agregar un proyecto nuevo, se cambia su carta aquí. */
    val cartas: List<Carta> = listOf(
        Carta(0, "El Loco", "El Mazo", "Este lanzador: todas las cartas en un solo lugar.", Plataforma.ANDROID,
            paquete = "com.davidsant7e.mazo", repo = GH + "mazo-android", apk = apk("mazo-android", "el-mazo.apk")),
        Carta(1, "El Mago", "El Mago", "Escribir en el PC desde el celular.", Plataforma.ANDROID,
            paquete = "com.davidsant7e.mago", repo = GH + "el-mago", donde = "también en el PC con Windows", bateria = true),
        Carta(2, "La Sacerdotisa", "La Sacerdotisa", "Corrector que explica cada cambio.", Plataforma.ANDROID,
            paquete = "com.davidsant7e.sacerdotisa", repo = GH + "sacerdotisa", donde = "también en Windows y el navegador"),
        Carta(3, "La Emperatriz", "La Emperatriz", "Lista de compras por proyectos, precios con Gemini.", Plataforma.ANDROID,
            paquete = "com.davidsant7e.emperatriz", repo = GH + "emperatriz-android",
            apk = apk("emperatriz-android", "la-emperatriz.apk"), donde = "también página para el PC"),
        Carta(4, "El Emperador", "El Emperador", "Inventario de la casa, orientación y soleamiento.", Plataforma.ANDROID,
            paquete = "com.davidsant7e.emperador", repo = GH + "emperador-android"),
        Carta(5, "El Hierofante", "CodeLock", "Una pregunta para aprender al desbloquear.", Plataforma.ANDROID,
            paquete = "com.codelock.app", repo = GH + "codelock-android", donde = "también en Windows", bateria = true),
        Carta(6, "Los Enamorados", "", "", Plataforma.LIBRE),
        Carta(7, "El Carro", "El Carro", "Rutas alternativas en carro y moto, varias paradas.", Plataforma.ANDROID,
            paquete = "com.davidsant7e.carro", repo = GH + "el-carro-android",
            apk = apk("el-carro-android", "el-carro.apk")),
        Carta(8, "La Fuerza", "", "", Plataforma.LIBRE),
        Carta(9, "El Ermitaño", "Perfil de GitHub", "El mazo en el perfil de GitHub.", Plataforma.OTRA,
            repo = GH + "davidsant7e", donde = "Web"),
        Carta(10, "La Rueda", "GirarPantalla", "Girar la pantalla y accesos rápidos.", Plataforma.OTRA,
            repo = GH + "girar-pantalla", donde = "PC con Windows"),
        Carta(11, "La Justicia", "La Justicia", "Revisa el commit y sugiere el mensaje.", Plataforma.OTRA,
            repo = GH + "justicia", donde = "VS Code, Kiro y Visual Studio"),
        Carta(12, "El Colgado", "", "", Plataforma.LIBRE),
        Carta(13, "La Muerte", "", "", Plataforma.LIBRE),
        Carta(14, "La Templanza", "La Templanza", "Horas en Azure DevOps Boards.", Plataforma.ANDROID,
            paquete = "com.davidsant7e.templanza", bateria = true),
        Carta(15, "El Diablo", "", "", Plataforma.LIBRE),
        Carta(16, "La Torre", "", "", Plataforma.LIBRE),
        Carta(17, "La Estrella", "AudioNovel", "Novelas traducidas en audio, retoma donde quedaste.", Plataforma.ANDROID,
            paquete = "com.audionovel.app", donde = "la traducción y el audio se hacen en el PC", bateria = true),
        Carta(18, "La Luna", "", "", Plataforma.LIBRE),
        Carta(19, "El Sol", "El Sol", "Escenarios de prueba y pruebas de punta a punta.", Plataforma.OTRA,
            repo = GH + "el-sol", donde = "VS Code y Kiro"),
        Carta(20, "El Juicio", "MicCheck", "Diagnóstico del micrófono, grabadora y registro.", Plataforma.ANDROID,
            paquete = "com.dseu1.miccheck", repo = GH + "miccheck-android"),
        Carta(21, "El Mundo", "El Mundo", "Validación local antes del push, el PR y el CI.", Plataforma.OTRA,
            donde = "Terminal, VS Code y Kiro"),
    )

    /** Paquetes que El Mazo necesita poder ver (se declaran en el manifiesto, <queries>). */
    val paquetes: List<String> get() = cartas.mapNotNull { it.paquete }

    fun porPaquete(paquete: String): Carta? = cartas.firstOrNull { it.paquete == paquete }

    /**
     * Ordena el mazo según lo instalado en el celular. El Mazo mismo no aparece entre las apps
     * (ya estás en él), pero sí cuenta para las cartas tomadas.
     */
    fun vista(instaladas: Map<String, Instalada>, propio: String = "com.davidsant7e.mazo"): Vista {
        val android = cartas.filter { it.plataforma == Plataforma.ANDROID && it.paquete != propio }
        return Vista(
            enEsteCelular = android.filter { it.paquete in instaladas },
            sinInstalar = android.filter { it.paquete !in instaladas },
            otrasPlataformas = cartas.filter { it.plataforma == Plataforma.OTRA },
            libres = cartas.filter { it.plataforma == Plataforma.LIBRE },
        )
    }
}
