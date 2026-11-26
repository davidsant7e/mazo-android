<p align="center"><img src="docs/carta.svg" alt="0 · El Loco — El Mazo" width="180"></p>

# 0 · El Loco — El Mazo

Un solo ícono en el celular para todas las apps de los Arcanos. El Loco es la carta que lleva el mazo completo.

## Qué hace (versión 0.2)

- **En este celular**: las apps de los Arcanos instaladas, como cartas del tarot (número romano, ícono de la app y nombre). **Tocar** abre la app.
- **Mantener presionada una carta**: versión y fecha de la última actualización, **Enviar registro** de esa app (el de `claude/registros.md`), **Ajustes de la app** (permisos, batería, desinstalar) y **Ver en GitHub**.
- **Sin instalar**: las apps Android del mazo que faltan en este celular; si el repo es público y compila en GitHub, **Descargar APK**; si no, se instala desde el PC (`C:\Git\compilar-todo.bat` → I).
- **En otras plataformas**: los proyectos del PC, VS Code y la web (La Rueda, La Justicia, El Sol, El Mundo, El Ermitaño).
- **Cartas libres**: los números que aún no tienen proyecto.
- **Permisos de cada app**: al mantener presionada una carta se ve qué tiene y qué le falta (accesibilidad, «Mostrar sobre otras apps», notificaciones, ubicación, micrófono, Bluetooth, cámara, batería sin restricciones y el permiso opcional que se da con adb), con un botón **Abrir** que lleva al ajuste exacto. Si le falta algo, la carta muestra **«!»**. Lo que Android no deja ver desde otra app sale con «?».
- **Depuración USB**: aviso arriba si la depuración USB o la inalámbrica están activas (útil para instalar desde el PC; algunos bancos no abren con ella activa) o apagadas, con botón a Opciones de desarrollador.
- **Versiones**: al abrir (y con «Buscar versiones») compara cada app con el último cambio de su repo en GitHub. Si hay cambios después de instalarla, la carta muestra **«↑»** y el detalle dice qué cambió y cómo actualizar (APK o desde el PC). Los repos privados se actualizan desde el PC.
- Se actualiza solo al volver a la app (por ejemplo, después de instalar o desinstalar algo).
- Tiene su propio registro y atajo «Enviar registro».

## Lo que sigue (ideas anotadas)

- **El Mazo en el reloj** (si llega un reloj con Wear OS, o con la app propia del reloj HryFine): ver las cartas en la muñeca.
- **Todas las notificaciones de los Arcanos al reloj**: un canal común para que el reloj las reenvíe.
- Aviso «esta app tiene errores en su registro».
- Ajustes compartidos entre apps (clave de Gemini una sola vez, ciudad, idioma).
- Burbuja flotante con el mazo sobre cualquier app.

## Agregar una carta nueva

Editar `nucleo/Catalogo.kt` (número, nombre, proyecto, paquete, repo, APK, batería) y, si es Android, agregar su paquete en `<queries>` del manifiesto (Android 11+ solo deja ver las apps nombradas ahí). Agregar también su repo en **`repos.txt`**: `C:\Git\actualizar-todo.bat` lo lee para clonar en el PC los proyectos que falten. La prueba `CatalogoTest` revisa que estén las 22 cartas en orden y sin paquetes repetidos.

## Instalar

**Sin el PC**: cada cambio se compila en GitHub y el APK queda en [releases/ultima](https://github.com/el-ermitanho/mazo-android/releases/tag/ultima) → descargar `el-mazo.apk` en el celular.

**Con el PC**: Android Studio → abrir la carpeta → Run, o `C:\Git\compilar-todo.bat` → I.

## Cómo está hecho

Kotlin + Jetpack Compose, minSdk 26, mismo esqueleto que las demás apps. `nucleo/Catalogo.kt` es puro (con pruebas); `datos/Apps.kt` habla con el PackageManager; `ui/` la pantalla de cartas. Sin permisos especiales: solo `<queries>` con los paquetes de los Arcanos.

Parte del mazo de [el-ermitanho](https://github.com/el-ermitanho).
