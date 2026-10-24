# Historial de cambios — JH MUSIC

Versiones de la más nueva a la más antigua. Entre paréntesis, el commit de git.

## 2.9.6 — 2026-10-24, pendiente de probar en el auto

Diseño revisado a la pantalla real del radio (1024 × 600 a 160 dpi, medida con Monitor JH). Igual en la maqueta web y en JH VIDEO 1.1.1.

- **Pestañas:** el número va en una etiqueta chica al lado, sin paréntesis (`Canciones 109`, `Mi Mix 4`). Antes "Canciones (109)" no entraba y se partía en dos renglones.
- **Filas de Librería:** menos sangría por nivel, ícono de carpeta chico y fijo (la raíz USB conserva su cuadro verde), nombre en 21 en 1 renglón que se desliza si es largo, `+` de 46 y ▶ cuadrado sin número. Sin la línea de cantidad: filas de 60 (antes 72); entra una carpeta más a la vista.
- **La carpeta tocada** muestra su nombre completo en 2 renglones hasta que se toque otra. Ya no queda pintada: solo destella al tocarla.
- **La carpeta que suena:** su ▶ muestra un ecualizador que se mueve mientras suena y queda quieto en pausa (`MiniEqualizerView`). La abierta en Canciones, si no es la que suena, lleva solo un borde cian fino.
- **Al volver a Librería** la lista se mueve hasta la carpeta que suena.
- **Arreglo:** anterior / play / siguiente se movían un poco según la canción. Título y artista tienen ahora alto fijo.
- **Arreglo:** al tocar las tarjetas de anterior / siguiente (y filas de listas) salía un recuadro naranja de Android; ahora es cian translúcido (`bg_pressed_cyan`).
- **Maqueta:** el árbol marcaba como "la que suena" la carpeta recién abierta; ahora usa la canción cargada (`loadedTrack`).
- Recursos nuevos: `bg_tree_node_open`, `bg_pressed_cyan`, `MiniEqualizerView`. Se quitó `bg_tree_node_playing`.

## 2.9.5 — 2026-10-23, pendiente de probar en el auto

- **Sin barra arriba (opción C2):** la barra de arriba (marca, versión, saludo, reloj y Re-escanear USB) se oculta y su alto pasa al resto de la pantalla.
  - **Reloj grande en la tira de volumen:** horas arriba y minutos abajo (30 sp), y debajo un punto **verde si hay pendrive, gris si no**, con "USB". Solo se lee, no se toca.
  - **La carátula crece de 260 a 298 dp** (su tope pasa de 260 a 300); título, tarjetas, barra y botones quedan donde estaban. En la maqueta, de 221 a 263 px.
  - **Menú ☰ → Re-escanear USB** (cierra el menú) y la **versión** al pie del menú (`JH MUSIC v2.9.5`).
  - La barra queda en el layout oculta, porque el código la sigue usando (reloj, versión y botón).
- La 2.9.3, la 2.9.4 y la 2.9.5 las subió otra herramienta al ajustar el gesto de volumen, sin anotarlas aquí.
- El gesto de volumen no funciona en ningún lugar del explorador, tampoco en el espacio vacío debajo de la lista: ahí deslizar arriba / abajo es el scroll. Igual en las dos maquetas.
- **Carátula al tocarla:** se achica apenas (97 %) y el borde se ilumina en cian, como en la maqueta. Antes Android la tapaba entera con un naranja (`selectableItemBackground`). Recurso nuevo: `fg_album_card_pressed`.
- **Arreglo del gesto de volumen:** si empezaba sobre la carátula, la carátula quedaba presionada (se ponía naranja). Ahora, cuando el gesto toma el toque, lo que estaba debajo del dedo recibe un cancelar. Igual en JH VIDEO.
- **El explorador se oculta o muestra con doble toque en la carátula**, como la pantalla completa en JH VIDEO (y desde el menú ☰). Un toque en la carátula ya no pausa (se confundía con el doble toque): para pausar están ▶ / ⏸ y su franja. El doble toque cuenta si los dos toques llegan en menos de 0,35 s. Ya no se jala de costado: la franja junto a la carátula queda libre para el gesto de volumen. Igual en la maqueta.
- Recursos nuevos: `bg_usb_dot`, `ic_rescan`.

## 2.9.2 — 2026-10-23, pendiente de probar en el auto

