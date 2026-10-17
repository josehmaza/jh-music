# Historial de cambios — JH MUSIC

Versiones de la más nueva a la más antigua. Entre paréntesis, el commit de git.

## 2.3.0 — 2026-10-16

- **Cuadro de la carátula como la web:** un haz láser cian gira por el borde mientras suena; al mezclar se suma un haz magenta en sentido contrario. Abajo, una cápsula `PAUSED / PLAYING / MIXING ⇄` con ecualizador.
- **Doble toque en Siguiente/Anterior** (y en sus tarjetas): adelanta o retrocede 10 s, con un aviso `+10s` / `-10s` centrado en el reproductor.
- **Tarjetas Anterior/Siguiente:** íconos ⏮ ⏭ en los extremos y nombre más grande, en gris claro.
- **Título largo:** se desplaza como en la web (inicio, deslizamiento, final y vuelta), también en las tarjetas.
- **Tiempos** de la canción más grandes y en blanco.
- **Automix** al lado de la lupa; la lupa queda resaltada con el explorador oculto.
- Al mezclar, los datos de la canción nueva aparecen al instante y solo la carátula hace fundido.

## 2.2.0 — 2026-10-16 (`eeba62b`)

- **Automix inteligente:** analiza dónde se apaga cada canción para mezclar ahí, ignora los silencios y adapta la duración (final seco → 3 s).
- Siguiente y Anterior con fundido de 0,5 s y uso de la canción ya precargada.

## 2.1.0 — 2026-10-16 (`c8f9063`)

- **Automix** (fase 1): mezcla de 5 s entre canciones con un botón bajo la lupa.
- **Favoritos** guardados en el radio; **duración real** en la lista.

## 2.0.1 — 2026-10-16 (`f2707d6`)

- **Siguiente y Anterior en Favoritos** recorren los favoritos, no la carpeta de la canción.
- **Cola de reproducción independiente:** abrir otra carpeta mientras suena música ya no cambia lo que viene después.
- **Detección real de pendrives:** un USB solo cuenta como conectado si tiene contenido. Ya no aparece una raíz `USB 2` vacía al quitarlo, y con un solo pendrive no se muestran los chips USB 1 / USB 2.
- **Re-escaneo automático** al conectar o quitar un pendrive.
- Al re-escanear se conservan los favoritos, se limpia la carpeta activa y se detiene la reproducción si se quitó su pendrive.
- La canción que suena se marca por archivo: ya no queda marcada otra fila al abrir otra carpeta.

## 2.0 — 2026-10-16 (`f19f9c9`)

- El número de **Pistas (N)** es siempre el de la carpeta activa; las búsquedas no lo cambian.
- **Tocar una carpeta** en el árbol solo la expande o contrae.
- La **X** del buscador conserva el chip y muestra la carpeta activa.
- La **búsqueda de Pistas se conserva** al cambiar de pestaña y se descarta al abrir otra carpeta con `[N ▶]`.
- Al abrir Pistas sin búsqueda pendiente se elige el chip de la carpeta activa.

## 1.9.8 — 2026-10-16 (`4c7064f`, `49bdb1d`)

- **Nuevo nombre:** JH MUSIC.
- **Nuevo ícono:** letra H con ecualizador, adaptativo en Android 8+.
- **Mute:** lee el estado real del sistema y restaura el porcentaje al quitarlo.
- **Botón `[N ▶]`:** abre solo las canciones directas de la carpeta, igual que su contador.
- La **lupa** vuelve a abrir y cerrar el explorador con un toque.
- El texto del buscador cambia según la pestaña.
- **Reproducir Todo** oculto en Carpetas USB.
- **Pistas** empieza en 0 hasta elegir carpeta o buscar.
- El APK se genera con la versión en el nombre (`JH-MUSIC-v1.9.8.apk`) y se copia solo a la raíz.

## 1.9.7 — 2026-10-16 (`18b5438`)

- **Buscador mucho más rápido con dos pendrives:**
  - puntaje calculado una vez por resultado;
  - texto normalizado precalculado al escanear;
  - regex precompilados;
  - ruta relativa al pendrive en el índice;
  - espera adaptativa al escribir (500/250 ms).

## 1.9.6 y anteriores — 2026-10-15 / 16

- Explorador a la izquierda y reproductor a la derecha (`f1b36bc`).
- Lupa ampliada, zona táctil ergonómica y gesto de jalar para mostrar u ocultar el explorador (`5f446be`).
- Textos más grandes en pestañas, chips y buscador (`feee464`).
- Barra de progreso separada de las tarjetas Anterior / Siguiente para evitar toques accidentales (`52a79aa`).
- Un toque expande el nombre completo y doble toque reproduce (`8e1e6b8`, `dd52a0a`).
- Toque en la carátula para pausar o reproducir (`7c87530`).
- Botón HOME en pantalla para salir al launcher (`21c6429`).
- Chip de carpeta activa con su USB (`96d833a`) y carpeta fijada al reproducir desde una búsqueda (`27bf3b9`).
- Indicador "Buscando…" con spinner (`62bbda2`).
- Resultados ordenados por relevancia (`81b9573`).
- Bitrate real del audio, por ejemplo `MP3 320K` (`cd10254`).
- Barra de volumen vertical extendida, con porcentaje entre `+` y `−` (`b3badea`, `4ea4f58`).

## 1.4.x — 2026-10-15

- Filtros de búsqueda: Esta Carpeta, TODO, USB 1, USB 2 (`24908d4`).
- La búsqueda en Carpetas USB busca solo carpetas (`3ee158f`).
- Teclado que se oculta al tocar la pantalla.

## 1.3.0 — 2026-10-15 (`75ed777`)

- Búsqueda por pestaña, cola por carpeta, íconos grandes e ícono de launcher.

## 1.2.0 — 2026-10-15 (`b43da1e`)

- Primera versión: reproductor para Hyundai Tucson ix35 con soporte para varios USB e interfaz optimizada para conducir.
