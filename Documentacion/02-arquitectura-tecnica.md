# Arquitectura técnica — JH MUSIC

App Android nativa en Java: una sola `Activity`, adapters de `ListView` (y un `RecyclerView` para Mi Mix) y vistas propias dibujadas con `Canvas`. El audio se reproduce con `MediaPlayer` del sistema; Automix usa dos a la vez durante la mezcla.

| Dato | Valor |
|---|---|
| Lenguaje | Java 8 |
| `minSdk` / `targetSdk` / `compileSdk` | 21 / 28 / 33 |
| Dependencias | Solo `androidx.recyclerview:recyclerview:1.3.0` (desde 2.7.0, para reordenar Mi Mix) |
| Paquete | `com.jh.caraudio` |

## 1. Clases

Todas están en `android/app/src/main/java/com/jh/caraudio/`.

| Clase | Responsabilidad |
|---|---|
| `MainActivity` | Toda la lógica: escaneo USB, árbol de carpetas, pestañas, buscador, cola y reproducción, volumen, gestos y teclas multimedia |
| `Track` | Una canción: ruta, título, artista, álbum, carpeta, pendrive, formato, duración (0 = desconocida). Guarda su `SearchKey` |
| `FolderItem` | Carpeta agrupada por nombre (resultados de búsqueda en la pestaña Librería). Guarda su `SearchKey` |
| `TreeNode` | Nodo del árbol de Librería: hijos, contador y lista de canciones directas |
| `SearchText` | Utilidades del buscador con regex precompilados: normalizar, colapsar letras repetidas, rutas relativas |
| `AutoMixer` | Automix: precarga de la siguiente canción, mezcla de igual potencia entre dos `MediaPlayer`, fundido corto de Siguiente/Anterior y aplicación de la corrección de volumen |
| `AudioAnalyzer` | Analiza inicio, medio y final de cada canción: silencios, fade-out y sonoridad (LUFS aprox.) |
| `LoudnessLeveler` | Igualar volumen: corrección por reproductor, con `setVolume` para bajar y `LoudnessEnhancer` para subir |
| `TrackAdapter` | Lista de canciones: resaltado de la que suena, toque / doble toque, botón de Mi Mix (+ / ✓ / ✕), marcas "ya sonó", índice A-Z |
| `MixRecyclerAdapter` | Lista de la pestaña Mi Mix (`RecyclerView`): mismas filas que `TrackAdapter` y, en modo reordenar, la manija `≡` que arrastra la fila con `ItemTouchHelper` |
| `FolderTreeAdapter` | Filas del árbol de Librería con `+` y ▶; `flash(node)` enciende la fila tocada 250 ms y deja su nombre completo; el ▶ de la carpeta que suena muestra `MiniEqualizerView` |
| `MiniEqualizerView` | Tres barritas blancas en el ▶ de la carpeta que suena: se mueven mientras suena (`setAnimating`), quietas en pausa; redibujo a 30 cuadros/s solo si está a la vista |
| `FolderAdapter` | Lista plana de carpetas (resultados de búsqueda en Librería) |
| `VerticalVolumeBar` | Barra de volumen vertical dibujada a mano |
| `SearchSpinnerView` | Spinner de "Buscando…" que no depende de las animaciones del sistema |
| `LaserRingView` | Haz láser que gira por el borde de la carátula: cian al sonar, más uno magenta al mezclar |
| `NowPlayingPillView` | Cápsula `PAUSED / PLAYING / MIXING ⇄` con 5 barras de ecualizador, montada sobre el borde de abajo de la carátula |
| `CarMarqueeTextView` | Texto de una línea que se desplaza como la marquesina de la web (título, tarjetas y fila que suena) |
| `AmbientTheme` | Fondo de toda la app con el color de la carátula (degradados sobre la raíz) y fundido entre canciones; también da el color al resplandor |
| `AlbumGlowView` | Sombra y resplandor detrás de la carátula (el box-shadow de la web), con BlurMaskFilter en software |
| `SoundWaveBarsView` | Barras de ecualizador antiguas. **Sin uso desde 2.3.0** (las reemplazó `NowPlayingPillView`); se puede borrar |

## 2. Estado principal en `MainActivity`

| Campo | Qué guarda |
|---|---|
| `allTracks` | Todas las canciones encontradas en los pendrives, ordenadas A-Z por título |
| `folderList` | Carpetas agrupadas por nombre (`FolderItem`) |
| `rootTreeNodes` | Raíces del árbol (`USB 1`, `USB 2`) |
| `currentSelectedFolder` / `currentSelectedFolderUsbSource` | Carpeta activa de la pestaña Canciones |
| `currentPlaylist` | Canciones de la carpeta activa (lo que muestra la pestaña Canciones sin búsqueda y su número) |
| `displayedTracks` | Lo que muestra la lista en este momento (carpeta, resultados o Mi Mix) |
| `playQueue` + `currentTrackIndex` | Cola de reproducción (copia) y posición en ella |
| `currentTab` | 0 Librería, 1 Canciones, 2 Mi Mix |
| `currentSearchFilter` | Chip: `FILTER_FOLDER`, `FILTER_ALL`, `FILTER_USB1`, `FILTER_USB2` |
| `savedTracksQuery` / `savedTracksFilter` | Búsqueda de Canciones guardada al cambiar de pestaña |
| `hasUsb1` / `hasUsb2` | Pendrives conectados (decide qué raíces y chips mostrar) |

