package com.davidsant7e.mazo.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.davidsant7e.mazo.datos.Apps
import com.davidsant7e.mazo.datos.Estado
import com.davidsant7e.mazo.datos.GitHub
import com.davidsant7e.mazo.nucleo.Ajuste
import com.davidsant7e.mazo.nucleo.Catalogo
import com.davidsant7e.mazo.nucleo.Comparacion
import com.davidsant7e.mazo.nucleo.Depuracion
import com.davidsant7e.mazo.nucleo.Instalada
import com.davidsant7e.mazo.nucleo.Requisito
import com.davidsant7e.mazo.nucleo.Versiones
import com.davidsant7e.mazo.registro.Registro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val apps by lazy { Apps(this) }
    private val estado by lazy { Estado(this) }

    private var instaladas by mutableStateOf<Map<String, Instalada>>(emptyMap())
    private var requisitos by mutableStateOf<Map<String, List<Requisito>>>(emptyMap())
    private var depuracion by mutableStateOf(Depuracion(false, false, false))
    private var versiones by mutableStateOf<Map<String, Comparacion>>(emptyMap())
    private var buscando by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaMazo {
                Mazo(
                    datos = DatosMazo(Catalogo.vista(instaladas), instaladas, requisitos, depuracion, versiones, buscando),
                    icono = apps::icono,
                    acciones = Acciones(
                        abrir = { avisarSi(!apps.abrir(it), "No se pudo abrir la app.") },
                        registro = { avisarSi(!apps.enviarRegistro(it), "Esta versión aún no tiene «Enviar registro»: actualízala desde el PC.") },
                        ajustes = { avisarSi(!apps.ajustes(it), "No se pudieron abrir los ajustes.") },
                        ajuste = { p, a -> avisarSi(!apps.ajuste(p, a), "No se pudo abrir ese ajuste.") },
                        desarrollador = { avisarSi(!apps.opcionesDesarrollador(), "No se pudieron abrir las opciones de desarrollador.") },
                        enlace = { avisarSi(!apps.enlace(it), "No hay un navegador para abrir el enlace.") },
                        buscarVersiones = ::buscarVersiones,
                    ),
                )
            }
        }
        if (savedInstanceState == null) buscarVersiones()
    }

    /** Al volver (después de dar un permiso, instalar o desinstalar), se revisa todo de nuevo. */
    override fun onResume() {
        super.onResume()
        instaladas = apps.instaladas()
        requisitos = estado.requisitos(instaladas.keys)
        depuracion = estado.depuracion()
        Registro.evento(
            "MAZO", "revision", "apps" to instaladas.size,
            "con_faltantes" to requisitos.count { (_, r) -> com.davidsant7e.mazo.nucleo.Requisitos.faltan(r) > 0 },
            "depuracion_usb" to depuracion.usb,
        )
    }

    /** Compara cada app instalada con el último cambio de su repo público en GitHub. */
    private fun buscarVersiones() {
        if (buscando) return
        buscando = true
        lifecycleScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                val ahora = apps.instaladas()
                Catalogo.cartas.filter { it.paquete != null && it.paquete in ahora }.associate { c ->
                    val inst = ahora.getValue(c.paquete!!)
                    val comp = if (c.repo == null) {
                        Comparacion.SinDato("Repo privado: actualízala desde el PC")
                    } else {
                        val r = GitHub.ultimoCambio(c.repo)
                        Versiones.comparar(inst.actualizada, r.cambio, r.motivo)
                    }
                    c.paquete to comp
                }
            }
            versiones = resultado
            buscando = false
            Registro.evento("MAZO", "versiones", "con_cambios" to resultado.values.count { it is Comparacion.HayCambios })
        }
    }

    private fun avisarSi(condicion: Boolean, texto: String) {
        if (condicion) Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}

/** Todo lo que la pantalla muestra. */
class DatosMazo(
    val vista: com.davidsant7e.mazo.nucleo.Vista,
    val instaladas: Map<String, Instalada>,
    val requisitos: Map<String, List<Requisito>>,
    val depuracion: Depuracion,
    val versiones: Map<String, Comparacion>,
    val buscando: Boolean,
)

/** Lo que la pantalla le pide a la actividad (todo recibe el paquete, salvo enlace que recibe la URL). */
class Acciones(
    val abrir: (String) -> Unit,
    val registro: (String) -> Unit,
    val ajustes: (String) -> Unit,
    val ajuste: (String, Ajuste) -> Unit,
    val desarrollador: () -> Unit,
    val enlace: (String) -> Unit,
    val buscarVersiones: () -> Unit,
)
