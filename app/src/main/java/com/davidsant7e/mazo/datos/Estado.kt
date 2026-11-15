package com.davidsant7e.mazo.datos

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.davidsant7e.mazo.nucleo.Catalogo
import com.davidsant7e.mazo.nucleo.Depuracion
import com.davidsant7e.mazo.nucleo.Hechos
import com.davidsant7e.mazo.nucleo.Requisito
import com.davidsant7e.mazo.nucleo.Requisitos
import com.davidsant7e.mazo.registro.Registro

/** Lee de Android el estado de permisos de las otras apps y la depuración USB. */
class Estado(private val ctx: Context) {
    private val pm = ctx.packageManager

    fun depuracion(): Depuracion {
        val r = ctx.contentResolver
        fun global(nombre: String) = runCatching { Settings.Global.getInt(r, nombre, 0) == 1 }.getOrDefault(false)
        return Depuracion(
            opcionesDesarrollador = global(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED),
            usb = global(Settings.Global.ADB_ENABLED),
            inalambrica = global("adb_wifi_enabled"),
        )
    }

    /** Requisitos de cada app instalada de los Arcanos. */
    fun requisitos(instaladas: Set<String>): Map<String, List<Requisito>> = instaladas.associateWith { p ->
        try {
            Requisitos.de(hechos(p))
        } catch (e: Exception) {
            Registro.error("PERMISOS", e, "app" to p)
            emptyList()
        }
    }

    @Suppress("DEPRECATION")
    private fun hechos(p: String): Hechos {
        val info: PackageInfo = pm.getPackageInfo(p, PackageManager.GET_PERMISSIONS or PackageManager.GET_SERVICES)
        val pedidos = info.requestedPermissions.orEmpty()
        val flags = info.requestedPermissionsFlags ?: IntArray(pedidos.size)
        val permisos = pedidos.indices.associate { i ->
            pedidos[i] to ((flags.getOrElse(i) { 0 } and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0)
        }
        val usaAccesibilidad = info.services.orEmpty().any { it.permission == Manifest.permission.BIND_ACCESSIBILITY_SERVICE }
        val activos = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        return Hechos(
            permisos = permisos,
            usaAccesibilidad = usaAccesibilidad,
            accesibilidadActiva = activos.split(':').any { it.startsWith("$p/") },
            superposicion = if (Manifest.permission.SYSTEM_ALERT_WINDOW in permisos) superposicion(p, info) else null,
            bateriaSinRestricciones = runCatching { ctx.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(p) }.getOrNull(),
            necesitaBateria = Catalogo.porPaquete(p)?.bateria == true,
        )
    }

    /** «Mostrar sobre otras apps» de otra app; null si Android no deja saberlo. */
    @Suppress("DEPRECATION")
    private fun superposicion(p: String, info: PackageInfo): Boolean? = runCatching {
        val ops = ctx.getSystemService(AppOpsManager::class.java)
        val uid = info.applicationInfo?.uid ?: return null
        val modo = if (Build.VERSION.SDK_INT >= 29) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, uid, p)
        } else {
            ops.checkOpNoThrow(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, uid, p)
        }
        when (modo) {
            AppOpsManager.MODE_ALLOWED -> true
            AppOpsManager.MODE_IGNORED, AppOpsManager.MODE_ERRORED -> false
            else -> null
        }
    }.getOrNull()
}
