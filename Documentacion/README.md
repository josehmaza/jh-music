# JH MUSIC — Documentación

Reproductor de música para el radio Android del **Hyundai Tucson ix35** (sin botones en el volante), pensado para usarse mientras se conduce: botones y textos grandes, zonas de toque amplias (anterior / play / siguiente responden en toda la franja de abajo), gestos simples (doble toque en la carátula, volumen deslizando el dedo), lectura directa de uno o dos pendrives USB, **Mi Mix** (una lista que armas y suena seguida) y **Automix**, que mezcla las canciones e iguala su volumen. Al encender el auto abre en la última canción.

- **Versión actual:** 2.9.6 (`versionCode 37`), pendiente de probar en el auto (ver [historial](04-historial-de-cambios.md)).
- **Paquete Android:** `com.jh.caraudio`
- **APK:** `JH-MUSIC-v<versión>.apk` en la carpeta `jh-music/`

## Documentos

| Documento | Para quién | Contenido |
|---|---|---|
| [01 · Manual de usuario](01-manual-de-usuario.md) | Quien usa la app en el auto | Pantalla, pestañas, búsqueda, reproducción, menú ☰, Automix, volumen y gesto, pendrives, última canción |
| [02 · Arquitectura técnica](02-arquitectura-tecnica.md) | Quien modifica el código | Clases, flujo de datos, escaneo, buscador, cola de reproducción, gestos, menú ☰, zonas de toque, gesto de volumen, maqueta web |
| [03 · Compilación y versiones](03-compilacion-y-versiones.md) | Quien genera el APK | Requisitos, comandos, cómo subir la versión, instalación |
| [04 · Historial de cambios](04-historial-de-cambios.md) | Todos | Qué cambió en cada versión |
| [05 · Propuestas de mejora](05-propuestas-de-mejora.md) | Quien planifica | Mejoras y funciones pendientes, priorizadas para uso en el auto y radios de gama baja |
| [06 · Investigación del mezclador](06-investigacion-mezclador.md) | Quien modifique Automix | Cómo mezclar entre canciones según género, formato y bitrate, cómo igualar el volumen, y qué está implementado |
| [07 · Biblioteca y carátulas](07-biblioteca-y-caratulas.md) | Quien prepara la música del pendrive | Carpetas y nombres de `D:\musica`, carátulas oficiales y JH MUSIC (con las zonas que la app tapa), VirtualDJ y copias de seguridad |
| [08 · El radio destino](08-radio-destino.md) | Quien diseña o programa | Ficha del radio del Tucson ix35 (pantalla 1024 × 600 a 160 dpi, AC8227L, Android 8.1 que dice ser 10) y qué implica al desarrollar |

## Requisitos del radio

El radio para el que se desarrolla está descrito en [08 · El radio destino](08-radio-destino.md). La app en sí pide:

- Android 5.0 o superior (`minSdk 21`).
- Pantalla horizontal (la app se fija en modo apaisado).
- Pendrives montados en rutas tipo `/storage/udisk1`, `/storage/udisk2`, `/storage/usb0`, etc. (plataformas AC8227L, Allwinner, Rockchip, MTK).
- Formatos de audio: MP3, FLAC, WAV, M4A, OGG y AAC.

## Estructura del repositorio

```
jh-music/
├── android/                    Proyecto Android nativo (la app real)
│   └── app/src/main/
│       ├── java/com/jh/caraudio/   Código Java
│       ├── res/                    Layouts, íconos, colores y textos
│       └── assets/                 Copia del simulador web (no la usa la app nativa)
├── index.html, app.js, style.css   Simulador web de la interfaz (prototipo en navegador)
├── build.sh                    Compila el APK (Git Bash)
├── Documentacion/              Esta documentación
└── JH-MUSIC-v<versión>.apk     APK listo para instalar (lo genera build.sh; no se versiona)
```

JH VIDEO, la app de videos, está en su propio repositorio: [josehmaza/jh-video](https://github.com/josehmaza/jh-video).

El **simulador web** (`index.html` + `app.js` + `style.css`) reproduce la pantalla de 10" (1024×600) en un navegador para probar el diseño sin el radio. La app que se instala en el auto es la nativa de `android/`.
