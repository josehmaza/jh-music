# Arquitectura técnica — JH MUSIC

App Android nativa en Java, sin librerías externas: una sola `Activity`, adapters de `ListView` y vistas propias dibujadas con `Canvas`. El audio se reproduce con `MediaPlayer` del sistema.

| Dato | Valor |
|---|---|
| Lenguaje | Java 8 |
| `minSdk` / `targetSdk` / `compileSdk` | 21 / 28 / 33 |
| Dependencias | Ninguna (`dependencies {}` vacío) |
| Paquete | `com.jh.caraudio` |

## 1. Clases

Todas están en `android/app/src/main/java/com/jh/caraudio/`.

| Clase | Responsabilidad |
|---|---|
| `MainActivity` | Toda la lógica: escaneo USB, árbol de carpetas, pestañas, buscador, cola y reproducción, volumen, gestos y teclas multimedia |
| `Track` | Una canción: ruta, título, artista, álbum, carpeta, pendrive, formato, duración (0 = desconocida), favorito. Guarda su `SearchKey` |
| `FolderItem` | Carpeta agrupada por nombre (resultados de búsqueda en la pestaña Carpetas). Guarda su `SearchKey` |
| `TreeNode` | Nodo del árbol de Carpetas: hijos, contador y lista de canciones directas |
| `SearchText` | Utilidades del buscador con regex precompilados: normalizar, colapsar letras repetidas, rutas relativas |
| `AutoMixer` | Automix: precarga de la siguiente canción y mezcla de igual potencia entre dos `MediaPlayer` |
| `AudioAnalyzer` | Analiza inicio y final de cada canción (silencios y fade-out) para que Automix mezcle en el lugar correcto |
| `TrackAdapter` | Lista de canciones: resaltado de la que suena, toque / doble toque, favoritos, índice A-Z |
| `FolderTreeAdapter` | Filas del árbol de Carpetas con el botón `[N ▶]` |
| `FolderAdapter` | Lista plana de carpetas (resultados de búsqueda en Carpetas) |
| `VerticalVolumeBar` | Barra de volumen vertical dibujada a mano |
| `SearchSpinnerView` | Spinner de "Buscando…" que no depende de las animaciones del sistema |
| `SoundWaveBarsView` | Barras animadas de ecualizador en la carátula |

## 2. Estado principal en `MainActivity`

| Campo | Qué guarda |
|---|---|
| `allTracks` | Todas las canciones encontradas en los pendrives, ordenadas A-Z por título |
| `folderList` | Carpetas agrupadas por nombre (`FolderItem`) |
| `rootTreeNodes` | Raíces del árbol (`USB 1`, `USB 2`) |
| `currentSelectedFolder` / `currentSelectedFolderUsbSource` | Carpeta activa de la pestaña Pistas |
| `currentPlaylist` | Canciones de la carpeta activa (lo que muestra Pistas sin búsqueda y su número) |
| `displayedTracks` | Lo que muestra la lista en este momento (carpeta, resultados o favoritos) |
| `playQueue` + `currentTrackIndex` | Cola de reproducción (copia) y posición en ella |
| `currentTab` | 0 Carpetas, 1 Pistas, 2 Favoritos |
| `currentSearchFilter` | Chip: `FILTER_FOLDER`, `FILTER_ALL`, `FILTER_USB1`, `FILTER_USB2` |
| `savedTracksQuery` / `savedTracksFilter` | Búsqueda de Pistas guardada al cambiar de pestaña |
| `hasUsb1` / `hasUsb2` | Pendrives conectados (decide qué raíces y chips mostrar) |

Las tres listas de canciones se separaron a propósito:

```
currentPlaylist  →  qué carpeta está "activa" en Pistas (y su número)
displayedTracks  →  qué se ve ahora en la lista
playQueue        →  qué suena después (Anterior / Siguiente)
```

Así, explorar o abrir otra carpeta no altera lo que suena, y una búsqueda no cambia el número de Pistas.

## 3. Escaneo de pendrives — `startUsbScan()`

Se ejecuta en `scanExecutor` (hilo de fondo) al abrir la app, con **Re-escanear USB** y automáticamente al conectar o quitar un pendrive.

