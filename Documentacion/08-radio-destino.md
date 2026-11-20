# 08 · El radio para el que se desarrolla

JH MUSIC se hace para **un equipo concreto**: el radio Android genérico instalado en un **Hyundai Tucson ix35**. Todo el diseño parte de sus límites. Antes de proponer o revisar un cambio, tener en cuenta esta hoja.

## Ficha

| Qué | Valor | Fuente |
|---|---|---|
| Auto | Hyundai Tucson ix35, **sin botones de control en el volante**: todo se maneja tocando la pantalla | — |
| Pantalla | **1024 × 600** a **160 dpi** (1 dp = 1 px), 60 Hz, ancho de 600 dp; unas 10" | Monitor JH |
| Plataforma | Autochips **AC8227L** (`ac8227l`, el sistema se presenta como `alps 8227L_demo`) | Ajustes y Monitor JH |
| Procesador | 4 núcleos **Cortex-A7** a ~1,3 GHz, 32 bits (ARMv7, "ARMv7 Processor rev 3 (v7l)") | Ajustes y Monitor JH |
| Gráficos | **Mali-400** | Monitor JH |
| Gama | **Baja** | Monitor JH |
| RAM | Ajustes dice **4 GB**; sin confirmar (estos radios suelen mostrar más de lo que tienen; Monitor JH lee la real) | Ajustes |
| Almacenamiento | Ajustes dice **64 GB**; memoria eMMC de unos **35 MB/s** de escritura | Ajustes y Monitor JH |
| Android | Ajustes dice **10.0**, pero el nivel de API es **27 = Android 8.1** (versión falsificada por la fábrica). La huella del sistema dice `6.0/MRA58K`: es un resto del sistema base, no la versión que corre (hay archivos `.vdex`, que solo existen desde Android 8) | Ajustes, Monitor JH y diagnóstico de JH RADIO |
| Apps de fábrica que usan las apps JH | Radio FM `com.jancar.radio` (la maneja [JH RADIO](https://github.com/josehmaza/jh-radio)), servicio `com.jancar.services` (frecuencia de la radio) y `com.jancar.btservice` (audio Bluetooth del teléfono, lo usa [JH Launcher](https://github.com/josehmaza/jh-launcher)) | Diagnóstico de JH RADIO (1/11/2026) |
| Sistema de fábrica | `JCAC10003-OA1-V1.0.22B6-210317_0851` (marzo de 2021), MCU `JM5-00C9` | Ajustes |
| Sonido / Bluetooth | MT6323L / MT6625L | Ajustes |
| Posicionamiento | GPS + AGPS | Ajustes |
| Google | Google Maps 9.49.2 (muy viejo) y Servicios de Google Play 21.06 | Monitor JH |
| Pendrives | Se montan como `/storage/udisk1`, `/storage/udisk2` (también `/mnt/udisk1`, `/storage/usb0` en otros radios) | App |

"Ajustes" es la pantalla del radio **Configuración → Acerca del motor del coche** (fotos del 14/10/2026). "Monitor JH" es la app que lee el hardware real desde el sistema (medido el 24/10/2026).

## Qué implica al desarrollar

- **Diseñar y revisar a 1024 × 600.** Lo que entra en una pantalla más grande puede quedar cortado en el radio (pasó con "Canciones (109)" y los nombres de carpeta). Las demos y la maqueta web se miran a ese tamaño, y la app se prueba en el emulador `radio-tucson` (ver abajo).
- **Manejando y sin volante:** objetivos de toque grandes (filas de 60 dp o más, botones de 46 dp o más), textos de 21 sp en listas y 25 sp en el título, zonas de anterior / play / siguiente amplias, gestos simples. Nada que obligue a leer mucho.
- **Android 8.1 de verdad:** `minSdk 21`, `targetSdk 28`. No usar APIs de Android 9 (API 28) o más nuevas sin comprobar la versión, aunque el radio diga "10".
- **Procesador y gráficos lentos:** cada repintado dibuja la ventana entera, así que las animaciones propias van a ~20 cuadros por segundo en una misma rejilla (`FrameGrid`, desde 2.9.8) y solo mientras hacen falta; nada de desenfoques ni sombras grandes; listas con vistas recicladas; trabajo pesado (escaneo, duraciones, carátulas) en hilos de fondo.
- **El silencio de Android deja mudo el Bluetooth:** en el Tucson, silenciar `STREAM_MUSIC` con `ADJUST_MUTE` (desde JH Launcher) dejó el audio Bluetooth del teléfono sin sonido aunque se subiera el volumen; JH MUSIC seguía sonando (hallazgo del 3/11/2026). Por eso JH Launcher, JH RADIO y JH MUSIC (desde 2.10.1) silencian bajando el volumen a 0 y devolviéndolo, sin `ADJUST_MUTE`. Pendiente de confirmar en el Tucson que así el Bluetooth vuelve a sonar.
- **Memoria lenta:** no escribir en la memoria mientras suena (por eso la última canción se guarda solo al empezar cada una, sin el segundo).
- **El tema de Android del radio** pinta de naranja el resaltado al tocar (`selectableItemBackground`): usar fondos propios (`bg_pressed_cyan`).
- **Las animaciones del sistema pueden estar desactivadas** en el radio: las animaciones que importan se hacen a mano, no con `Animator`.

## Emulador que imita al radio: `radio-tucson`

Para probar sin ir al auto hay un emulador de Android configurado lo más parecido posible al radio:

| Qué | Emulador `radio-tucson` | Radio |
|---|---|---|
| Android | **8.1 (API 27)** | 8.1 (API 27), aunque diga 10 |
| Pantalla | **1024 × 600 a 160 dpi**, horizontal | Igual |
| Procesador | 4 núcleos, 32 bits (x86) | 4 × Cortex-A7, 32 bits (ARM) |
| RAM | 2 GB | Sin confirmar (Ajustes dice 4 GB) |
| Otros | GPS, audio, sin batería ni cámaras, sin Play Store | — |

Imagen `system-images;android-27;default;x86`.

**Cómo abrirlo, instalar el APK, simular los pendrives y medir:** ver [10 · Emulador y ADB](10-emulador-y-adb.md), la única guía de prueba.

**Lo que el emulador no imita:** la velocidad real del radio (corre en la CPU de la PC, mucho más rápida), la GPU Mali-400, la memoria lenta, los decodificadores de audio del radio ni el tema de Android de la fábrica. Por eso lo lento o lo que depende del formato de los archivos se confirma en el auto.

# Especificaciones ideales al generar o exportar videos
No tiene sentido meter archivos en 1080p o 4K en un panel de 1024 × 600; solo gastarás espacio y forzarás el decodificador de video del hardware.

Resolución exacta: 1024 × 576 (16:9 estándar) o 1024 × 600 (si quieres llenar el panel de borde a borde sin barras negras).

Códec de video: H.264 (AVC) Baseline o Main Profile. Los chips de las radios de auto suelen tener decodificación por hardware dedicada para H.264, pero sufren o tiran de CPU (calentándose) con H.265 (HEVC) o AV1.

Bitrate (tasa de bits): 1.5 a 2.5 Mbps (VBR). Es el punto dulce donde no notarás compresión en 10 pulgadas y los archivos pesarán muy poco (~12–18 MB por minuto).

Fotogramas por segundo (FPS): 30 fps (o 24/25 fps si son visualizadores o clips cinemáticos). Subir a 60 fps en video duplica el trabajo de lectura sin aportar mejora real en ese panel.

Audio: AAC-LC a 192 kbps / 44.1 kHz (estéreo). Evita pistas en FLAC o AC3 dentro del contenedor MP4 para no depender de decodificadores por software.

Contenedor: .mp4 con la bandera -movflags +faststart para que arranquen al instante al cambiar de canción.
