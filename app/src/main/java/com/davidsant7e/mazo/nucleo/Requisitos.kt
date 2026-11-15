package com.davidsant7e.mazo.nucleo

/** Qué ajuste de Android abre el botón de un requisito. */
enum class Ajuste { APP, ACCESIBILIDAD, SUPERPOSICION, BATERIA, ADB }

enum class EstadoReq { OK, FALTA, DESCONOCIDO }

/** Algo que una app necesita para funcionar bien, y si lo tiene. */
data class Requisito(
    val nombre: String,
    val estado: EstadoReq,
    val ajuste: Ajuste,
    /** Explicación corta para el usuario (por qué lo necesita o cómo darlo). */
    val ayuda: String = "",
    /** Opcional: si falta no es un problema (solo mejora algo). */
    val opcional: Boolean = false,
)

/**
 * Lo que El Mazo pudo averiguar de otra app desde Android.
 * null = Android no deja verlo desde otra app.
 */
data class Hechos(
    /** Permisos que la app pide → si están concedidos. */
    val permisos: Map<String, Boolean>,
    /** Tiene un servicio de accesibilidad declarado. */
    val usaAccesibilidad: Boolean = false,
    /** Su servicio de accesibilidad está activado en Ajustes. */
    val accesibilidadActiva: Boolean = false,
    /** «Mostrar sobre otras apps» concedido. */
    val superposicion: Boolean? = null,
    /** Excluida de la optimización de batería. */
    val bateriaSinRestricciones: Boolean? = null,
    /** El catálogo dice que conviene batería sin restricciones (trabaja en segundo plano). */
    val necesitaBateria: Boolean = false,
)

object Requisitos {
    private const val P = "android.permission."

    /** Permisos que vale la pena revisar, con su nombre para el usuario y para qué sirven. */
    private val PERMISOS = linkedMapOf(
        "${P}POST_NOTIFICATIONS" to ("Notificaciones" to "Para avisarte (recordatorios, estado, controles)."),
        "${P}ACCESS_FINE_LOCATION" to ("Ubicación" to "Para tu posición (rutas, ubicación de la casa)."),
        "${P}RECORD_AUDIO" to ("Micrófono" to "Para medir, probar y grabar el micrófono."),
        "${P}BLUETOOTH_CONNECT" to ("Dispositivos Bluetooth" to "Para usar audífonos o el casco por Bluetooth."),
        "${P}CAMERA" to ("Cámara" to "Para tomar fotos dentro de la app."),
        "${P}READ_PHONE_STATE" to ("Estado del teléfono" to "Para saber si estás en una llamada."),
    )

    /** Calcula lo que le falta a una app. Puro (sin Android) para probarlo. */
    fun de(h: Hechos): List<Requisito> {
        val r = mutableListOf<Requisito>()
        if (h.usaAccesibilidad) {
            r += Requisito(
                "Accesibilidad", if (h.accesibilidadActiva) EstadoReq.OK else EstadoReq.FALTA, Ajuste.ACCESIBILIDAD,
                "Actívala en Ajustes → Accesibilidad → Apps instaladas.",
            )
        }
        if ("${P}SYSTEM_ALERT_WINDOW" in h.permisos) {
            r += Requisito(
                "Mostrar sobre otras apps", estado(h.superposicion), Ajuste.SUPERPOSICION,
                "Para la burbuja o la pregunta encima de otras apps.",
            )
        }
        for ((perm, info) in PERMISOS) {
            // La ubicación aproximada también sirve.
            val concedido = h.permisos[perm] ?: continue
            val ok = concedido || (perm.endsWith("FINE_LOCATION") && h.permisos["${P}ACCESS_COARSE_LOCATION"] == true)
            r += Requisito(info.first, if (ok) EstadoReq.OK else EstadoReq.FALTA, Ajuste.APP, info.second)
        }
        if (h.necesitaBateria) {
            r += Requisito(
                "Batería sin restricciones", estado(h.bateriaSinRestricciones), Ajuste.BATERIA,
                "Para que Android no la detenga en segundo plano: Ajustes de la app → Batería → Sin restricciones.",
            )
        }
        if (h.permisos.containsKey("${P}WRITE_SECURE_SETTINGS")) {
            r += Requisito(
                "Permiso dado por el PC (adb)", if (h.permisos["${P}WRITE_SECURE_SETTINGS"] == true) EstadoReq.OK else EstadoReq.FALTA,
                Ajuste.ADB, "Opcional: se da una vez desde el PC con adb (el comando está en el README de la app).", opcional = true,
            )
        }
        return r
    }

    private fun estado(b: Boolean?) = when (b) {
        true -> EstadoReq.OK
        false -> EstadoReq.FALTA
        null -> EstadoReq.DESCONOCIDO
    }

    /** Requisitos que faltan y no son opcionales (para la marca «!» en la carta). */
    fun faltan(lista: List<Requisito>): Int = lista.count { it.estado == EstadoReq.FALTA && !it.opcional }
}

/** Depuración USB y opciones de desarrollador del celular. */
data class Depuracion(val opcionesDesarrollador: Boolean, val usb: Boolean, val inalambrica: Boolean) {
    /** Texto para el aviso de la pantalla principal. */
    val aviso: String
        get() = when {
            usb && inalambrica -> "Depuración USB e inalámbrica activas. Sirven para instalar desde el PC; apágalas si no las usas (algunos bancos no abren con la depuración activa)."
            usb -> "Depuración USB activa. Sirve para instalar desde el PC (compilar-todo.bat); apágala si no la usas (algunos bancos no abren con ella activa)."
            inalambrica -> "Depuración inalámbrica activa. Apágala si no la estás usando."
            opcionesDesarrollador -> "Depuración USB apagada. Para instalar desde el PC, actívala en Opciones de desarrollador."
            else -> "Opciones de desarrollador apagadas. Para instalar desde el PC: Ajustes → Acerca del teléfono → toca 7 veces «Número de compilación»."
        }
    val activa: Boolean get() = usb || inalambrica
}
