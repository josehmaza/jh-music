# JH MUSIC — Documentación

Reproductor de música para el radio Android del **Hyundai Tucson ix35**, pensado para usarse mientras se conduce: botones grandes, gestos simples y lectura directa de uno o dos pendrives USB.

- **Versión actual:** 2.0.1 (`versionCode 20`)
- **Paquete Android:** `com.jh.caraudio`
- **APK:** `JH-MUSIC-v<versión>.apk` en la raíz del repositorio

## Documentos

| Documento | Para quién | Contenido |
|---|---|---|
| [01 · Manual de usuario](01-manual-de-usuario.md) | Quien usa la app en el auto | Pantalla, pestañas, búsqueda, reproducción, volumen, pendrives |
| [02 · Arquitectura técnica](02-arquitectura-tecnica.md) | Quien modifica el código | Clases, flujo de datos, escaneo, buscador, cola de reproducción |
| [03 · Compilación y versiones](03-compilacion-y-versiones.md) | Quien genera el APK | Requisitos, comandos, cómo subir la versión, instalación |
| [04 · Historial de cambios](04-historial-de-cambios.md) | Todos | Qué cambió en cada versión |
| [05 · Propuestas de mejora](05-propuestas-de-mejora.md) | Quien planifica | Mejoras y funciones pendientes, priorizadas para uso en el auto y radios de gama baja |
| [06 · Investigación del mezclador](06-investigacion-mezclador.md) | Quien implemente la mezcla | Cómo hacer la transición automática entre canciones según género, formato y bitrate |

## Requisitos del radio

- Android 5.0 o superior (`minSdk 21`).
- Pantalla horizontal (la app se fija en modo apaisado).
- Pendrives montados en rutas tipo `/storage/udisk1`, `/storage/udisk2`, `/storage/usb0`, etc. (plataformas AC8227L, Allwinner, Rockchip, MTK).
- Formatos de audio: MP3, FLAC, WAV, M4A, OGG y AAC.

## Estructura del repositorio

```
jh-car/
├── android/                    Proyecto Android nativo (la app real)
│   └── app/src/main/
│       ├── java/com/jh/caraudio/   Código Java
│       ├── res/                    Layouts, íconos, colores y textos
│       └── assets/                 Copia del simulador web (no la usa la app nativa)
├── index.html, app.js, style.css   Simulador web de la interfaz (prototipo en navegador)
├── capturas/                   Fotos de la app en el radio
├── Documentacion/              Esta documentación
└── JH-MUSIC-v2.0.1.apk         APK listo para instalar
```

El **simulador web** (`index.html` + `app.js` + `style.css`) reproduce la pantalla de 10" (1024×600) en un navegador para probar el diseño sin el radio. La app que se instala en el auto es la nativa de `android/`.
