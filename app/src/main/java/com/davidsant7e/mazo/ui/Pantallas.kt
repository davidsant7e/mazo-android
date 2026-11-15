package com.davidsant7e.mazo.ui

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.davidsant7e.mazo.nucleo.Carta
import com.davidsant7e.mazo.nucleo.Instalada
import com.davidsant7e.mazo.nucleo.Vista
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Mazo(
    vista: Vista,
    instaladas: Map<String, Instalada>,
    icono: (String) -> Drawable?,
    acciones: Acciones,
) {
    var elegida by remember { mutableStateOf<Carta?>(null) }
    var verLibres by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("0 · El Mazo") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulNoche, titleContentColor = Durazno),
            )
        },
    ) { pad ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 104.dp),
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            titulo("En este celular · ${vista.enEsteCelular.size}")
            if (vista.enEsteCelular.isEmpty()) {
                texto("Todavía no hay apps de los Arcanos instaladas. Instálalas desde el PC con compilar-todo.bat.")
            }
            items(vista.enEsteCelular, key = { it.numero }) { c ->
                CartaApp(
                    carta = c,
                    icono = c.paquete?.let(icono),
                    alTocar = { c.paquete?.let(acciones.abrir) },
                    alMantener = { elegida = c },
                )
            }
            texto("Toca una carta para abrirla; mantenla presionada para enviar su registro, ver la versión o sus ajustes.")

            if (vista.sinInstalar.isNotEmpty()) {
                titulo("Sin instalar · ${vista.sinInstalar.size}")
                items(vista.sinInstalar, key = { it.numero }) { c ->
                    CartaApp(carta = c, icono = null, apagada = true, alTocar = { elegida = c }, alMantener = { elegida = c })
                }
            }

            titulo("En otras plataformas · ${vista.otrasPlataformas.size}")
            items(vista.otrasPlataformas, key = { it.numero }) { c ->
                CartaApp(carta = c, icono = null, apagada = true, alTocar = { elegida = c }, alMantener = { elegida = c })
            }

            item(span = { GridItemSpan(maxLineSpan) }, key = "libres") {
                TextButton(onClick = { verLibres = !verLibres }) {
                    Text((if (verLibres) "▾ " else "▸ ") + "Cartas libres · ${vista.libres.size}")
                }
            }
            if (verLibres) {
                items(vista.libres, key = { it.numero }) { c ->
                    CartaApp(carta = c, icono = null, apagada = true, alTocar = {}, alMantener = {})
                }
            }
        }
    }

    elegida?.let { c -> DialogoCarta(c, c.paquete?.let { instaladas[it] }, acciones) { elegida = null } }
}

private fun LazyGridScope.titulo(t: String) {
    item(span = { GridItemSpan(maxLineSpan) }, key = "t-$t") {
        Text(t, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    }
}

private fun LazyGridScope.texto(t: String) {
    item(span = { GridItemSpan(maxLineSpan) }, key = "x-$t") {
        Text(t, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Una carta del tarot: número romano arriba, ícono de la app (o el número grande) y el nombre abajo. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CartaApp(carta: Carta, icono: Drawable?, apagada: Boolean = false, alTocar: () -> Unit, alMantener: () -> Unit) {
    val fondo = if (apagada) MaterialTheme.colorScheme.surfaceVariant else AzulNoche
    val tinta = if (apagada) MaterialTheme.colorScheme.onSurfaceVariant else Durazno
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.62f)
            .background(fondo, RoundedCornerShape(12.dp))
            .border(1.dp, tinta.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .combinedClickable(onClick = alTocar, onLongClick = alMantener, onLongClickLabel = "Opciones")
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(carta.romano, color = tinta, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 2.sp)
        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            val bmp = remember(icono) { icono?.toBitmap(144, 144)?.asImageBitmap() }
            if (bmp != null) {
                Image(bmp, contentDescription = null, Modifier.size(56.dp))
            } else {
                Text(carta.romano, color = tinta.copy(alpha = 0.5f), fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                carta.nombre.uppercase(), color = tinta, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, maxLines = 2, letterSpacing = 1.sp, lineHeight = 12.sp,
            )
            if (carta.proyecto.isNotBlank() && carta.proyecto != carta.nombre) {
                Text(
                    carta.proyecto, color = if (apagada) tinta else Color.White, fontSize = 10.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

@Composable
private fun DialogoCarta(c: Carta, instalada: Instalada?, a: Acciones, cerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = cerrar,
        title = { Text(c.titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (c.proyecto.isNotBlank()) Text(c.proyecto, fontWeight = FontWeight.Bold)
                if (c.descripcion.isNotBlank()) Text(c.descripcion)
                c.donde?.let { Text("Dónde: $it", style = MaterialTheme.typography.bodySmall) }
                if (instalada != null) {
                    Text(
                        "Versión ${instalada.version ?: "?"} · actualizada el ${fecha.format(Date(instalada.actualizada))}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else if (c.paquete != null) {
                    Text(
                        if (c.apk != null) "No está instalada. Puedes bajar el APK de GitHub o instalarla desde el PC."
                        else "No está instalada. Se instala desde el PC: compilar-todo.bat → I.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(Modifier.height(4.dp))
                val p = c.paquete
                if (p != null && instalada != null) {
                    Boton("Abrir") { a.abrir(p); cerrar() }
                    Boton("Enviar registro") { a.registro(p); cerrar() }
                    Boton("Ajustes de la app") { a.ajustes(p); cerrar() }
                }
                if (instalada == null) c.apk?.let { url -> Boton("Descargar APK") { a.enlace(url); cerrar() } }
                c.repo?.let { url -> Boton("Ver en GitHub") { a.enlace(url); cerrar() } }
            }
        },
        confirmButton = { TextButton(onClick = cerrar) { Text("Cerrar") } },
    )
}

@Composable
private fun Boton(texto: String, alTocar: () -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        TextButton(onClick = alTocar) { Text(texto) }
        Spacer(Modifier.width(1.dp))
    }
}