1. **Recorrido de carpetas:** `scanDirectory()` recorre cada raíz conocida (`/storage/udisk1`, `/storage/udisk2`, `/storage/udisk3`, `/storage/usb0`, `/storage/usb1`, `/mnt/media_rw/udiskN`, `/mnt/udiskN`) hasta 6 niveles, buscando `.mp3 .flac .wav .m4a .ogg .aac`. Las rutas con `udisk2` o `usb1` son **USB 2**; el resto, **USB 1**.
2. **Complemento con MediaStore:** consulta la biblioteca de música del sistema y agrega solo las pistas de rutas con `udisk`/`usb` que aún no estén y cuyo archivo exista.
3. **Agrupado y precálculo:** arma `FolderItem` por nombre de carpeta y calcula el `SearchKey` de cada pista y carpeta (en el hilo de fondo).
4. **Detección de pendrives:** un USB cuenta como conectado si tiene pistas o si su punto de montaje tiene contenido (`isUsbMounted`). La carpeta `/storage/udiskN` existe vacía aunque el pendrive no esté, por eso no basta con `File.exists()`.
5. **En el hilo de UI:** ordena, marca los favoritos guardados, reemplaza `allTracks`, llama a `reconcileListsAfterRescan()`, arranca la lectura de duraciones y reconstruye el árbol.

### Re-escaneo automático

`usbMountReceiver` escucha `ACTION_MEDIA_MOUNTED`, `UNMOUNTED`, `REMOVED`, `BAD_REMOVAL` y `EJECT`. Cada aviso reinicia una espera de 1,5 s (`usbRescanRunnable`), de modo que la ráfaga de avisos de un solo cambio genera **un único** escaneo. No hay sondeo periódico. Se registra en `onCreate` y se libera en `onDestroy`.

### Duración de las pistas

`scanDirectory()` crea las pistas con duración 0, que significa desconocida (`Track.hasDuration()`, y la lista muestra `--:--`). Luego:

1. **MediaStore:** si el sistema ya indexó el archivo, se toma su `DURATION` durante el mismo escaneo.
2. **Lectura en segundo plano:** `startDurationFill()` pone en `pendingDurations` las pistas que siguen sin duración. Un hilo propio (`durationExecutor`) las lee con `MediaMetadataRetriever` y obtiene duración y bitrate real.
3. **Prioridad a lo visible:** cada vez que se muestra una lista (`showDisplayedTracks()`), `prioritizeDurations()` pasa sus pistas al frente de la cola. La lista se refresca como máximo cada 500 ms, y solo si cambió alguna pista visible.
4. **Al reproducir:** la duración del `MediaPlayer` también se guarda en la pista.

`durationGeneration` invalida el trabajo pendiente cuando hay un re-escaneo o se cierra la app.

### Favoritos persistentes

Se guardan en `SharedPreferences` (`jh_music` → `favorite_paths`) como un conjunto de rutas **relativas al pendrive** (`favoriteKey()`). Así sobreviven a cerrar la app y a que el pendrive cambie de `udisk1` a `udisk2`. Se escriben al tocar la estrella (`saveFavorite()`) y se aplican a las pistas nuevas en cada escaneo.

### `reconcileListsAfterRescan()`

Tras un re-escaneo pasa `currentPlaylist` y `playQueue` a los objetos `Track` nuevos (por ruta) y quita los que ya no existen. Si la carpeta activa queda vacía, se limpia. Si la canción que sonaba ya no existe, libera el `MediaPlayer` y detiene la reproducción.

## 4. Árbol de carpetas — `buildTreeFromTracks()`

Para cada pista calcula su ruta relativa al pendrive y crea los nodos que falten. La pista se agrega con `addDirectTrack()` solo al nodo de **su** carpeta. Por eso:

- El botón `[N ▶]` muestra `getDirectTrackCount()`.
- `getTracksForNode()` devuelve esas mismas canciones directas, así que lo contado y lo que se abre coinciden.
- Si un nodo no tiene canciones propias (por ejemplo la raíz del USB), devuelve todas las de su subárbol, en orden alfabético.