Las tres listas de canciones se separaron a propósito:

```
currentPlaylist  →  qué carpeta está "activa" en Canciones (y su número)
displayedTracks  →  qué se ve ahora en la lista
playQueue        →  qué suena después (Anterior / Siguiente)
```

Así, explorar o abrir otra carpeta no altera lo que suena, y una búsqueda no cambia el número de Canciones.

## 3. Escaneo de pendrives — `startUsbScan()`

Se ejecuta en `scanExecutor` (hilo de fondo) al abrir la app, con **Menú ☰ → Re-escanear USB** y automáticamente al conectar o quitar un pendrive.

1. **Recorrido de carpetas:** `scanDirectory()` recorre cada raíz conocida (`/storage/udisk1`, `/storage/udisk2`, `/storage/udisk3`, `/storage/usb0`, `/storage/usb1`, `/mnt/media_rw/udiskN`, `/mnt/udiskN`) hasta 6 niveles, buscando `.mp3 .flac .wav .m4a .ogg .aac`. Las rutas con `udisk2` o `usb1` son **USB 2**; el resto, **USB 1**.
2. **Complemento con MediaStore:** consulta la biblioteca de música del sistema y agrega solo las pistas de rutas con `udisk`/`usb` que aún no estén y cuyo archivo exista.
3. **Agrupado y precálculo:** arma `FolderItem` por nombre de carpeta y calcula el `SearchKey` de cada pista y carpeta (en el hilo de fondo).
4. **Detección de pendrives:** un USB cuenta como conectado si tiene pistas o si su punto de montaje tiene contenido (`isUsbMounted`). La carpeta `/storage/udiskN` existe vacía aunque el pendrive no esté, por eso no basta con `File.exists()`.
5. **En el hilo de UI:** ordena, carga Mi Mix (`loadMyMix`), reemplaza `allTracks`, llama a `reconcileListsAfterRescan()`, arranca la lectura de duraciones y reconstruye el árbol.

### Re-escaneo automático

`usbMountReceiver` escucha `ACTION_MEDIA_MOUNTED`, `UNMOUNTED`, `REMOVED`, `BAD_REMOVAL` y `EJECT`. Cada aviso reinicia una espera de 1,5 s (`usbRescanRunnable`), de modo que la ráfaga de avisos de un solo cambio genera **un único** escaneo. No hay sondeo periódico. Se registra en `onCreate` y se libera en `onDestroy`.

### Duración de las pistas

`scanDirectory()` crea las pistas con duración 0, que significa desconocida (`Track.hasDuration()`, y la lista muestra `--:--`). Luego:

1. **MediaStore:** si el sistema ya indexó el archivo, se toma su `DURATION` durante el mismo escaneo.
2. **Lectura en segundo plano:** `startDurationFill()` pone en `pendingDurations` las pistas que siguen sin duración. Un hilo propio (`durationExecutor`) las lee con `MediaMetadataRetriever` y obtiene duración y bitrate real.
3. **Prioridad a lo visible:** cada vez que se muestra una lista (`showDisplayedTracks()`), `prioritizeDurations()` pasa sus pistas al frente de la cola. La lista se refresca como máximo cada 500 ms, y solo si cambió alguna pista visible.
4. **Al reproducir:** la duración del `MediaPlayer` también se guarda en la pista.

`durationGeneration` invalida el trabajo pendiente cuando hay un re-escaneo o se cierra la app.

### Mi Mix (reemplaza a Favoritos)

Mi Mix es una lista **ordenada** de claves (ruta relativa al pendrive, `favoriteKey()`), guardada en `SharedPreferences` (`jh_music` → `mymix_keys`, una clave por línea). Así sobrevive a cerrar la app y a que el pendrive cambie de `udisk1` a `udisk2`. La primera vez (`loadMyMix`) pasan las claves de los viejos favoritos (`favorite_paths`): primero en el orden A-Z de la biblioteca y después las de pendrives no conectados.

| Pieza | Qué hace |
|---|---|
| `myMixKeys` | Orden de la lista (el orden en que suena) |
| `mixTracks()` | Las canciones conectadas, en ese orden |
| `mixPlayedKeys` | Las que ya sonaron en esta sesión (marcas ✓, solo en memoria; se borran con **▶ Reproducir**) |
| `playingFromMix` | La cola que suena (`playQueue`) es Mi Mix |
| `addToMix` / `removeFromMix` / `clearMix` / `reAddToMixInPlace` | Cambian la lista y llaman a `onMixChanged()`: guardar, contador de la pestaña, `syncMixQueue()` y refrescar la pestaña |
| `syncMixQueue()` | Con Mi Mix sonando, rearma `playQueue` con la lista actual (agregar en vivo). Si se quitó la que suena, la deja en su posición (`mixCurPos`) para que siga y después venga la siguiente. Llama a `autoMixer.invalidateNext()` para que Automix vuelva a elegir la siguiente |
| `nextMixIndex()` | Siguiente en Mi Mix, o −1 al final. En aleatorio, una que no haya sonado |
| `finishMix()` | Al final: pausa, vuelve al inicio de la canción y avisa "Fin de Mi Mix" |

