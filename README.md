# JH MUSIC

Reproductor de música para el radio Android del **Hyundai Tucson ix35** (pantalla de 1024 × 600, sin botones en el volante; ficha completa del equipo en [Documentacion/08-radio-destino.md](Documentacion/08-radio-destino.md)), pensado para usarse mientras se conduce: botones y textos grandes, zonas de toque amplias, gestos simples y lectura directa de uno o dos pendrives USB.

- **Mi Mix:** una lista que armas con `+` y suena seguida.
- **Automix:** mezcla las canciones e iguala su volumen.
- Al encender el auto abre en la última canción, en pausa.
- Se controla desde [JH Launcher](https://github.com/josehmaza/jh-launcher), la pantalla de inicio: canción, ⏮ ▶ ⏭ y barra de tiempo.

Versión actual: **2.13.0** · [Releases](https://github.com/josehmaza/jh-music/releases) (APK listo para instalar).

## Contenido

| Carpeta / archivo | Qué es |
|---|---|
| `android/` | La app nativa (Java 8, minSdk 21, Gradle Wrapper) |
| `index.html`, `app.js`, `style.css` | Maqueta web: la misma pantalla en un navegador, para probar el diseño |
| `Documentacion/` | Manual de usuario, arquitectura, compilación, historial, propuestas y la ficha del radio |
| `build.sh` | Compila el APK y lo deja en esta carpeta |

## Compilar y Probar en Emulador

```bash
bash build.sh
```

Requiere JDK 11. Detalles en [Documentacion/03-compilacion-y-versiones.md](Documentacion/03-compilacion-y-versiones.md).

- **Probar en el emulador (1024 × 600 px):** ejecutar el script automatizado [probar-music.bat](file:///d:/workspaces/general/jh-music/probar-music.bat) (doble clic o `cmd.exe //c probar-music.bat`). Inicia ADB, lanza el emulador visible (`radio-tucson`), enlaza el pendrive virtual USB e instala automáticamente el último `JH-MUSIC-v*.apk`.
- **Medición avanzada y comandos manuales:** seguir [Documentacion/10-emulador-y-adb.md](Documentacion/10-emulador-y-adb.md). Explica cómo abrir el emulador rápido, enlazar los pendrives, instalar el APK y medir con ADB.

## Documentación

Empieza por [Documentacion/README.md](Documentacion/README.md).