Un toque en un nodo solo lo expande o contrae. La carpeta activa cambia únicamente con `[N ▶]` (`openFolderFromNode`) o al reproducir una canción de otra carpeta (`playTrack`).

## 5. Buscador

### Índice precalculado

Cada `Track` guarda un `SearchKey` inmutable con título, artista, álbum y carpeta ya normalizados, las palabras del título y del artista, y un `haystack` con todo eso más la ruta relativa al pendrive sin extensión. Se calcula una vez en el escaneo y se invalida solo si cambian título o artista (`setTitle` / `setArtist`). `FolderItem` tiene uno equivalente.

`SearchText` concentra los `Pattern` precompilados. `String.replaceAll` y `split` compilaban el regex en cada llamada.

### Flujo — `performAsyncSearch()`

1. **Debounce:** 500 ms con 1–2 letras y 250 ms con más (el botón Buscar del teclado lo salta).
2. En `searchExecutor` (un hilo): normaliza la consulta y la separa en palabras.
3. Recorre la fuente según la pestaña y el chip. Cada elemento coincide si **todas** las palabras están en su `haystack` (o en su versión sin letras repetidas).
4. A cada coincidencia le calcula **una sola vez** su puntaje (`calculateTrackRelevance` / `calculateFolderRelevance`) y la guarda como `Scored`.
5. Ordena comparando enteros (`sortByScore`).
6. Publica en el hilo de UI solo si `searchVersion` no cambió.

`searchVersion` sube con cada tecla: si llega una búsqueda nueva, la anterior se abandona en su siguiente vuelta del bucle.

### Reglas de la pestaña Pistas

- Sin texto, Pistas muestra `currentPlaylist`, sea cual sea el chip. Los chips solo definen dónde se busca.
- El número de la pestaña siempre sale de `updateTracksTabCount()` (`currentPlaylist.size()`).
- `selectTab()` guarda texto y chip al salir de Pistas y los restaura al volver. Abrir otra carpeta (`openFolder*`) descarta lo guardado.

## 6. Reproducción

### `playTrack(index)` — al tocar una canción de la lista

| Situación | Cola (`playQueue`) | Carpeta activa |
|---|---|---|
| Pestaña Favoritos | Copia de la lista mostrada | No cambia |
| La canción está en `currentPlaylist` | Copia de `currentPlaylist` | No cambia |
| Canción de otra carpeta (búsqueda) | Se rearma la carpeta de la canción | Pasa a ser esa carpeta |
| Canción sin carpeta | Copia de la lista mostrada | No cambia |

La cola es una **copia**, para que abrir otra carpeta no altere lo que suena después.

### Inicio de reproducción

Todo inicio manual pasa por `startPlayback(track)`: cancela cualquier mezcla y precarga, prepara el archivo y llama a `onTrackStarted(track)`. Ese método, compartido con Automix, actualiza pantalla, resaltado, carátula y vistas previas. Los reproductores se crean con `createPlayer()`, que asigna el `OnCompletionListener` común (`onPlayerCompleted`).

### Automix — `AutoMixer`

Clase aparte que maneja dos `MediaPlayer` durante la mezcla y habla con la Activity mediante la interfaz `AutoMixer.Host`.

1. **Tick cada 250 ms:** si Automix está activo y suena música, mira cuánto falta para el final.
2. **Precarga:** cuando falta menos de mezcla + 15 s, pide la siguiente a `pickNextTrack()` (respeta Repetir y Aleatorio) y la prepara con `prepareAsync`.
3. **Mezcla:** cuando falta menos que la duración de la mezcla, arranca la siguiente en volumen 0, la Activity la toma como `mediaPlayer` (`onMixStarted`) y un fundido de igual potencia ajusta los volúmenes cada 50 ms. Al terminar se libera el reproductor que salía.
4. **Respaldo:** si la canción principal termina sin mezcla, `onPlayerCompleted` usa la siguiente ya preparada (`startPreparedNextNow`) antes de recurrir a `playNextTrack()`.