Con Mi Mix sonando, `playNextTrack()`, `playPreviousTrack()` (Anterior en la primera la reinicia), `pickNextTrack()` (Automix no mezcla después de la última) y `updatePreviews()` (`■ Fin de Mi Mix`) usan `nextMixIndex()`. `playTrack()` desde cualquier otra pestaña pone `playingFromMix = false`.

La pestaña (`showMixTab()`) tiene una cabecera propia (`layoutMixHeader`: resumen, Reproducir, Vaciar con confirmación) y `tvMixEmpty` cuando está vacía. `TrackAdapter.setMixMode(true)` numera por la lista, muestra ✕ y apaga las que ya sonaron; la que suena fuera de la lista va primero con un + (`MixState.isOrphan`). Los botones + de carpetas están en `FolderTreeAdapter` (`onNodeAddMixClick`) y `FolderAdapter` (`OnFolderAddMixListener`). Los avisos cortos son un `TextView` sobre `android.R.id.content` (`showMixToast`).

**Indicar que suena Mi Mix:** `refreshTabLabels()` agrega a la pestaña Mi Mix un punto cian (`addMixLiveDot`, un `GradientDrawable` de 11dp dentro de un `CenteredImageSpan`) mientras `playingFromMix`; con música sonando, `mixLiveDotPulse` le cambia la opacidad cada 60 ms (late). Se refresca en `updatePlaybackStateUI()` y en `onTrackStarted()`. `updateTrackSource()` pone debajo del título "Mi Mix · N de M" (posición en `playQueue`) en lugar del álbum; se actualiza al empezar cada canción y en `syncMixQueue()`.

**Reordenar (2.7.0):** sin búsqueda, `showMixTab()` muestra Mi Mix en `recyclerMixContent` (un `RecyclerView` con `MixRecyclerAdapter`) y oculta `listViewContent`. El botón `btnMixReorder` (⇅) activa `isReorderingMix`: la `✕` de cada fila pasa a ser la manija `≡`, y tocarla llama a `ItemTouchHelper.startDrag()` (sin pulsación larga ni deslizar). `onMove` mueve la fila en vivo (las demás abren el hueco) y no deja mover ni pasar la fila "Sonando · ya no está en Mi Mix" (posición 0). Al soltar (`clearView`) y con **✓ Listo** (`btnMixReorderDone`), `saveReorderedMixFromDisplayedTracks()` rearma `myMixKeys` en el orden de la pantalla (las de pendrives no conectados quedan al final), guarda (`saveMyMix`) y llama a `syncMixQueue()`.

**Ubicar la canción que suena en la lista:** `scrollToPlayingTrack()` busca la canción en `displayedTracks` y la deja con una canción antes arriba: en Canciones con `setSelectionFromTop(índice - 1, 0)` y en Mi Mix (que está en `recyclerMixContent`) con `LinearLayoutManager.scrollToPositionWithOffset(índice - 1, 0)`. Se llama al volver a Canciones o Mi Mix (`selectTab`) y al tocar el chip de la carpeta actual; si hay búsqueda, después de que llegan los resultados (`scrollToPlayingAfterSearch`). En la maqueta web lo mismo hace `scrollListToPlaying()`, y `renderList()` conserva el desplazamiento cuando se vuelve a dibujar la misma lista. La web mueve solo la lista (`scrollListToRow`), no `scrollIntoView`, que también desplazaba toda la pantalla.

### `reconcileListsAfterRescan()`

Tras un re-escaneo pasa `currentPlaylist` y `playQueue` a los objetos `Track` nuevos (por ruta) y quita los que ya no existen. Si la carpeta activa queda vacía, se limpia. Si la canción que sonaba ya no existe, libera el `MediaPlayer` y detiene la reproducción.

## 4. Árbol de carpetas — `buildTreeFromTracks()`

Para cada pista calcula su ruta relativa al pendrive y crea los nodos que falten. La pista se agrega con `addDirectTrack()` solo al nodo de **su** carpeta. Por eso:

- El botón ▶ abre las `getDirectTrackCount()` canciones directas (el número ya no se muestra en la fila; se ve en la pestaña Canciones al abrirla).
- `getTracksForNode()` devuelve esas mismas canciones directas, así que lo contado y lo que se abre coinciden.
- Si un nodo no tiene canciones propias (por ejemplo la raíz del USB), devuelve todas las de su subárbol, en orden alfabético.

Un toque en un nodo solo lo expande o contrae. La carpeta activa cambia únicamente con ▶ (`openFolderFromNode`) o al reproducir una canción de otra carpeta (`playTrack`).

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

### Escritura exacta (ñ y tildes)

`SearchText.normalize()` quita las tildes con NFD, y la **ñ cuenta como tilde** (es `n` + `◌̃`): `ñaña` y `nana` quedan iguales. Por eso coinciden, pero para ordenar se premia la escritura exacta:

