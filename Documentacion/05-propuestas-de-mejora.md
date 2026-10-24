# Propuestas de mejora — JH MUSIC

Análisis hecho sobre la versión 2.0.1, con el estado actualizado a la **2.9.7**. Piensa en dos condiciones: uso **mientras se maneja** y un radio de **gama baja** (CPU lenta, poca RAM, pantalla de 1024×600 con poco brillo y contraste).

Cada propuesta indica el problema actual, la solución y el esfuerzo estimado. Están ordenadas por impacto. Cada una pendiente tiene su **issue** en GitHub (etiqueta [`propuesta`](https://github.com/josehmaza/jh-music/issues?q=label%3Apropuesta)) para decidir si se hace; si se descarta, el issue se cierra como *not planned*.

| # | Propuesta | Prioridad | Esfuerzo | Estado |
|---|---|---|---|---|
| 1 | Retomar donde quedaste al encender el auto | 🔴 Alta | Bajo | **Hecha en 2.9.0** (la última canción, en pausa y desde el inicio; no se guarda el segundo) |
| 2 | Bajar la música con el GPS y pausar en llamadas (foco de audio) | 🔴 Alta | Bajo | Pendiente · [issue 1](https://github.com/josehmaza/jh-music/issues/1) |
| 3 | Reproducción independiente de la pantalla (servicio + MediaSession) | 🔴 Alta | Medio | Pendiente · [issue 2](https://github.com/josehmaza/jh-music/issues/2) |
| 4 | Una canción dañada no traba la reproducción | 🔴 Alta | Bajo | Pendiente · [issue 3](https://github.com/josehmaza/jh-music/issues/3) |
| 5 | Carátulas reducidas al tamaño del cuadro | 🟠 Media | Muy bajo | Pendiente · [issue 4](https://github.com/josehmaza/jh-music/issues/4) |
| 6 | Biblioteca guardada para arrancar al instante | 🟠 Media | Medio | Pendiente · [issue 5](https://github.com/josehmaza/jh-music/issues/5) |
| 7 | Animación del ecualizador a menor velocidad | 🟠 Media | Muy bajo | **En parte** (las animaciones de la carátula van a ~30 fps; falta el spinner de búsqueda) · [issue 6](https://github.com/josehmaza/jh-music/issues/6) |
| 8 | Carpeta siguiente / anterior con pulsación larga en pantalla | 🟡 Usabilidad | Bajo | Pendiente · [issue 7](https://github.com/josehmaza/jh-music/issues/7) |
| 9 | Modo de alto contraste para sol y pantalla de bajo brillo | 🟡 Usabilidad | Medio | Pendiente · [issue 8](https://github.com/josehmaza/jh-music/issues/8) |
| 10 | Modo "Ahora suena" a pantalla completa | 🟡 Usabilidad | Bajo | Pendiente · [issue 9](https://github.com/josehmaza/jh-music/issues/9) |
| 11 | Volumen de arranque limitado | 🟡 Usabilidad | Bajo | Pendiente · [issue 10](https://github.com/josehmaza/jh-music/issues/10) |
| 12 | Mezcla automática entre canciones (crossfade / sin pausas) | 🎵 Audio | Medio | **Fases 1, 2 y 3 hechas** (Automix: mezcla, mezcla inteligente e igualar volumen). Faltan sin pausas y ajustes · [issue 11](https://github.com/josehmaza/jh-music/issues/11) |
| 13 | Ecualizador | 🎵 Audio | Medio | Pendiente · [issue 12](https://github.com/josehmaza/jh-music/issues/12) |
| 14 | Extras opcionales | 🟢 Opcional | Variable | Pendiente |
| 15 | Recortar las franjas negras de las carátulas | 🟡 Usabilidad | Bajo | Pendiente (para más adelante) · [issue 13](https://github.com/josehmaza/jh-music/issues/13) |

---

## 🔴 Prioridad alta: problemas reales al manejar

### 1. Retomar donde quedaste al encender el auto

**Estado (2.9.0).** Hecha: al abrir, queda cargada en pausa la última canción con su lista (su carpeta o Mi Mix), y se recuerdan aleatorio, repetir y Automix. No se guarda el segundo donde iba: se decidió no escribir en la memoria mientras suena (ver [arquitectura §11](02-arquitectura-tecnica.md#11-última-canción-al-abrir-290)).

**Problema (antes).** Cada vez que se apaga y prende el auto, la app arranca de cero: sin carpeta, sin canción y con la pestaña de canciones en 0. No guarda la sesión; lo único que guarda es Mi Mix.

**Por qué importa.** Hay mixes de más de 14 minutos. Perder el punto cada vez que se baja del auto es molesto.

**Propuesta.**
- Guardar en `SharedPreferences` la carpeta activa, la cola, la canción y el segundo exacto. Hacerlo al pausar, al cambiar de canción y cada pocos segundos mientras suena.
- Al abrir la app, retomar automáticamente, o mostrar "Continuar: *Tema X* (02:15)".
- Si el pendrive de esa canción no está conectado, arrancar normal.

**Esfuerzo:** bajo.

---

### 2. Bajar la música cuando habla el GPS o entra una llamada

**Problema.** La app no pide **foco de audio** (no hay `requestAudioFocus` en el código):
- Cuando Waze o Google Maps dan una indicación, la música no baja de volumen y la voz queda tapada.
- Con una llamada por Bluetooth, la música puede seguir sonando encima.

**Propuesta.**
- Pedir el foco de audio al empezar a reproducir.
- `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK` (indicación del GPS): bajar el volumen de la música mientras dura.
- `AUDIOFOCUS_LOSS_TRANSIENT` (llamada): pausar y reanudar al terminar.
- `AUDIOFOCUS_LOSS` (otra app de música): pausar.

**Esfuerzo:** bajo. En un auto es seguramente lo más importante.

---

### 3. Reproducción independiente de la pantalla

**Problema.** La reproducción vive dentro de la pantalla (`Activity`) y las teclas multimedia solo se atienden con la app al frente (`onKeyDown`). En un radio con poca RAM:
- Si se abre el GPS encima, Android puede **cerrar la app para liberar memoria** y la música se corta.
- Con el GPS al frente, un control Bluetooth o multimedia no respondería (el auto no tiene botones en el volante, así que esto es secundario).

**Propuesta.** Pasar la reproducción a un **servicio en primer plano** con `MediaSession`, el estándar de Android para reproductores:
- La música sigue con la app minimizada o con el GPS al frente.
- Los controles de la notificación y de un control Bluetooth funcionan desde cualquier pantalla.
- Aparece una notificación con título y controles.
- La pantalla pasa a ser solo el control remoto del servicio.

**Esfuerzo:** medio. Es el cambio más grande de la lista, pero es lo que distingue a un reproductor de auto confiable.

---

### 4. Una canción dañada no debe trabar la reproducción

**Problema.**
- No hay manejo de errores del reproductor (`setOnErrorListener`). Un MP3 dañado, o un pendrive que se mueve con un bache, puede dejar la reproducción detenida en silencio.
- `mediaPlayer.prepare()` corre en el hilo de la pantalla. Con un pendrive lento, la app **se congela** un momento en cada cambio de canción, y en el peor caso Android muestra "La app no responde".

**Propuesta.**
- Preparar la canción en segundo plano (`prepareAsync` + `OnPreparedListener`).
- Si falla, mostrar un aviso breve ("No se pudo reproducir *X*") y saltar a la siguiente.
- Si fallan varias seguidas (por ejemplo, se quitó el pendrive), detenerse en lugar de recorrer toda la lista.

**Esfuerzo:** bajo.

---

## 🟠 Prioridad media: rendimiento en hardware de gama baja

### 5. Carátulas reducidas al tamaño del cuadro

**Problema.** `BitmapFactory.decodeByteArray` carga la carátula embebida en su tamaño original. Una carátula de 3000×3000 ocupa unos **36 MB de RAM** para mostrarse en un cuadro de ~150 px. En un radio de 1–2 GB eso provoca lentitud o cierres por falta de memoria.

**Propuesta.** Leer primero solo las dimensiones (`inJustDecodeBounds`) y decodificar reducida con `inSampleSize` al tamaño del cuadro. Liberar la carátula anterior al cambiar de canción.

**Esfuerzo:** muy bajo, con mucho impacto.

---

### 6. Biblioteca guardada para arrancar al instante

**Problema.** Cada arranque recorre los pendrives completos y consulta MediaStore. Con dos pendrives esto tarda varios segundos.

**Propuesta.**
- Guardar en disco el resultado del escaneo: pistas, carpetas, duraciones y bitrate.
- Al arrancar, mostrar la biblioteca guardada de inmediato.
- En segundo plano, verificar si algo cambió comparando las fechas de modificación de las carpetas, y re-escanear solo lo que cambió.

**Resultado:** la app quedaría lista en menos de 1 segundo, y las duraciones ya leídas no se vuelven a leer.

**Esfuerzo:** medio.

---

### 7. Animación del ecualizador a menor velocidad

**Estado (2.4.0).** `SoundWaveBarsView` ya no se usa. El haz láser, la cápsula y la marquesina se redibujan a unos 30 cuadros por segundo (`postInvalidateDelayed(33)`) y solo mientras hace falta. Queda el spinner de búsqueda, que sigue a 60 por segundo (`postInvalidateDelayed(16)`) mientras busca, y falta detener las animaciones cuando la app no está visible.

**Problema original.** `SoundWaveBarsView` se redibujaba en cada cuadro (`postInvalidateOnAnimation`, unos 60 por segundo) mientras sonaba música. En una CPU débil eso compite con la interfaz y con el GPS.

**Propuesta.** Limitar la animación a 15–20 cuadros por segundo, donde se ve igual, o agregar una opción para apagarla. Detenerla cuando la app no está visible.

**Esfuerzo:** muy bajo.

---

## 🟡 Usabilidad al manejar

### 8. Carpeta siguiente / anterior con pulsación larga en pantalla

**Problema.** Con varias carpetas (Descargas, MIX, ENG…), cambiar de carpeta obliga a mirar la pantalla y buscar en el árbol. El auto no tiene botones en el volante.

**Propuesta.**
- **Mantener presionado** Siguiente o Anterior **en la pantalla** (unos 0,6 s) para pasar a la carpeta siguiente o anterior y reproducir su primera canción. Un toque corto sigue cambiando de canción.
- Mostrar el nombre de la carpeta en grande durante 2–3 segundos.

**Esfuerzo:** bajo.

---

### 9. Modo de alto contraste para sol y pantalla de bajo brillo

**Problema.** En las fotos del radio, los textos grises (`text_muted #64748b`) y los acentos azules sobre fondo oscuro se ven lavados, sobre todo con sol.

**Propuesta.**
- Un **modo día de alto contraste**: texto blanco puro, grises más claros, bordes más gruesos y tipografía en negrita.
- Activación automática según la hora o manual con un botón.
- Revisar que ningún texto importante use colores con poca opacidad.

**Esfuerzo:** medio. Hay que revisar colores en `colors.xml`, los layouts y los colores fijos en Java.

---

### 10. Modo "Ahora suena" a pantalla completa

**Problema.** El reproductor comparte la pantalla con la tira de volumen y el explorador. Los textos y botones podrían ser más grandes.

**Propuesta.** Con un toque, ocultar todo y dejar título y artista en letra muy grande, una barra de progreso gruesa y los botones Anterior / Play / Siguiente enormes. Ya existe algo parecido al ocultar el explorador; la idea es llevarlo un paso más allá, para usarlo sin quitar la vista del camino.

**Esfuerzo:** bajo.

---

### 11. Volumen de arranque limitado

**Problema.** Si el volumen quedó alto, por ejemplo al 90 %, al encender el auto la música suena de golpe a todo volumen.

**Propuesta.** Al abrir la app, si el volumen supera un límite (por ejemplo 40 %), bajarlo a ese límite. Empezar la primera canción con un fundido de 1–2 segundos.

**Esfuerzo:** bajo.

---

### 15. Recortar las franjas negras de las carátulas

**Problema.** Muchas canciones bajadas de YouTube traen como carátula la miniatura del video, que ya viene con **barras negras arriba y abajo** (o a los costados) dentro de la imagen. La app la muestra tal cual, así que la foto no llena el cuadro y queda una franja oscura (visto en el auto con "A LA VOZ DEL CARNAVAL" de Mary Murillo). Las carátulas sin franjas, como la de DJ MAI, sí llenan el cuadro: no es un error de la app.

**Propuesta.** Al cargar la carátula, en segundo plano (`loadAlbumArt`), detectar las franjas y recortarlas antes de mostrarla:
- Revisar filas desde arriba y desde abajo (y columnas desde los costados) con unas 32 muestras por línea: es franja si el brillo es muy bajo (menos de ~32 sobre 255) y parejo (poca diferencia entre muestras).
- Recortar solo si hay franja **en los dos lados opuestos**, de al menos 4 % cada una y de tamaño parecido, como las barras de un video. Así una foto oscura solo arriba (un cielo de noche) no se toca.
- No recortar si quedaría menos de la mitad de la imagen.

**Riesgo.** Una carátula con un marco negro a propósito podría perderlo. Es poco común.

**Costo en gama baja:** muy bajo: una vez por canción, en segundo plano.

**Esfuerzo:** bajo. El código se llegó a escribir y compilar en la 2.6.1 (sin probarlo en el auto) y se quitó a pedido, para dejarlo para más adelante.

---

## 🎵 Audio

### 12. Mezcla automática entre canciones

**Estado (2.4.0).** Implementada como **Automix** (botón arriba a la derecha del reproductor), antes de #3 y #4:
- Fase 1: mezcla de igual potencia de 5 s, con precarga.
- Fase 2: análisis del audio para mezclar donde la canción se apaga y saltar silencios.
- Fase 3: igualar el volumen entre canciones.

Faltan el modo **sin pausas** (fase 4) y la pantalla de **ajustes** (duración, mezcla inteligente, igualar volumen). Detalle en [06 · Investigación del mezclador](06-investigacion-mezclador.md#13-estado-de-la-implementación). Lo que sigue es la propuesta original.

**Problema.** Al terminar una canción, la siguiente arranca recién cuando la anterior se detuvo del todo (`OnCompletionListener` → `playNextTrack()` → `prepare()`). Queda un silencio, más largo con un pendrive lento, que corta el ambiente, sobre todo con mixes y música bailable.

**Propuesta: dos modos, elegibles en ajustes.**

| Modo | Qué hace | Cómo se implementa |
|---|---|---|
| **Sin pausas** (*gapless*) | La siguiente canción empieza justo cuando termina la actual, sin silencio ni mezcla | `MediaPlayer.setNextMediaPlayer()`: Android prepara la siguiente por adelantado y la encadena solo. Disponible desde Android 4.1 |
| **Mezcla** (*crossfade*) | Los últimos N segundos de una canción se superponen con el inicio de la siguiente: una baja mientras la otra sube | Dos `MediaPlayer`. N segundos antes del final se prepara y arranca el segundo en volumen 0 y se cruzan los volúmenes (`setVolume`) en pasos de ~100 ms. Duración configurable: 2, 4, 6 u 8 s |

**Detalles a cuidar.**
- **Conocer la siguiente por adelantado:** la cola ya existe (`playQueue`). En aleatorio hay que sortear la siguiente antes, no al terminar.
- **Siguiente / Anterior manual:** cortar la mezcla en curso y saltar directo, o hacer una mezcla corta de 1 s.
- **Pausa durante una mezcla:** pausar los dos reproductores.
- **Repetir canción, última canción de la lista o una sola canción:** no mezclar.
- **Foco de audio (#2):** la bajada por el GPS debe aplicarse a los dos reproductores.
- **Silencio al final de los archivos:** muchas descargas tienen 1–3 s de silencio al final o al inicio. Una mejora posterior sería detectarlo y empezar la mezcla antes.

**Costo en gama baja.** Durante la mezcla se decodifican dos canciones a la vez por unos segundos. Decodificar MP3 es liviano incluso en estos radios, y leer dos archivos del pendrive en paralelo no es problema. El modo **Sin pausas** prácticamente no tiene costo extra.

**Esfuerzo:** medio. Sin pausas es bajo; la mezcla agrega el manejo de dos reproductores.

**Conviene hacerlo después de #3 (servicio + MediaSession) y #4 (`prepareAsync`)**, porque los tres cambian el mismo código de reproducción.

📄 **Investigación detallada:** [06 · Investigación del mezclador](06-investigacion-mezclador.md). Cubre curvas, cuándo mezclar, géneros, análisis de audio, volumen, formatos y bitrate, pruebas y plan por fases.

---

### 13. Ecualizador

**Propuesta.**
- **Preajustes:** Normal, Rock, Pop, Reggaetón / Urbano, Cumbia / Tropical, Graves+, Voces, y uno **Personalizado**.
- **Bandas:** las que ofrezca el radio, normalmente 5 (60 Hz, 230 Hz, 910 Hz, 3,6 kHz y 14 kHz), con deslizadores verticales grandes, cómodos al tacto.
- **Refuerzo de graves** (`BassBoost`) y **realce de volumen** (`LoudnessEnhancer`, Android 4.4+) como controles aparte.
- Recordar el preajuste elegido entre sesiones.
- Acceso con un botón en el reproductor; la pantalla del ecualizador se cierra sola tras unos segundos sin tocarla.

**Cómo se implementa.** `android.media.audiofx.Equalizer` sobre el `audioSessionId` del `MediaPlayer`. Los preajustes y bandas reales se leen del dispositivo (`getNumberOfBands()`, `getBandLevelRange()`), porque varían entre radios. Con la mezcla (#12) activa, el ecualizador debe aplicarse a los dos reproductores.

**Precauciones en estos radios.**
- **Muchos radios chinos ya tienen ecualizador o DSP propio** en el sistema. Si se usan los dos, los efectos se suman: graves excesivos o distorsión. Conviene un aviso en la pantalla del ecualizador: "Si tu radio tiene ecualizador propio, usa solo uno".
- **Algunos firmwares ignoran `audiofx`.** Hay que verificar al abrir si el efecto se pudo crear (`Equalizer` lanza excepción si no) y, si no, ocultar la función.
- **No pasar de ~+10 dB por banda:** en parlantes de auto y amplificadores básicos se distorsiona fácil.
- **Convivir con Automix:** el igualado de volumen ya usa `LoudnessEnhancer` en cada reproductor (`LoudnessLeveler`). Un control de realce aparte debe sumarse a esa corrección, no crear un segundo efecto sobre la misma sesión.

**Costo en gama baja:** bajo. El ecualizador corre en el procesamiento de audio del sistema.

**Esfuerzo:** medio, principalmente por la pantalla y los preajustes. La parte de audio es poca.

---

## 🟢 Extras opcionales

| Idea | Descripción |
|---|---|
| Recientes / Más escuchadas | Pestaña o chip con lo último y lo más reproducido · [issue 14](https://github.com/josehmaza/jh-music/issues/14) |
| Fundido al pausar o reanudar | Bajada o subida suave en lugar de un corte seco (reutiliza la lógica de la mezcla #12) · [issue 15](https://github.com/josehmaza/jh-music/issues/15) |
| Volumen normalizado | **Hecho en 2.4.0** dentro de Automix (fase 3 del mezclador). Pendiente: poder usarlo sin la mezcla · [issue 16](https://github.com/josehmaza/jh-music/issues/16) |
| Reproducción automática al encender | Retomar la música sola al prender el radio (complementa la #1) · [issue 17](https://github.com/josehmaza/jh-music/issues/17) |

---

## Orden de implementación sugerido

| Paso | Propuestas | Motivo |
|---|---|---|
| 1 | #2 Foco de audio, #4 errores y `prepareAsync`, #5 carátulas | Corrigen fallos que hoy pueden pasar manejando; poco código |
| 2 | #1 Retomar sesión | Mejora inmediata en el uso diario |
| 3 | #3 Servicio + MediaSession | Cambio estructural; conviene hacerlo antes de seguir agregando funciones |
| 4 | #13 Ecualizador (#12 Mezcla ya está hecha) | Se apoya en el servicio de reproducción del paso 3 |
| 5 | #6 Biblioteca en caché, #8 carpeta con pulsación larga | Rendimiento y comodidad |
| 6 | #7, #9, #10, #11 y extras | Pulido visual y opciones |