**Mezcla inteligente:** `onTrackStarted` pide a `AudioAnalyzer` el análisis del final de la canción, y la precarga pide el del inicio de la siguiente. `planMix()` usa ese análisis para decidir dónde empieza la mezcla y cuánto dura (fade-out → desde que bajó 8 dB; final seco → 3 s antes del fin real), y `startMix` salta el silencio inicial de la que entra. Sin análisis se usan los últimos 5 s. Detalle en [06 · Investigación del mezclador](06-investigacion-mezclador.md#13-estado-de-la-implementación).

La siguiente precargada se descarta con cualquier inicio manual, al cambiar Aleatorio o Repetir, y al re-escanear (`invalidateNext`). El estado se guarda en `SharedPreferences` (`automix_enabled`).

### Siguiente, anterior y fin de canción

`playNextTrack()` / `playPreviousTrack()` recorren `getQueue()` (la cola o, si está vacía, la carpeta activa) con vuelta circular. Aleatorio elige un índice al azar. Al terminar una canción, Repetir vuelve a reproducirla; si no, pasa a la siguiente.

### Resaltado

`TrackAdapter.setCurrentPlayingTrack(track)` guarda la **ruta** de la canción que suena y cada fila se compara por ruta. Así la marca no queda en otra fila al cambiar de lista.

### Carátula y bitrate

`loadAlbumArt()` usa `MediaMetadataRetriever` en segundo plano para la carátula embebida, el bitrate real (`MP3 192K`) y el artista de las etiquetas.

## 7. Volumen y mute

Trabaja sobre `AudioManager.STREAM_MUSIC` en pasos de 5 %.

- `updateVolumeUI()` consulta el mute real del sistema (`isStreamMute`, Android 6+). En mute `getStreamVolume` devuelve 0, por eso en ese estado se muestra `MUTE` y no se pisa el último porcentaje.
- `setMuted()` aplica `ADJUST_MUTE` / `ADJUST_UNMUTE` y luego vuelve a leer el volumen real.
- Subir, bajar o arrastrar la barra estando en mute primero lo quita.

## 8. Gestos e interfaz

- **`dispatchTouchEvent`:** detecta el gesto de jalar en la zona de la lupa (más de 35 px en horizontal) para mostrar u ocultar el explorador, y protege las barras de progreso y de volumen.
- **Lupa:** está dentro de esa zona y no es clicable por sí misma; un toque simple sobre ella se detecta en el `OnTouchListener` de la zona (`isTouchOnView`).
- **`TrackAdapter`:** un toque en la fila espera 280 ms para distinguirlo de un doble toque. Uno expande el nombre; dos reproducen.
- **Modo inmersivo** a pantalla completa y pantalla siempre encendida (`FLAG_KEEP_SCREEN_ON`).
- **Teclas multimedia** (control Bluetooth u otro auto; el Tucson del proyecto no tiene botones en el volante) en `onKeyDown`: `MEDIA_PLAY_PAUSE`, `HEADSETHOOK`, `MEDIA_NEXT`, `MEDIA_PREVIOUS`, `VOLUME_UP/DOWN`, `VOLUME_MUTE`.

## 9. Recursos

| Recurso | Uso |
|---|---|
| `res/layout/activity_main.xml` | Pantalla completa |
| `res/layout/item_track.xml`, `item_folder.xml`, `item_tree_folder.xml` | Filas de las listas |
| `res/drawable/ic_launcher.xml` | Ícono para Android 7 o anterior |
| `res/drawable-v26/ic_launcher.xml` | Ícono adaptativo (Android 8+), con `ic_launcher_background` y `ic_launcher_foreground` |
| `res/values/strings.xml` | Nombre de la app (`JH MUSIC`) |
| `res/values/colors.xml` | Paleta (fondo carbón, acento cian `#38bdf8`) |

## 10. Simulador web

`index.html`, `app.js` y `style.css` (raíz) son una maqueta de la interfaz en navegador, con lógica propia en JavaScript y audio simulado. Se mantiene sincronizada a mano con la app nativa y se copia en `android/app/src/main/assets/`. La app nativa **no** la carga (no usa `WebView`).
