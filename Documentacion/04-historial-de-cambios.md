# Historial de cambios — JH MUSIC

Versiones de la más nueva a la más antigua. Entre paréntesis, el commit de git.

## 2.7.0 — 2026-10-18

- **Reordenar canciones en Mi Mix (Apertura de Hueco en Vivo + Drag & Drop táctil ≡):**
  - **Apertura de hueco en vivo (Live Gap):** al arrastrar una pista, las demás canciones se deslizan en tiempo real hacia arriba o abajo abriendo el espacio exacto donde caerá.
  - **Motor nativo de alto rendimiento (Android):** implementado con `RecyclerView` e `ItemTouchHelper` oficial de Google, acelerado en el **RenderThread de la GPU a 60 fps**, garantizando cero sobrecarga en la CPU de la radio.
  - **Motor web sincronizado:** implementado con arquitectura FLIP y tarjeta flotante sólida 100% opaca con elevación 3D (cero textos encimados ni desajustes visuales).
  - **Auto-Scroll continuo en bordes:** al arrastrar una pista cerca del borde superior o inferior (70px), la lista se desplaza sola a velocidad adaptativa para reorganizar listas largas cómodamente.
  - **Botón `[ ⇅ ]` en cabecera:** conmuta el modo reordenar.
  - **Cero pérdida de espacio:** la pantalla conserva el 100% del ancho para los títulos largos; la `✕` (quitar) se transforma en la manija `≡` en el mismo espacio.
  - **Aterrizaje magnético:** al soltar, la pista se imanta en el hueco y confirma la posición con un destello neón cian.
  - Botón **`[ ✓ Listo ]`** para finalizar y guardar en memoria persistente.

## 2.6.1 — 2026-10-17

- **Mi Mix:** el botón Reproducir queda solo con ▶ y el resumen dice "2 sonaron", para que entre completo.
- **Se sabe cuándo suena Mi Mix:** la pestaña Mi Mix muestra un punto cian que late mientras suena (quieto en pausa), y debajo del título dice "Mi Mix · 2 de 4" en lugar de la carpeta.
- **Al volver a Pistas o Mi Mix**, la lista se ubica en la canción que está sonando: arriba una canción antes, luego la que suena y luego las demás (antes empezaba arriba y había que buscarla).
- **Tocar el filtro de la carpeta actual** lleva la lista a la canción que suena, ubicada igual: sirve para encontrarla si moviste la lista.

## 2.6.0 — 2026-10-17 (`bf4139a`)

Solo diseño, sin cambios de funcionamiento. La app queda igual que la maqueta web:

- **Controles estilo YouTube Music:** play/pausa en un círculo blanco grande (76dp; 88dp con el explorador oculto), anterior/siguiente y aleatorio/repetir como íconos blancos sin caja. Aleatorio y Repetir activos se ven en cian con un puntito debajo.
- **Barra de progreso fina** blanca con un punto blanco; tiempos en blanco suave.
- **Fondo con el color de la carátula** en toda la app (también el explorador), con los paneles translúcidos. Cambia con un fundido de 1,2 s; en una mezcla de Automix, en lo que dura la mezcla.
- **Resplandor de la carátula** con el color de la canción (cian sin carátula). El resto de lo resaltado sigue en cian.
- **Botón Automix ON/OFF:** cian lleno con brillo y la palabra ON; apagado, oscuro con OFF.
- **Íconos propios iguales a los de la web** en lugar de los del sistema Android: controles, tarjetas Anterior/Siguiente, lupa, pestañas (carpeta, lista, lista con +), silencio (rojo cuando está activo), buscador y su ×, y carpetas en la búsqueda.
- **Pestaña elegida en cian lleno** con texto oscuro, como la web.

## 2.5.0 — 2026-10-17 (pendiente de commit)

- **Mi Mix reemplaza a Favoritos:** una lista que armas con **+** (en cada canción o en una carpeta entera) y que suena seguida, en su orden, con Automix, como la lista de Automix de Virtual DJ.
  - Lo que agregas mientras suena entra al final de la cola en vivo.
  - Al terminar la última se detiene (`■ Fin de Mi Mix`).
  - Las que ya sonaron quedan marcadas con ✓.
  - Cabecera con resumen, **▶ Reproducir** y **Vaciar** con confirmación. Al vaciar o quitar la que suena, esa canción sigue hasta el final.
  - Las estrellas de Favoritos pasan a Mi Mix la primera vez.

## 2.4.0 — 2026-10-17 (pendiente de commit)

- **Automix iguala el volumen entre canciones** (fase 3): mide qué tan fuerte suena cada canción y la acerca a un mismo nivel. Baja las fuertes hasta 6 dB y sube las suaves hasta 4 dB. La que entra en una mezcla ya llega corregida; si el análisis llega tarde, se ajusta con una rampa de 2 s en los primeros segundos.
- El análisis de cada canción suma 10 s del medio para medir la sonoridad.

## 2.3.1 — 2026-10-17 (`c674ab4`, `2366cbe`)

- **Carátula más grande**, arriba del reproductor, entre la lupa (izquierda) y Automix (derecha). En la app su tamaño se ajusta solo al espacio libre.
- **Automix arriba a la derecha** y etiqueta de estado oculta, como en la web.
- **Arreglo:** el título y la tarjeta Siguiente salían vacíos con nombres largos.
- La canción que suena también se desplaza en la lista.
- **Cápsula PLAYING / MIXING** más grande y nítida: texto de 15 sp y barras sin brillo, dibujadas en píxeles enteros.
- Menos espacio entre la barra de progreso y los controles.

## 2.3.0 — 2026-10-16 (`13978ea`)

- **Cuadro de la carátula como la web:** un haz láser cian gira por el borde mientras suena; al mezclar se suma un haz magenta en sentido contrario. Abajo, una cápsula `PAUSED / PLAYING / MIXING ⇄` con ecualizador.
- **Doble toque en Siguiente/Anterior** (y en sus tarjetas): adelanta o retrocede 10 s, con un aviso `+10s` / `-10s` centrado en el reproductor.
- **Tarjetas Anterior/Siguiente:** íconos ⏮ ⏭ en los extremos y nombre más grande, en gris claro.
- **Título largo:** se desplaza como en la web (inicio, deslizamiento, final y vuelta), también en las tarjetas.
- **Tiempos** de la canción más grandes y en blanco; nombres de las tarjetas en gris claro (`#cbd5e1`).
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