- **Volumen con un gesto, sin mirar:** arrastrar el dedo hacia arriba o hacia abajo sobre el reproductor sube o baja el volumen (240 dp de recorrido = 100 %). Al moverlo aparece un indicador grande al centro (ícono, barra y porcentaje) que se va solo. El gesto arranca después de 14 dp de movimiento vertical; si el movimiento es horizontal (jalar el explorador) no toca el volumen. No empieza sobre las barras de progreso y de volumen, ni en el buscador. Igual en la maqueta web y en JH VIDEO.
- **Arreglo del gesto:** no empieza sobre el explorador. Ahí un arrastre vertical es para recorrer la lista (o reordenar Mi Mix): antes, al buscar una canción, la lista se trababa y el volumen cambiaba.
- Recursos nuevos: `layout_blind_volume_hud.xml`, `bg_blind_volume_hud`, `bg_hud_track` y `bg_hud_fill`.

## 2.9.1 — 2026-10-23, pendiente de probar en el auto

- **Maqueta web igual que la app:** anterior / play / siguiente responden en toda la franja de abajo del reproductor (hasta el borde, con 24 px de margen alrededor del play), y aleatorio y repetir se recuerdan en el navegador.
- **Tira de volumen más ancha para no fallar al tocar:** 72 dp (antes 54), con HOME, MUTE, `+` y `−` de 60 × 52 dp (antes 44 × 40), el `+` / `−` en 26 sp y el porcentaje en 16 sp. La barra vertical responde en 60 dp de ancho y su pista mide 16 dp (pomo de 13 dp de radio). Igual en la maqueta web y en JH VIDEO.

## 2.9.0 — 2026-10-23 (`087ee51`), pendiente de probar en el auto

- **Abre en la última canción:** al volver a encender el auto, la app deja cargada en pausa (desde el inicio) la canción que sonaba, con su lista (su carpeta, que queda como la activa, o Mi Mix). Si el pendrive se monta después de abrir la app, se carga al aparecer. Si la canción ya no está, abre como siempre.
- **Se recuerdan Aleatorio y Repetir canción** (Automix ya se recordaba).
- **Anterior y Siguiente responden en toda la franja de abajo del reproductor**, desde debajo de los tiempos hasta el borde de la pantalla (a cada lado del play), también por debajo y a los costados de los botones. Al tocar se ilumina solo el botón (un círculo cian), no toda la zona.
- **Play / pausa más grande** (88 dp; 100 dp con el explorador oculto; antes 76 / 88) y con 24 dp de tolerancia a cada lado: un toque que falla por poco ya no cambia de canción. Se borra `bg_ctrl_zone` y entra `bg_ctrl_icon`.
- **Doble toque en una carpeta** de Librería = lo mismo que su botón `[N ▶]` (la abre en Canciones). Un toque sigue abriendo o cerrando sus subcarpetas; el segundo la deja como estaba. Cada toque enciende la fila en cian 0,25 s (`bg_tree_node_flash`), para saber que el toque entró.
- **Carpetas de Librería más grandes para leer y tocar manejando:** nombre de 21 sp (antes 16), filas de 72 dp (antes 58) con 8 dp entre una y otra (antes 4), ícono de 44 dp, `+` de 52 × 48 dp y `[N ▶]` de 46 dp de alto con el número en 19 sp. Igual en la maqueta web.
- **Volumen de 10 en 10:** los botones `+` / `−` (y las flechas en la maqueta web) suben o bajan 10 % en vez de 5 %.
- **Pestaña Canciones con una nota musical** en lugar de las tres rayas, que parecían un menú o una lista. `ic_tab_list` pasa a ser `ic_tab_music`.
- Solo en la app: la maqueta web no guarda nada entre aperturas.

## 2.8.0 — 2026-10-23 (`c94a28d`), pendiente de probar en el auto

Incluye los cambios que estaban sin publicar para la 2.7.1 (más abajo).

- **Anterior y Siguiente fáciles de tocar manejando:** se siguen viendo como íconos blancos sin caja, más grandes (50 dp; 56 dp con el explorador oculto; antes 36 / 40), pegados hacia el play, pero cada uno responde en todo el ancho que deja el play, hasta el borde del reproductor (también con el explorador oculto, donde antes la fila no pasaba de 540 dp), y en 76 dp de alto (88 dp con el explorador oculto); antes, solo en un cuadro de 58 dp. Al tocar, esa zona entera se ilumina en cian un instante (también en un toque rápido). Doble toque = ±10 s, igual que antes.
- **Menú ☰** arriba a la derecha, con **Automix debajo**: Aleatorio y Repetir canción (con ON / OFF escrito) y Ocultar / Mostrar explorador. Se cierra solo a los 5 s, tocando fuera o en ✕. Aleatorio y Repetir salen de la botonera, así no se tocan por error al buscar Siguiente. Cuando están encendidos, sus íconos se ven en cian arriba a la izquierda del reproductor (repetir arriba, aleatorio debajo); no se tocan, así no estorban al gesto de jalar el explorador.
- **Sin la lupa:** el explorador se oculta o muestra jalando en la franja libre a la izquierda de la carátula (como antes) o desde el menú ☰.
- **Nombres nuevos:** la pestaña **Pistas** pasa a **Canciones** y **Carpetas USB** a **Librería** (también en la ruta de arriba de la lista y en "Buscando canciones...").
- Recursos nuevos: `bg_ctrl_zone`, `ic_menu`, `ic_explorer_panel`, `bg_player_menu*` y `layout/popup_player_menu.xml`. Se borran `bg_ctrl_flat` e `ic_music_search`.
- En la app el menú es un `PopupWindow` sin foco y sin recorte: con foco Android mostraba la barra de navegación y el menú quedaba corrido hacia arriba.