- `SearchKey` guarda también `exactTitle` y `exactHaystack`: el mismo texto en minúsculas y en NFC, **conservando ñ y tildes** (`SearchText.exactLower()`). `FolderItem.SearchKey` tiene `exactName` y `exactHaystack`.
- `exactTokens(rawQuery, norm)` devuelve las palabras tal como se escribieron, o `null` si la consulta no trae ñ ni tildes (en ese caso no hay nada que premiar y el orden es el de siempre).
- `exactSpellingBonus()` suma **+20000** por cada palabra que aparece con la misma escritura en el título (o nombre de carpeta) y **+12000** si aparece en el resto. Se aplica al final de `calculateTrackRelevance` y `calculateFolderRelevance`; los demás puntajes no cambian.

La maqueta web hace lo mismo con `exactLower`, `exactTokens` y `exactSpellingBonus` en `app.js`.

### Reglas de la pestaña Canciones

- Sin texto, Canciones muestra `currentPlaylist`, sea cual sea el chip. Los chips solo definen dónde se busca.
- El número de la pestaña siempre sale de `updateTracksTabCount()` (`currentPlaylist.size()`).
- `selectTab()` guarda texto y chip al salir de Canciones y los restaura al volver. Abrir otra carpeta (`openFolder*`) descarta lo guardado.

## 6. Reproducción

### `playTrack(index)` — al tocar una canción de la lista

| Situación | Cola (`playQueue`) | Carpeta activa |
|---|---|---|
| Pestaña Mi Mix | Mi Mix (`playMix`), en vivo | No cambia |
| La canción está en `currentPlaylist` | Copia de `currentPlaylist` | No cambia |
| Canción de otra carpeta (búsqueda) | Se rearma la carpeta de la canción | Pasa a ser esa carpeta |
| Canción sin carpeta | Copia de la lista mostrada | No cambia |

La cola es una **copia**, para que abrir otra carpeta no altere lo que suena después.

### Inicio de reproducción

