package com.davidsant7e.mazo.datos

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import com.davidsant7e.mazo.nucleo.Catalogo
import com.davidsant7e.mazo.nucleo.Instalada
import com.davidsant7e.mazo.registro.Registro

/** Lo que El Mazo hace con las otras apps: verlas, abrirlas y pedirles su registro. */
class Apps(private val ctx: Context) {
    private val pm = ctx.packageManager

    /** Las apps de los Arcanos instaladas (Android 11+ solo deja ver las declaradas en <queries>). */
    fun instaladas(): Map<String, Instalada> = Catalogo.paquetes.mapNotNull { p ->
        try {
            val info = pm.getPackageInfo(p, 0)
            p to Instalada(info.versionName, info.lastUpdateTime)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }.toMap()

    fun icono(paquete: String): Drawable? = try {
        pm.getApplicationIcon(paquete)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    /** Abre la app; false si no se pudo. */
    fun abrir(paquete: String): Boolean {
        val i = pm.getLaunchIntentForPackage(paquete) ?: return false
        return iniciar(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), "abrir", paquete)
    }

    /** Abre «Enviar registro» de esa app (todas lo tienen en <paquete>.registro). */
    fun enviarRegistro(paquete: String): Boolean {
        val i = Intent().setClassName(paquete, "$paquete.registro.EnviarRegistroActivity")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return iniciar(i, "registro", paquete)
    }

    /** Ajustes de Android de esa app (permisos, batería, desinstalar). */
    fun ajustes(paquete: String): Boolean = iniciar(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$paquete")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        "ajustes", paquete,
    )

    /** Abre el ajuste de Android que corresponde a un requisito. */
    fun ajuste(paquete: String, a: com.davidsant7e.mazo.nucleo.Ajuste): Boolean {
        val uri = Uri.parse("package:$paquete")
        val i = when (a) {
            com.davidsant7e.mazo.nucleo.Ajuste.ACCESIBILIDAD -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            com.davidsant7e.mazo.nucleo.Ajuste.SUPERPOSICION -> Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
            com.davidsant7e.mazo.nucleo.Ajuste.ADB -> Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            else -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return iniciar(i, "ajuste_" + a.name.lowercase(), paquete) || ajustes(paquete)
    }

    /** Opciones de desarrollador (depuración USB). */
    fun opcionesDesarrollador(): Boolean = iniciar(
        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), "desarrollador", "ajustes",
    ) || iniciar(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), "acerca", "ajustes")

    fun enlace(url: String): Boolean =
        iniciar(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), "enlace", url)

    private fun iniciar(i: Intent, que: String, a: String): Boolean = try {
        ctx.startActivity(i)
        Registro.evento("MAZO", que, "destino" to a)
        true
    } catch (e: ActivityNotFoundException) {
        Registro.error("MAZO", e, "accion" to que, "destino" to a)
        false
    } catch (e: SecurityException) {
        Registro.error("MAZO", e, "accion" to que, "destino" to a)
        false
    }
}