### Cambios que eran para la 2.7.1

- **Buscador con ñ y tildes:** si escribes `ñaña`, primero salen las canciones que lo tienen escrito igual ("Las Ñañas") y después las que solo se parecen ("didi nana"). Antes salía primero "didi nana", porque al quitar tildes la ñ quedaba como n. Sin ñ ni tildes en la búsqueda, el orden es el de siempre. En la app y en la maqueta web.
- **Nada tapa la carátula** (las carátulas oficiales no se editan; cambia solo dónde va lo que la app pone encima):
  - La **cápsula PLAYING / MIXING** queda **montada sobre el borde de abajo** (mitad dentro, mitad fuera). Mismo tamaño (164 × 38 dp) y misma animación.
  - La etiqueta **MP3 320K** queda **pegada a la esquina** de arriba a la derecha, con la misma curva de la carátula y fondo sólido (antes estaba a 8 dp del borde y dejaba ver la carátula por su esquina).
  - El **título y todo lo de abajo no se mueven**: la carátula se achica 19 dp para dejar lugar a la mitad de la cápsula que sobresale.
- **Arreglo:** al volver a la pestaña **Mi Mix**, la lista otra vez se ubica en la canción que suena (desde la 2.7.0 solo lo hacía en Pistas, porque Mi Mix pasó a otra lista).
- `bg_audio_badge.xml` reemplazado por `bg_audio_badge_corner.xml`.

## 2.7.0 — 2026-10-18 (`e1d8b0b`)

- **Reordenar canciones en Mi Mix (Apertura de Hueco en Vivo + Drag & Drop táctil ≡):**
  - **Apertura de hueco en vivo (Live Gap):** al arrastrar una pista, las demás canciones se deslizan en tiempo real hacia arriba o abajo abriendo el espacio exacto donde caerá.
  - **Motor nativo de alto rendimiento (Android):** implementado con `RecyclerView` e `ItemTouchHelper` oficial de Google, acelerado en el **RenderThread de la GPU a 60 fps**, garantizando cero sobrecarga en la CPU de la radio.
  - **Motor web sincronizado:** implementado con arquitectura FLIP y tarjeta flotante sólida 100% opaca con elevación 3D (cero textos encimados ni desajustes visuales).
  - **Auto-Scroll continuo en bordes:** al arrastrar una pista cerca del borde superior o inferior (70px), la lista se desplaza sola a velocidad adaptativa para reorganizar listas largas cómodamente.
  - **Botón `[ ⇅ ]` en cabecera:** conmuta el modo reordenar.
  - **Cero pérdida de espacio:** la pantalla conserva el 100% del ancho para los títulos largos; la `✕` (quitar) se transforma en la manija `≡` en el mismo espacio.
  - **Aterrizaje magnético:** al soltar, la pista se imanta en el hueco y confirma la posición con un destello neón cian.
  - Botón **`[ ✓ Listo ]`** para finalizar y guardar en memoria persistente.

## 2.6.1 — 2026-10-17 (`5254a2f`)

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

## 2.5.0 — 2026-10-17 (`8c56920`)

- **Mi Mix reemplaza a Favoritos:** una lista que armas con **+** (en cada canción o en una carpeta entera) y que suena seguida, en su orden, con Automix, como la lista de Automix de Virtual DJ.
  - Lo que agregas mientras suena entra al final de la cola en vivo.
  - Al terminar la última se detiene (`■ Fin de Mi Mix`).
  - Las que ya sonaron quedan marcadas con ✓.
  - Cabecera con resumen, **▶ Reproducir** y **Vaciar** con confirmación. Al vaciar o quitar la que suena, esa canción sigue hasta el final.
  - Las estrellas de Favoritos pasan a Mi Mix la primera vez.

## 2.4.0 — 2026-10-17 (`8c56920`)

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
