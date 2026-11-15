package com.davidsant7e.mazo.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.davidsant7e.mazo.datos.Apps
import com.davidsant7e.mazo.nucleo.Catalogo
import com.davidsant7e.mazo.nucleo.Instalada

class MainActivity : ComponentActivity() {
    private val apps by lazy { Apps(this) }
    private var instaladas by mutableStateOf<Map<String, Instalada>>(emptyMap())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaMazo {
                Mazo(
                    vista = Catalogo.vista(instaladas),
                    instaladas = instaladas,
                    icono = apps::icono,
                    acciones = Acciones(
                        abrir = { avisarSi(!apps.abrir(it), "No se pudo abrir la app.") },
                        registro = { avisarSi(!apps.enviarRegistro(it), "Esta versión aún no tiene «Enviar registro»: actualízala desde el PC.") },
                        ajustes = { avisarSi(!apps.ajustes(it), "No se pudieron abrir los ajustes.") },
                        enlace = { avisarSi(!apps.enlace(it), "No hay un navegador para abrir el enlace.") },
                    ),
                )
            }
        }
    }

    /** Al volver (por ejemplo, después de instalar o desinstalar una app), se revisa de nuevo. */
    override fun onResume() {
        super.onResume()
        instaladas = apps.instaladas()
    }

    private fun avisarSi(condicion: Boolean, texto: String) {
        if (condicion) Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}

/** Lo que la pantalla le pide a la actividad (todo recibe el paquete, salvo enlace que recibe la URL). */
class Acciones(
    val abrir: (String) -> Unit,
    val registro: (String) -> Unit,
    val ajustes: (String) -> Unit,
    val enlace: (String) -> Unit,
)