Todo inicio manual pasa por `startPlayback(track)`: cancela cualquier mezcla y precarga, prepara el archivo (con `prepare()`, que bloquea el hilo de UI mientras lee el pendrive; ver propuesta #4) y llama a `onTrackStarted(track)`. Ese método, compartido con Automix, actualiza pantalla, resaltado, carátula y vistas previas. Los reproductores se crean con `createPlayer()`, que asigna el `OnCompletionListener` común (`onPlayerCompleted`).

### Automix — `AutoMixer`

Clase aparte que maneja dos `MediaPlayer` durante la mezcla y habla con la Activity mediante la interfaz `AutoMixer.Host`.

1. **Tick cada 250 ms:** si Automix está activo y suena música, mira cuánto falta para el final.
2. **Precarga:** cuando falta menos de mezcla + 15 s, pide la siguiente a `pickNextTrack()` (respeta Repetir y Aleatorio) y la prepara con `prepareAsync`.
3. **Mezcla:** cuando falta menos que la duración de la mezcla, arranca la siguiente en volumen 0, la Activity la toma como `mediaPlayer` (`onMixStarted`) y un fundido de igual potencia ajusta los volúmenes cada 50 ms. Al terminar se libera el reproductor que salía.
4. **Respaldo:** si la canción principal termina sin mezcla, `onPlayerCompleted` usa la siguiente ya preparada (`startPreparedNextNow`) antes de recurrir a `playNextTrack()`.

**Mezcla inteligente:** `onTrackStarted` pide a `AudioAnalyzer` el análisis del final de la canción, y la precarga pide el del inicio de la siguiente. `planMix()` usa ese análisis para decidir dónde empieza la mezcla y cuánto dura (fade-out → desde que bajó 8 dB; final seco → 3 s antes del fin real), y `startMix` salta el silencio inicial de la que entra. Sin análisis se usan los últimos 5 s. Detalle en [06 · Investigación del mezclador](06-investigacion-mezclador.md#13-estado-de-la-implementación).

**Siguiente / Anterior con Automix:** `startPlayback(track, quickFade)` prepara la canción en un reproductor nuevo y `fadeManual()` la cruza con la anterior en 0,5 s. Si la siguiente ya estaba precargada, `skipToPreparedNext()` la usa directo, sin esperar al pendrive.

**Igualar volumen (fase 3):** `AudioAnalyzer` también mide la sonoridad de la canción (ponderación K de ITU-R BS.1770 y compuertas de EBU R128, sobre 10 s de inicio, 10 s del medio y 25 s del final). `LoudnessLeveler.gainFor()` calcula la corrección hacia −12 LUFS, con tope de −6 dB / +4 dB:
- La parte de **bajar** se multiplica en cada `setVolume` del mezclador (`setPlayerVolume`), también durante los fundidos.
- La parte de **subir** usa un `LoudnessEnhancer` sobre el `audioSessionId` del reproductor, creado solo si hace falta. Si el radio no lo soporta, se omite.
- `startMix` fija la corrección de la que entra **antes** de arrancarla (en volumen 0). Por eso `beginFade` se llama antes de `onMixStarted`: mientras dura la mezcla, `onTrackStarted` no aplica volumen completo.
- Si una canción iniciada a mano no tenía análisis, `checkPendingGain()` (en el tick) aplica la corrección con una rampa de 2 s cuando llega, solo si llega en los primeros 20 s.
- Al apagar Automix, la canción vuelve a 0 dB con la misma rampa. Cada `LoudnessEnhancer` se libera junto con su reproductor (`releaseQuietly`, `forgetPlayer`, `release`).

**Pantalla durante la mezcla:** `onMixVisualChanged(true/false)` enciende el haz magenta de `LaserRingView` y el estado `MIXING ⇄` de `NowPlayingPillView`. Solo las mezclas automáticas lo muestran; el fundido de Siguiente/Anterior, no.

La siguiente precargada se descarta con cualquier inicio manual, al cambiar Aleatorio o Repetir, y al re-escanear (`invalidateNext`). El estado se guarda en `SharedPreferences` (`automix_enabled`).

### Siguiente, anterior y fin de canción

`playNextTrack()` / `playPreviousTrack()` recorren `getQueue()` (la cola o, si está vacía, la carpeta activa) con vuelta circular. Aleatorio elige un índice al azar. Al terminar una canción, Repetir vuelve a reproducirla; si no, pasa a la siguiente.

### Resaltado

`TrackAdapter.setCurrentPlayingTrack(track)` guarda la **ruta** de la canción que suena y cada fila se compara por ruta. Así la marca no queda en otra fila al cambiar de lista.

### Carátula y bitrate

`loadAlbumArt()` usa `MediaMetadataRetriever` en segundo plano para la carátula embebida, el bitrate real (`MP3 192K`) y el artista de las etiquetas. En una mezcla automática, `showAlbumArt(bitmap, smooth)` cambia la carátula con un `TransitionDrawable` (fundido cruzado); el resto de los datos cambia al instante, como en la web. Sin carátula se muestra el disco (`ivVinylDisc`).

### Doble toque en Siguiente / Anterior

`handleNextButtonClick()` / `handlePrevButtonClick()` (botones ⏮ ⏭ y tarjetas) esperan 280 ms (`DOUBLE_TAP_TIMEOUT_MS`): un toque cambia de canción; dos o más llaman a `seekRelative(±10 s)` y suman 10 s por toque extra. `showSeekFeedback()` muestra un aviso `+10s` / `-10s` en un `TextView` agregado sobre `android.R.id.content`, centrado en el reproductor, que aparece y se va con fundido. Adelantar termina cualquier mezcla en curso.

## 7. Volumen y mute

Trabaja sobre `AudioManager.STREAM_MUSIC` en pasos de 10 %.

- `updateVolumeUI()` consulta el mute real del sistema (`isStreamMute`, Android 6+). En mute `getStreamVolume` devuelve 0, por eso en ese estado se muestra `MUTE` y no se pisa el último porcentaje.
- `setMuted()` aplica `ADJUST_MUTE` / `ADJUST_UNMUTE` y luego vuelve a leer el volumen real.
- Subir, bajar o arrastrar la barra estando en mute primero lo quita.

## 8. Pantalla, gestos e interfaz

### Barra de arriba (oculta desde 2.9.4)

`layoutCarStatusBar` (marca, versión, saludo, reloj y Re-escanear USB) está en el layout con `visibility="gone"`: el código la sigue usando (`tvClock`, `tvAppVersion`, `tvHeaderPath`, `btnRescanUsb`), pero no se ve. Lo que tenía pasó a:

- **Reloj:** `tvStripClockHour` y `tvStripClockMin` (30 sp, dos líneas) arriba de la tira de volumen; `setupClock()` los actualiza cada segundo junto con `tvClock`. Debajo, `viewUsbDot` (fondo `bg_usb_dot`) se pinta verde si hay pendrive (`hasUsb1 || hasUsb2`) y gris si no (`updateUsbDot()`, desde `updateFilterChipsVisibility()`).
- **Re-escanear USB y versión:** en el menú ☰ (fila `menuRescan` y pie `menuVersion`, ver §12).

### Tira de volumen

72 dp de ancho, con HOME, MUTE, `+` y `−` de 60 × 52 dp y `VerticalVolumeBar` de 60 dp de ancho (pista de 16 dp, pomo de 13 dp de radio).

### Carátula

- **Tamaño:** el cuadro mide 150 dp en el layout. `setupAlbumArtAutoSize()` espera al primer layout y lo agranda con el espacio que sobra en la fila de controles (`layoutTransportControls`, dejando 8 dp arriba y abajo de los botones), con un máximo de **300 dp** y dejando 66 dp a cada lado (a la derecha están el menú ☰ y Automix). Sin la barra de arriba, en el radio queda en unos 298 dp.
- **Toques:** el `OnTouchListener` de `layoutAlbumArtCard` llama a `handleCoverTap()` en cada toque (si el dedo no se movió más de 20 dp): dos toques en menos de 350 ms (`COVER_DOUBLE_TAP_MS`) ocultan o muestran el explorador (`toggleExplorerVisibility()`); un toque solo no hace nada. Ya no pausa: con la espera para distinguir el doble toque, un doble toque algo lento pausaba.
- **Presionado:** `foreground="@drawable/fg_album_card_pressed"` (borde cian de 3 dp) y el mismo listener achica el cuadro al 97 %. Antes usaba `selectableItemBackground`, que lo tapaba entero con el color de acento del sistema (naranja).
- **Cápsula y etiqueta (para no tapar su contenido):**
  - `NowPlayingPillView` (38 dp de alto) **no** está dentro del cuadro: es hija del `RelativeLayout` de arriba, alineada al borde de abajo de `layoutAlbumArtCard` con `marginBottom="-19dp"`, así queda mitad dentro y mitad fuera. Va después de `LaserRingView` para dibujarse encima. Está fuera del cuadro porque `layoutAlbumArtInner` recorta su contenido con `setClipToOutline(true)`.
  - Para que esa mitad no tape el título, `tvTrackTitle` tiene `marginTop="25dp"` (6 dp de siempre + 19 dp). Ese espacio sale de la carátula: como `setupAlbumArtAutoSize()` reparte lo que sobra, el cuadro queda 19 dp más chico y **el título y todo lo de abajo quedan donde estaban**.
  - `tvFormatBadge` (MP3 320K) va dentro del cuadro, arriba a la derecha, con márgenes de −1 dp (tapa el borde suavizado de la imagen en la curva) y fondo `bg_audio_badge_corner`: sólido, con curva propia de 18,5 dp arriba a la derecha (la del cuadro es 17,5 dp) y borde solo a la izquierda y abajo. Un fondo translúcido o sin curva propia dejaba ver la carátula por la esquina.

### `dispatchTouchEvent`

En este orden:

1. **Anterior / play / siguiente** (`handleNavZoneTouch`, §13): si el toque empieza en la franja de abajo del reproductor, se maneja ahí y no sigue.
2. **Gesto de volumen** (§14): empieza si el toque no cae sobre las barras de progreso o de volumen, el buscador con foco ni el explorador (`isTouchInExplorer`).
3. Protege las barras de progreso y de volumen de los demás gestos, y oculta el teclado al tocar fuera del buscador.

El explorador ya no se jala de costado: se quitaron `layoutExplorerToggleZone` y su lógica (la franja junto a la carátula quedó para el gesto de volumen). Se oculta o muestra con doble toque en la carátula o desde el menú ☰.

### Otros

- **Automix:** botón normal (`btnAutoMix`, debajo del menú ☰) que llama a `toggleAutoMix()`.
- **Indicadores:** `ivModeRepeat` y `ivModeShuffle`, arriba a la izquierda del reproductor, se ven solo si repetir o aleatorio están encendidos (`updateModeIndicators()`). No reciben toques.
- **Librería (2.9.6, pensada para 1024 × 600):** `item_tree_folder.xml` de 60 dp de alto, 8 dp entre filas, sangría `2 + nivel × 12` dp, flecha de 24 dp, ícono fijo (cuadro verde de 44 dp en la raíz USB; en carpetas, solo el dibujo de 20 dp), nombre de 21 sp en 1 renglón con marquesina, `+` de 46 × 46 dp y ▶ cuadrado de 52 × 48 dp. Sin línea de cantidad. Doble toque en una fila (dos en menos de 350 ms, `FOLDER_DOUBLE_TAP_MS`) = `playFolderFromNode()`; el segundo toque deja la carpeta abierta o cerrada como estaba. Cada toque llama a `folderTreeAdapter.flash(node)`: fondo `bg_tree_node_flash` 250 ms y `fullTitleNode`, que muestra ese nombre en hasta 2 renglones.
- **Colores del árbol:** la carpeta que suena no se pinta: su ▶ cambia por `MiniEqualizerView` (`setPlaybackRunning(isPlaying)` desde `updatePlaybackStateUI()`); la abierta en Canciones que no suena lleva `bg_tree_node_open` (borde cian fino). `scrollTreeToPlayingFolder()` deja a la vista la que suena al volver a Librería (`selectTab(0)`), sin abrir carpetas cerradas.
- **Pestañas:** `setTabLabel(tab, ícono, texto, número, elegida)` dibuja el número con `CountBadgeSpan` (etiqueta redondeada de 13 sp) en vez de "(N)"; la pestaña queda en un renglón (`setSingleLine`, sin ancho mínimo).
- **Título y artista con alto fijo** (36 dp y 26 dp): con `wrap_content`, una canción con letras de otra fuente (más altas) agrandaba la línea y bajaba anterior / play / siguiente, que ocupan lo que sobra (`layout_weight`).
- **Al tocar:** `bg_pressed_cyan` (cian translúcido) en las tarjetas de anterior / siguiente y en las filas de canciones y de carpetas buscadas, en vez de `selectableItemBackground`, que en el emulador y en algunos radios sale naranja.
- **Animaciones propias:** `LaserRingView`, `NowPlayingPillView` y `CarMarqueeTextView` se redibujan con `postInvalidateDelayed(33 ms)` (unos 30 cuadros por segundo) y no con `Animator`, porque el radio puede tener las animaciones del sistema desactivadas. Solo se animan mientras hace falta (sonando, mezclando o con texto que no entra).
- **Marquesina (`CarMarqueeTextView`):** el ciclo dura entre 6 y 16 s según cuánto sobra el texto: quieto al inicio (0–20 %), se desliza al final (20–65 %), quieto (65–82 %) y vuelve (82–100 %). Antes de dibujar compensa el `scrollX` interno de `TextView`; sin eso, los textos centrados que no entraban salían vacíos. En la lista solo se desplaza la fila que suena; las demás se cortan con "…".
- **`TrackAdapter`:** un toque en la fila espera 280 ms para distinguirlo de un doble toque. Uno expande el nombre; dos reproducen.
- **Pestaña Canciones:** ícono `ic_tab_music` (nota musical).
- **Modo inmersivo** a pantalla completa y pantalla siempre encendida (`FLAG_KEEP_SCREEN_ON`).
- **Teclas multimedia** (control Bluetooth u otro auto; el Tucson del proyecto no tiene botones en el volante) en `onKeyDown`: `MEDIA_PLAY_PAUSE`, `HEADSETHOOK`, `MEDIA_NEXT`, `MEDIA_PREVIOUS`, `VOLUME_UP/DOWN` (±10 %), `VOLUME_MUTE`.

## 9. Recursos

| Recurso | Uso |
|---|---|
| `res/layout/activity_main.xml` | Pantalla completa (la barra de arriba está oculta) |
| `res/layout/item_track.xml`, `item_folder.xml`, `item_tree_folder.xml` | Filas de las listas |
| `res/layout/popup_player_menu.xml` | Menú ☰ (aleatorio, repetir, explorador, re-escanear y versión) |
| `res/layout/layout_blind_volume_hud.xml` | Indicador grande del gesto de volumen (con `bg_blind_volume_hud`, `bg_hud_track` y `bg_hud_fill`) |
| `res/drawable/bg_album_card.xml`, `bg_album_inner.xml` | Marco y fondo redondeados del cuadro de la carátula |
| `res/drawable/fg_album_card_pressed.xml` | Borde cian de la carátula presionada |
| `res/drawable/bg_audio_badge_corner.xml` | Etiqueta de formato (`MP3 320K`) pegada a la esquina de la carátula |
| `res/drawable/bg_ctrl_icon.xml`, `bg_ctrl_play.xml` | Círculo cian de anterior / siguiente y círculo blanco del play (los dos con `state_pressed` y `state_activated`) |
| `res/drawable/bg_player_menu*.xml`, `ic_menu.xml`, `ic_explorer_panel.xml`, `ic_rescan.xml` | Menú ☰ y sus íconos |
| `res/drawable/bg_tree_node_flash.xml` | Destello de la fila de carpeta tocada |
| `res/drawable/bg_usb_dot.xml` | Punto del pendrive en la tira (el código lo pinta verde o gris) |
| `res/drawable/ic_automix.xml` | Ícono de Automix (dos curvas cruzadas) |
| `res/drawable/ic_tab_music.xml` | Ícono de la pestaña Canciones |
| `res/drawable/ic_launcher.xml` | Ícono para Android 7 o anterior |
| `res/drawable-v26/ic_launcher.xml` | Ícono adaptativo (Android 8+), con `ic_launcher_background` y `ic_launcher_foreground` |
| `res/values/strings.xml` | Nombre de la app (`JH MUSIC`) |
| `res/values/colors.xml` | Paleta (fondo carbón, acento cian `#38bdf8`) |

## 10. Simulador web

`index.html`, `app.js` y `style.css` (en `jh-music/`) son una maqueta de la interfaz en navegador, con lógica propia en JavaScript y audio simulado. Se mantiene sincronizada a mano con la app nativa y se copia en `android/app/src/main/assets/`. La app nativa **no** la carga (no usa `WebView`).

La maqueta es la referencia de diseño: la app nativa debe verse igual. Muestra canciones de ejemplo con carátula (Colegiala, Cumbia del Sol, Guaracadita), el haz láser, la cápsula, la mezcla de Automix y el doble toque ±10 s. Igual que la app:

- **Sin barra de arriba:** `.car-status-bar` con `display: none`; el reloj va en `.strip-clock` (`#stripClockH`, `#stripClockM`, `#stripUsbDot`) arriba de la tira, que mide 72 px.
- **Carátula de 263 px** (221 + los 42 px de la barra). El menú ☰ y Automix flotan a la derecha (`.player-top-bar` con `position: absolute`). Un toque no hace nada; dos en menos de 350 ms llaman a `toggleExplorer()`.
- **Cápsula:** `#nowPlayingPill` está fuera de `.album-art-container` (que recorta con `overflow: hidden`), dentro de `.album-art-wrap`, con `bottom: -19px`; el `margin-bottom: 19px` de ese envoltorio le reserva espacio. Sus estilos de estado usan el selector hermano (`.album-art-container.is-playing ~ .now-playing-pill`). La etiqueta `.audio-badge` va pegada a la esquina (`top/right: -1px`, `border-radius: 0 18px 0 10px`, fondo sólido sin `backdrop-filter`, que no se recortaba bien en la curva).
- **Franja de anterior / play / siguiente:** `navZoneAt()` con dos escuchas en captura sobre `#playerPanel`: `pointerdown` ilumina el botón de la columna (`.pressed`) y `click` reenvía al botón el clic que cayó fuera de él.
- **Menú ☰:** `#btnPlayerMenu`, `#playerMenu` y `#playerMenuBackdrop` dentro de `.player-panel`, con la fila `#menuRescan` (hace clic en el `#btnRescanUsb` oculto) y la versión en `#menuVersion`. Aleatorio y repetir se recuerdan en `localStorage` (`jhmusic_shuffle`, `jhmusic_repeat`).
- **Gesto de volumen** con el indicador flotante (`#blindVolumeHud`): `canStartBlindVolumeGesture()` no lo empieza en el buscador, la barra de progreso ni `.explorer-panel`.

Las páginas `demo-*.html` (animaciones de la carátula, transiciones de Automix, Mi Mix sonando, reordenar Mi Mix, controles para el auto, encabezado, sin barra, reloj en la tira) son muestrarios de diseño con variantes. Sirvieron para elegir el diseño y no forman parte de la app.

## 11. Última canción al abrir (2.9.0)

- En `SharedPreferences` (`jh_music`): `last_track_path`, `last_track_from_mix`, `shuffle_on` y `repeat_on`. No se guarda el segundo donde iba: así no se escribe nada mientras suena.
- `saveLastPlayback(track)` guarda solo al final de `onTrackStarted()` (una vez por canción).
- `restoreLastPlayback()` corre al final de cada escaneo (antes de `filterByTab()`), solo si todavía no se restauró y no hay nada cargado (`mediaPlayer == null`, `currentTrackIndex == -1`), o si lo cargado es la primera canción de respaldo sin tocar (`firstTrackFallbackPath`), que se libera. Arma la cola (Mi Mix si `last_track_from_mix` y la canción sigue en Mi Mix; si no, su carpeta con `selectFolderOf()`), y llama a `preparePaused()`: prepara el `MediaPlayer` sin `start()` (queda en 00:00) y llama a `onTrackStarted()` con `skipSaveLast` (cargar en pausa no cuenta como "la última que sonó").
- **Sin última canción (2.9.7):** `loadFirstTrackPaused()` carga igual la primera canción del pendrive con su carpeta (`selectFolderOf()`) y anota su ruta en `firstTrackFallbackPath`. Antes solo se escribía su nombre ("vista previa") sin cola: ▶ no hacía nada y la pestaña decía Canciones 0. Al tocar ▶ sobre ella, `togglePlayPause()` la guarda como última y borra `firstTrackFallbackPath`.

## 12. Menú ☰ del reproductor (2.8.0)

`btnPlayerMenu` abre `showPlayerMenu()`, un `PopupWindow` con `layout/popup_player_menu.xml` del tamaño de `layoutPlayerPanel` y encima de él:

- Es **sin foco** (con foco Android muestra la barra de navegación, porque la app va a pantalla completa) y con `setClippingEnabled(false)` (si no, Android lo sube para no pisar la barra de navegación oculta).
- `refreshPlayerMenu()` pinta ON / OFF de aleatorio y repetir y el texto Ocultar / Mostrar explorador. Aleatorio y repetir se guardan al cambiarlos (`shuffle_on`, `repeat_on`).
- **Re-escanear USB** (`menuRescan`) cierra el menú, muestra el aviso "Escaneando pendrives USB..." y llama a `startUsbScan()`. Al pie, `menuVersion` muestra `JH MUSIC v<versionName>` (del `PackageManager`).
- Se cierra a los 5 s con `closePlayerMenu` en `navTapHandler`; cada toque en aleatorio o repetir vuelve a contar.

## 13. Anterior / play / siguiente

- `ImageButton` de 74 / 80 dp (ícono de 50 / 56 dp) con fondo `bg_ctrl_icon` (círculo cian tenue con `state_pressed` y `state_activated`); `applyPlayerControlSizes()` los separa 24 / 48 dp del play. El play mide 88 / 100 dp (ícono de 40 / 44 dp).
- `handleNavZoneTouch()`, al inicio de `dispatchTouchEvent()`: un toque que empieza dentro de `layoutPlayerPanel` y debajo de `layoutProgressBlock` se reparte en tres columnas: `btnPlayPause` con 24 dp de más a cada lado (`PLAY_ZONE_EXTRA_DP`), `btnPrev` a su izquierda y `btnNext` a su derecha. Deja el botón `activated` hasta 180 ms después de soltar (el presionado de Android dura apenas 64 ms) y llama a `performClick()`, así el doble toque ±10 s sigue igual.

## 14. Gesto de volumen y su indicador (2.9.2)

- En `dispatchTouchEvent()`: al tocar, si el toque puede empezar el gesto (ver §8), guarda el punto y el volumen. Al moverse, si el movimiento es horizontal más de 14 dp se descarta; si es vertical más de 14 dp (y 1,2 veces más que el horizontal), el gesto toma el toque: 240 dp de recorrido = 100 % (`setVolumePercent(…, true)`).
- Cuando lo toma, manda un `ACTION_CANCEL` a la vista que estaba debajo del dedo (`super.dispatchTouchEvent`). Si no, esa vista quedaba presionada (la carátula se ponía naranja).
- El indicador (`layout_blind_volume_hud.xml`, 140 dp, centrado sobre `android.R.id.content`) muestra el ícono (rojo en silencio), la barra y el porcentaje, y se oculta 1,1 s después de soltar.
- No empieza sobre el explorador (`isTouchInExplorer`): ahí deslizar arriba / abajo es el scroll de la lista, también en el espacio vacío debajo.
