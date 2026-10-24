# JH MUSIC

Reproductor de música para el radio Android del **Hyundai Tucson ix35** (pantalla de 1024 × 600, sin botones en el volante; ficha completa del equipo en [Documentacion/08-radio-destino.md](Documentacion/08-radio-destino.md)), pensado para usarse mientras se conduce: botones y textos grandes, zonas de toque amplias, gestos simples y lectura directa de uno o dos pendrives USB.

- **Mi Mix:** una lista que armas con `+` y suena seguida.
- **Automix:** mezcla las canciones e iguala su volumen.
- Al encender el auto abre en la última canción, en pausa.

Versión actual: **2.9.7**.

## Contenido

| Carpeta / archivo | Qué es |
|---|---|
| `android/` | La app nativa (Java 8, minSdk 21, Gradle Wrapper) |
| `index.html`, `app.js`, `style.css` | Maqueta web: la misma pantalla en un navegador, para probar el diseño |
| `Documentacion/` | Manual de usuario, arquitectura, compilación, historial, propuestas y la ficha del radio |
| `build.sh` | Compila el APK y lo deja en esta carpeta |

## Compilar

```bash
bash build.sh
```

Requiere JDK 11. Detalles en [Documentacion/03-compilacion-y-versiones.md](Documentacion/03-compilacion-y-versiones.md).

## Documentación

Empieza por [Documentacion/README.md](Documentacion/README.md).
