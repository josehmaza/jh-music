# Investigación: mezclador automático entre canciones

Complementa la propuesta [#12 de Propuestas de mejora](05-propuestas-de-mejora.md#12-mezcla-automática-entre-canciones). Reúne cómo lo resuelven otros reproductores y qué hay que cuidar para que la transición suene bien con distintos géneros, formatos y calidades en un radio de gama baja.

## 1. Objetivo

Que la música **no se detenga** entre canciones y que la transición **no se note como un error**: ni un hueco de silencio, ni dos canciones fuertes sonando encima una de otra, ni un salto brusco de volumen.

Cómo lo hacen otros:

| Reproductor | Qué ofrece |
|---|---|
| Spotify | **Crossfade** con duración configurable y **Automix**: transiciones con el ritmo sincronizado, solo en algunas playlists. Spotify no publica cómo lo calcula |
| Poweramp | Ajustes de fundido separados para cambio automático, salto manual, pausa y búsqueda, más un eliminador de silencios al inicio y al final. La mezcla "tipo radio" que detecta cuándo baja el volumen sigue pendiente en sus foros |

La propuesta para JH MUSIC toma de los dos: **fundidos distintos según la situación**, **recorte de silencios** y una **mezcla inteligente** que detecta el final real de cada canción. Lo que no conviene copiar es la sincronización de ritmo (ver §6).

## 2. La curva de mezcla

### Igual potencia (recomendada)

Si se baja una canción y se sube la otra en línea recta, a mitad de la mezcla el volumen total cae unos **3 dB**: se nota un "hueco". La curva de **igual potencia** mantiene constante el volumen percibido:

```
t = avance de la mezcla, de 0 a 1

volumen_sale  = cos(t · π/2)
volumen_entra = sin(t · π/2)
```

```
Volumen
1.0 ┤▀▀▄▄                ▄▄▀▀
    │    ▀▄            ▄▀
    │      ▀▄        ▄▀        ← se cruzan en 0,71 (no en 0,5): sin hueco
    │        ▀▄    ▄▀
    │          ▀▄▄▀
    │         ▄▀  ▀▄
0.0 ┤▄▄▄▄▄▀▀        ▀▀▄▄▄▄▄
    └──────────────────────── tiempo
     sale ↘           ↗ entra
```

La curva lineal solo sirve para fundir una canción consigo misma (por ejemplo, al repetir).

### Cómo aplicarla en Android

- **Android 8.0+ (`VolumeShaper`):** el sistema hace la rampa de volumen sin escalones audibles. Se crea un `VolumeShaper` por reproductor; no se pueden compartir, pero sí usar la misma configuración en los dos. Si hay varios sobre un mismo reproductor, sus efectos se multiplican. Eso permite combinar la mezcla con la bajada por el GPS (#2).
- **Android 5–7:** `MediaPlayer.setVolume()` en pasos de **≈50 ms**. Con pasos más largos se oye un "escalonado".

`setVolume()` recibe amplitud lineal (0–1), así que hay que pasarle el valor de la curva, no el porcentaje de avance.

## 3. Cuándo mezclar y cuándo no

No todas las transiciones deben tratarse igual.

| Situación | Transición recomendada |
|---|---|
| Fin normal de una canción | **Mezcla** con la duración configurada (por defecto 5 s) |
| La canción termina con su propio **fade-out** | Empezar la mezcla **cuando empieza el fade-out** y acortar la entrada de la siguiente |
| La canción termina **de golpe**, fuerte hasta el final (urbano, electrónica) | Mezcla **corta** (1,5–3 s) para no encimar dos canciones fuertes |
| Canciones seguidas del **mismo álbum** con número de pista consecutivo | **Sin pausas, sin mezcla**: álbumes en vivo o continuos se cortarían |
| **Mixes largos** (≥10 min, por ejemplo sets de DJ) | Mezcla corta (2 s) o sin pausas: ya vienen mezclados por dentro |
| Canción **corta** (menos de 3 veces la duración de la mezcla) | Sin mezcla: la mezcla se comería la canción |
| **Siguiente** o **Anterior** manual | Fundido rápido de 0,5–1 s |
| **Pausa** / reanudar | Fundido de 200–300 ms, solo para evitar el "clic" |
| **Repetir canción**, última de la lista, cola de una sola canción | Sin mezcla |
| **Aleatorio** | Mezcla normal, pero la siguiente se sortea **antes** de empezar la mezcla |
| El usuario **adelanta** dentro de la mezcla | Cancelar la mezcla y dejar solo la canción en la que adelantó |

## 4. Géneros

**El género no sirve como dato de entrada.** En las descargas de YouTube la etiqueta de género casi siempre viene vacía o mal puesta (`METADATA_KEY_GENRE`). Lo confiable es **analizar el audio** (§5), que se adapta solo:

| Música | Cómo suele terminar | Qué hace la mezcla inteligente |
|---|---|---|
| Reggaetón, urbano, electrónica | Corte seco, o un *outro* con el mismo ritmo | Detecta "fuerte hasta el final" y usa una mezcla corta |
| Cumbia, salsa, tropical | Fade-out o final en vivo con remate | Si hay fade-out, mezcla dentro de él; si es remate, mezcla corta |
| Baladas, pop lento | Fade-out largo | Mezcla larga (6–8 s) dentro del fade-out |
| Grabaciones en vivo | Aplausos y silencio | Recorta el silencio final; los aplausos quedan dentro de la mezcla |
| Clásica, álbumes conceptuales | Continuas entre pistas | Regla de "mismo álbum": sin pausas |

## 5. Mezcla inteligente: analizar el audio

### Qué medir

Para cada canción se decodifican a PCM los **últimos ~20 s** y los **primeros ~10 s**, con `MediaExtractor` + `MediaCodec`. Se calcula el nivel RMS en ventanas de 100 ms, en dBFS, y de ahí se sacan:

| Dato | Cómo se obtiene | Para qué |
|---|---|---|
| **Inicio real** | Primera ventana sobre −45 dBFS | Saltar el silencio inicial de la siguiente |
| **Fin real** | Última ventana sobre −45 dBFS | Ignorar el silencio final, que muchas canciones tienen de 1–3 s |
| **Inicio del fade-out** | Punto desde el que el nivel cae más de 10 dB respecto al promedio y sigue bajando | Empezar la mezcla ahí |
| **Final abrupto** | El nivel se mantiene alto hasta el fin real | Usar mezcla corta |
| **Nivel promedio** | Media RMS de lo analizado | Igualar volúmenes (§7) |

### Cuándo analizar (clave en gama baja)

Analizar toda la biblioteca de entrada sería lento: unos 0,2–0,5 s por canción en un procesador débil, varios minutos con cientos de canciones. En su lugar:

1. **Bajo demanda:** al empezar a sonar una canción, se analiza en segundo plano **su final y el inicio de la siguiente** en la cola. Hay de sobra para tenerlo listo antes de la mezcla.
2. **Guardar el resultado** con la biblioteca en caché (propuesta #6), para no repetirlo.
3. **Si el análisis no terminó** (por ejemplo, un salto rápido), usar la mezcla básica con duración fija.

## 6. Lo que NO conviene hacer en este radio

- **Sincronizar el ritmo (beat matching, como el Automix de Spotify).** Requiere detectar el tempo de cada canción y estirar el audio en tiempo real sin cambiar el tono. `MediaPlayer` solo puede cambiar la velocidad (`setPlaybackParams`, Android 6+), con calidad pobre y mucho costo de CPU en hardware débil. El resultado probable son transiciones "arrastradas", peores que una buena mezcla de igual potencia.
- **Mezclas largas (más de 8 s) por defecto.** En el auto, con ruido de motor y de calle, dos canciones encimadas durante mucho tiempo se perciben como ruido.

## 7. Igualar volúmenes

Las descargas de YouTube tienen volúmenes muy distintos. Si la siguiente canción suena 6 dB más fuerte, la mezcla termina en un salto brusco.

| Opción | Ventajas | Desventajas |
|---|---|---|
| Etiquetas **ReplayGain** del archivo | Estándar (ReplayGain 2.0 usa −18 LUFS de referencia) | Casi ningún archivo descargado las trae, y `MediaMetadataRetriever` no las lee: haría falta leer las etiquetas ID3 a mano |
| **Nivel promedio del análisis** (§5) | Ya se calcula; funciona con cualquier archivo | Es una aproximación (RMS de unos segundos), no una medición LUFS exacta |

**Recomendación:** usar el nivel del análisis y aplicar una corrección de **±6 dB como máximo**:
- **Bajar** las canciones fuertes con `setVolume()` (como mucho 1,0).
- **Subir** las suaves requiere `LoudnessEnhancer`. Hay que usarlo con cuidado: en parlantes de auto, subir mucho distorsiona.
- Ofrecerlo como opción aparte: "Igualar volumen entre canciones".

## 8. Formatos, bitrate y frecuencia de muestreo

| Caso | ¿Afecta la mezcla? | Qué hacer |
|---|---|---|
| **Bitrates distintos** (128 vs 320 kbps) | No. Se oye la diferencia de calidad, pero no hay problema técnico | Nada |
| **Frecuencias distintas** (44,1 vs 48 kHz) | No. Android convierte cada reproductor a la frecuencia de salida antes de mezclar | Nada; cuesta un poco de CPU |
| **FLAC / WAV de alta resolución** (24 bit / 96 kHz) | Decodificar y convertir dos a la vez pesa más en gama baja | Si alguna supera 48 kHz, acortar la mezcla a ≤2 s o pasar a sin pausas |
| **WAV** | Mucha lectura del pendrive (≈1,4 Mbps a 44,1 kHz, ≈4,6 Mbps a 96 kHz/24 bit) | USB 2.0 aguanta dos a la vez sin problema |
| **MP3 VBR sin cabecera Xing/VBRI** | `getDuration()` puede equivocarse por varios segundos y la mezcla arrancaría a destiempo | Usar el **fin real** del análisis (§5). Si la canción termina antes de lo previsto, arrancar la siguiente de inmediato (`OnCompletionListener` como respaldo) |
| **Relleno del codificador** (MP3 y AAC, decenas de ms) | No afecta la mezcla; solo importa en el modo sin pausas | En sin pausas, probar en el radio si Android recorta ese relleno; no está confirmado para su decodificador |
| **Mono / estéreo** | No | Nada |

## 9. Precarga para pendrives lentos

- **Preparar la siguiente canción con anticipación**, 15–20 s antes de que empiece la mezcla, con `prepareAsync`. Así la latencia del pendrive no retrasa la entrada.
- **Un solo reproductor extra** a la vez: unos pocos MB de RAM.
- **Si la siguiente no está lista** cuando toca mezclar, esperar a que esté y hacer un fundido corto. Nunca quedarse en silencio.

## 10. Coordinación con el resto de la app

| Parte | Qué hacer durante la mezcla |
|---|---|
| Foco de audio (#2) | La bajada por el GPS se aplica a **los dos** reproductores |
| Ecualizador (#13) | Aplicarlo a las dos sesiones de audio |
| Pantalla y MediaSession (#3) | Pasar a la canción nueva **a mitad de la mezcla**, no al final |
| Cola (`playQueue`) | Avanzar el índice a mitad de la mezcla |
| Siguiente durante la mezcla | Terminar la mezcla de inmediato y quedarse en la canción nueva |
| Anterior durante la mezcla | Cancelar la mezcla y volver a la canción que salía |
| Pendrive retirado | Detener los dos reproductores (como hoy en `reconcileListsAfterRescan`) |

## 11. Ajustes propuestos

```
Transición entre canciones
  ○ Desactivada
  ○ Sin pausas
  ● Mezcla
      Duración ........................ [ 5 s ]   (1–12 s)
      Mezcla inteligente .............. [ ✓ ]     (detecta fade-out y silencios)
      No mezclar álbumes completos .... [ ✓ ]
      Igualar volumen entre canciones . [   ]
```

## 12. Pruebas recomendadas en el radio

| Prueba | Qué verificar |
|---|---|
| Dos reggaetón fuertes con final seco | Que no se encimen fuerte; mezcla corta |
| Balada con fade-out largo | Que la mezcla empiece dentro del fade-out |
| Canción en vivo con aplausos y silencio final | Que el silencio no genere hueco |
| Canción con 3 s de silencio al inicio | Que la entrada no tenga hueco |
| MP3 a 44,1 kHz seguido de uno a 48 kHz | Sin cortes ni cambio de tono |
| FLAC 96 kHz seguido de MP3 | Que el radio no se trabe; mezcla acortada |
| MP3 VBR sin cabecera Xing | Que la mezcla no empiece tarde ni corte la canción |
| Mix de DJ de 60 min | Mezcla corta o sin pausas |
| Canción de 10 s | Sin mezcla |
| Siguiente / Anterior en medio de una mezcla | Respuesta inmediata, sin dos canciones colgadas |
| Indicación del GPS durante una mezcla | Bajan las dos canciones |
| Pendrive retirado en medio de una mezcla | Silencio limpio, sin cierre de la app |

## 13. Estado de la implementación

**Fase 1 implementada** en `AutoMixer.java`, activada con el botón Automix bajo la lupa.

| Punto | Cómo quedó |
|---|---|
| Curva | Igual potencia (cos/sin) con `setVolume()` en pasos de 50 ms en todas las versiones de Android. `VolumeShaper` queda como mejora posible |
| Duración | 5 s fijos (`DEFAULT_MIX_MS`); 2 s en canciones de 10 min o más; sin mezcla si alguna de las dos dura menos de 3 veces la mezcla |
| Precarga | La siguiente se prepara con `prepareAsync` 15 s antes de la mezcla |
| Respaldo | Si la canción termina antes de lo previsto, la siguiente ya preparada entra con un fundido de 300 ms |
| Cambio de pantalla | La canción nueva se muestra **al empezar** la mezcla (no a la mitad, como proponía la §10) |
| Pausa / adelantar / Siguiente / Anterior durante la mezcla | Terminan la mezcla de inmediato |
| Aleatorio | La siguiente se sortea al precargar, nunca la misma canción |
| Repetir, cola de 1 | Sin mezcla |

**Fase 2 implementada** en `AudioAnalyzer.java`. Motivo: al probar la fase 1, en las canciones con un final largo que se apaga, la mezcla en los últimos 5 s ocurría cuando la canción ya casi no se oía.

| Punto | Cómo quedó |
|---|---|
| Qué se analiza | Primeros 10 s y últimos 25 s, decodificados a PCM con `MediaExtractor` + `MediaCodec`; nivel RMS cada 100 ms, suavizado a 1 s |
| Decodificador | **Por software** (`OMX.google.*` / `c2.android.*`), para no quitarle decodificadores por hardware a la música. Si no hay, el del sistema |
| Hilo | Uno solo, de baja prioridad, una canción a la vez, con límite de 10 s por canción |
| Cuándo | Al empezar a sonar una canción (su final) y al precargar la siguiente (su inicio), solo con Automix activo. Resultados en memoria, hasta 300 canciones |
| Fade-out | Si la canción baja más de **8 dB** respecto a su parte fuerte durante más de 1,5 s antes del fin real, la mezcla empieza ahí y dura lo que dure el apagado (entre 2 y 5 s) |
| Final seco | Mezcla de 3 s que termina en el fin real |
| Silencios | Se ignora el silencio final (menos de −45 dBFS) y la canción que entra salta su silencio inicial (máx. 8 s) |
| Si falla | Se marca como "sin análisis" y se usa la mezcla de la fase 1 |

Los umbrales (`SILENCE_DB`, `FADE_DROP_DB`, `FADE_MIN_MS`, `ABRUPT_MIX_MS`) están en `AudioAnalyzer` y `AutoMixer` para ajustarlos al probar con música real. Un efecto a vigilar: una canción con un final tranquilo intencional (por ejemplo, un piano suave) se mezclaría desde que empieza esa parte tranquila.

Pendiente: pantalla de ajustes (§11), igualar volumen (fase 3, ya se mide `referenceDb`), modo sin pausas (fase 4) y regla de "mismo álbum". Esta última necesita etiquetas reales de álbum y número de pista; hoy las pistas encontradas recorriendo carpetas usan el nombre de la carpeta como álbum.

## 14. Plan por fases

| Fase | Contenido | Esfuerzo |
|---|---|---|
| 1 | Mezcla de igual potencia con duración fija, precarga, casos de la §3 y respaldo con `OnCompletionListener` | Medio |
| 2 | Análisis bajo demanda (§5): recorte de silencios, detección de fade-out y de final abrupto | Medio |
| 3 | Igualar volumen (§7) | Bajo, sobre la fase 2 |
| 4 | Modo sin pausas con `setNextMediaPlayer`, probando su comportamiento en el radio | Bajo |
| — | Sincronizar el ritmo | **No recomendado** (§6) |

Requisito previo: servicio de reproducción (#3) y `prepareAsync` (#4).

## Fuentes

- [Spotify — Transiciones entre canciones (Crossfade y Automix)](https://support.spotify.com/us/article/crossfade-feature/)
- [Android Developers — Controlar la amplitud con VolumeShaper](https://developer.android.com/media/platform/volumeshaper?hl=de)
- [Android Developers — VolumeAutomation (API 26)](https://d.android.com/reference/android/media/VolumeAutomation)
- [Web Audio API (O'Reilly) — Cap. 3: Volumen y sonoridad, curvas de igual potencia](https://www.oreilly.com/library/view/web-audio-api/9781449332679/ch03.html)
- [NoMercy Music Player — Crossfade y gapless](https://docs.nomercy.tv/nomercy-music-player/recipes/crossfade-and-gapless)
- [Poweramp — Opciones de crossfade (foro)](https://forum.powerampapp.com/topic/4940-crossfade-options/)
- [Poweramp — "Crossfade needs to be redesigned" (foro)](https://forum.powerampapp.com/topic/24734-crossfade-needs-to-be-redesigned/)
- [Wikipedia — Gapless playback (relleno del codificador en MP3)](https://en.wikipedia.org/wiki/Gapless_playback)
- [Android Developers — Media3 GaplessInfoHolder](https://developer.android.com/reference/androidx/media3/extractor/GaplessInfoHolder)
- [loudgain — Normalizador ReplayGain 2.0 (−18 LUFS)](https://mirror.hs-esslingen.de/Mirrors/archive.rpmfusion.org/free-archive/fedora/updates/33/SRPMS/repoview/loudgain.html)
- [Hydrogenaudio — ReplayGain vs EBU R128](https://hydrogenaud.io/index.php?msg=839951)
