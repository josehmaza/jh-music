# Manual de usuario — JH MUSIC

## 1. La pantalla

La pantalla tiene tres zonas:

```
┌──────┬───────────────────────────────┬──────────────────────┐
│ Tira │  EXPLORADOR                   │  REPRODUCTOR         │
│  de  │  [Carpetas][Pistas][Mi Mix]   │ 🔍  [ Carátula ]  ⇄  │
│volu- │  Filtros / Buscador           │  Título y artista    │
│ men  │  Lista                        │ ⏮ Anterior|Siguiente⏭│
│      │  Estado        [▶ Reproducir] │  Barra de progreso   │
│      │                               │ Alea ⏮  ⏯  ⏭  Rep   │
└──────┴───────────────────────────────┴──────────────────────┘
```

- **Barra superior:** marca del auto, versión de la app, saludo, reloj y botón **Re-escanear USB**.
- **Tira de volumen (izquierda):** HOME, MUTE, `+`, porcentaje, `−` y barra vertical.
- **Explorador (centro):** pestañas, buscador y listas. Se puede ocultar para dejar solo el reproductor.
- **Reproductor (derecha):** la canción que suena y sus controles. Arriba, a los lados de la carátula, la **lupa** 🔍 (muestra u oculta el explorador) y el botón **Automix** ⇄ (mezcla entre canciones e iguala su volumen).

### Colores de la pantalla

El **fondo de toda la app** (también el explorador) toma el **color de la carátula** de la canción que suena, y cambia con un fundido suave al pasar de canción; en una mezcla de Automix cambia durante la mezcla. El color se oscurece a propósito para que el texto blanco siempre se lea. Sin carátula, el fondo es azul petróleo. Lo que está activo o se puede tocar sigue en **cian** (pestaña elegida, artista, botones).

## 2. Pestañas

### Carpetas USB

Muestra los pendrives conectados como un árbol: `USB 1 (udisk1)`, `USB 2 (udisk2)` y sus carpetas.

- **Tocar una carpeta** solo la expande o contrae para explorar. No cambia lo que está en Pistas.
- **Botón `[N ▶]`** de una carpeta: abre esa carpeta en la pestaña Pistas. `N` es la cantidad de canciones que están **directamente** en esa carpeta (sin contar subcarpetas), y es exactamente lo que se abre.
- **Botón `+`** de una carpeta (al lado de `[N ▶]`): agrega esas mismas canciones a **Mi Mix**.
- **Botón contraer todo** (junto a las pestañas): cierra todas las carpetas del árbol.
- Solo aparecen los pendrives que están conectados. Si quitas uno, desaparece solo.
- Aquí el buscador busca **carpetas** por nombre.

### Pistas

Muestra las canciones de la **carpeta activa**.

- Al abrir la app, Pistas está vacía (`Pistas (0)`) hasta que elijas una carpeta con `[N ▶]` o reproduzcas una canción desde una búsqueda.
- El número de la pestaña, `Pistas (N)`, es siempre el de la carpeta activa. Una búsqueda no lo cambia.
- Aquí el buscador busca canciones por título, artista, álbum, carpeta o ruta.

### Mi Mix (reemplaza a Favoritos desde 2.5.0)

Una lista que armas cuando quieras y que después suena entera, seguida, con Automix. Funciona como la lista de Automix de Virtual DJ.

**Agregar canciones:**
- **+** en cualquier canción (Pistas o búsquedas). Si ya está en Mi Mix, el botón se ve cian con **✓**; tocarlo la quita.
- **+** al lado de `[N ▶]` en una carpeta (árbol o búsqueda de carpetas): agrega la carpeta entera, en orden A-Z, sin repetir las que ya estaban.
- Un aviso abajo del explorador confirma: "+1 a Mi Mix", "+12 canciones de BACHATAS a Mi Mix", "Ya está en Mi Mix".

**La pestaña `🎧 Mi Mix (N)`:**
- Arriba, una cabecera con `MI MIX` (y `● SONANDO` si es lo que suena) y un resumen: "8 canciones · 30 min · 2 sonaron".
- **▶** (botón cian, solo con el ícono): suena desde la primera y borra las marcas ✓.
- **Vaciar** (papelera): pide confirmación ahí mismo ("¿Vaciar Mi Mix?" → *Sí, vaciar* / *No*).
- Las canciones van **numeradas en el orden en que las agregaste**, que es el orden en que suenan. **✕** quita una.
- **Tocar el número** (o doble toque en la fila): suena Mi Mix desde esa canción.
- Las que **ya sonaron** quedan en la lista, apagadas, con ✓ verde y "Ya sonó".

**Cómo saber que suena Mi Mix** (desde cualquier pestaña):
- La pestaña muestra **"Mi Mix (4) ●"**: un punto cian que **late** mientras suena y queda **quieto** en pausa. Si la pestaña Mi Mix está elegida, el punto se ve oscuro sobre el cian.
- En el reproductor, debajo del título, en lugar de la carpeta dice **"Mi Mix · 2 de 4"**: en qué canción de la lista vas y cuántas son. Así sabes que Siguiente va a la siguiente de Mi Mix y cuánto falta.

**Cómo suena:**
- En orden, mezclando con Automix si está encendido.
- **Al terminar la última, se detiene.** La tarjeta Siguiente avisa `■ Fin de Mi Mix` y al final aparece el aviso "Fin de Mi Mix".
- **Lo que agregas mientras suena entra al final de la cola en vivo**, sin cortar nada. Puedes ir a Carpetas o Pistas, buscar y agregar: lo que suena sigue siendo Mi Mix.
- Si tocas una canción de **Pistas** o de una **búsqueda**, sales de Mi Mix y suena esa carpeta.
- En **aleatorio** elige solo entre las que todavía no sonaron y se detiene cuando ya sonaron todas.
- **Anterior** en la primera canción la vuelve a empezar.

**Quitar o vaciar con música sonando:** la canción que suena **sigue hasta el final**. Aparece arriba de la lista como "Sonando · ya no está en Mi Mix", con un **+** para devolverla a su lugar. Lo que agregues sonará después de ella; si no agregas nada, la música se detiene al terminar.

Mi Mix **se guarda en el radio**: se conserva al cerrar la app, al re-escanear y aunque el pendrive se conecte en otro puerto USB (se reconoce por la ruta dentro del pendrive). Si quitas un pendrive, sus canciones no aparecen, pero vuelven al conectarlo. La primera vez, las estrellas que tenías en Favoritos pasan a Mi Mix.

## 3. Buscador

- Escribe y la búsqueda arranca sola: espera 0,5 s si hay 1–2 letras y 0,25 s si hay más. El botón Buscar del teclado busca de inmediato.
- No importan tildes ni mayúsculas, y tolera letras repetidas (`holaaa` encuentra `hola`).
- Los resultados se ordenan por relevancia: primero los títulos que coinciden exacto o empiezan igual.
- La **X** borra el texto. El chip elegido se mantiene y la lista vuelve a mostrar la carpeta activa.

### Chips de filtro (solo en Pistas)

| Chip | Dónde busca |
|---|---|
| **Carpeta** (`⚲ 1 · Descargas`) | Solo en la carpeta activa. Aparece cuando hay una carpeta activa. |
| **TODO** | En todos los pendrives. |
| **USB 1** / **USB 2** | Solo en ese pendrive. Aparecen únicamente si hay **dos** pendrives conectados; con uno solo, TODO ya es ese pendrive. |

Con el buscador vacío, Pistas muestra siempre la carpeta activa, sea cual sea el chip.

**Encontrar la canción que suena:** si moviste la lista y no sabes si la canción quedó arriba o abajo, **toca el chip de la carpeta**: la lista vuelve a ella y deja una canción antes arriba, luego la que suena y luego las demás. Si hay una búsqueda escrita, se ubica en los resultados (si la canción está entre ellos).

### Búsqueda y cambio de pestaña

- Si buscas en Pistas, vas a otra pestaña sin hacer nada y vuelves, la búsqueda sigue ahí con su texto y su chip.
- Si en Carpetas abres otra carpeta con `[N ▶]`, la búsqueda anterior se descarta y Pistas muestra la carpeta nueva.
- Si al volver a Pistas no hay búsqueda pendiente, se elige el chip de la carpeta activa.
- Al volver a **Pistas** o **Mi Mix**, la lista se ubica en la canción que está sonando (si está en esa lista): una canción antes arriba, luego la que suena y luego las demás.

### Reproducir desde una búsqueda

Al reproducir una canción de los resultados, **su carpeta pasa a ser la carpeta activa**: cambia el número de Pistas y, al limpiar el buscador, verás las canciones de esa carpeta.

## 4. Reproducción

### En la lista de canciones

| Gesto | Resultado |
|---|---|
| Tocar el **número** de la izquierda | Reproduce de inmediato |
| **Doble toque** en la fila | Reproduce de inmediato |
| **Un toque** en la fila | Expande el nombre completo para leerlo (otro toque lo contrae) |
| Tocar **+** (a la derecha) | Agrega la canción a Mi Mix (**✓** = ya está; tocarlo la quita) |

La canción que suena aparece resaltada, solo en la lista donde realmente está. Si su nombre no entra en la fila, se desplaza solo para leerlo completo.

### Cuadro de la carátula

- **Si la canción trae carátula**, se ve la imagen completa. **Si no trae**, se muestra un disco.
- El cuadro ocupa la parte de arriba del reproductor, entre la lupa y Automix. Su tamaño se ajusta solo al espacio libre de la pantalla.
- **Arriba a la derecha**, la etiqueta de formato (`MP3 320K`).
- **Abajo**, una cápsula con el estado y un ecualizador:

  | Estado | Cápsula | Borde del cuadro |
  |---|---|---|
  | En pausa | `PAUSED`, barras grises quietas | Sin animación |
  | Sonando | `PLAYING`, barras cian en movimiento | Un haz láser cian gira por el borde |
  | Mezclando (Automix) | `MIXING ⇄`, barras magenta | Además, un haz magenta gira en sentido contrario |

- Alrededor del cuadro hay un **resplandor del color de la canción** (cian si no tiene carátula).
- **Tocar el cuadro** pausa o reanuda.

### Título y tarjetas Anterior / Siguiente

- Debajo de la carátula, el **título** (grande) y el **artista**.
- Si el título no entra, se desplaza como una marquesina: se queda quieto mostrando el inicio, se desliza hasta el final, espera y vuelve.
- Las tarjetas **Anterior** y **Siguiente** muestran el nombre de esas canciones, con los íconos ⏮ y ⏭ en los extremos. Los nombres largos también se desplazan.

### Controles del reproductor

Están al estilo de YouTube Music: **play/pausa en un círculo blanco grande** y los demás como **íconos blancos sin caja**. Aleatorio y Repetir encendidos se ven **en cian con un puntito debajo**. Con el explorador oculto, los controles se agrandan. La barra de progreso es una línea fina blanca con un punto blanco.

| Control | Función |
|---|---|
| ⏯ (botón central) o **tocar la carátula** | Reproducir / pausar |
| ⏮ / ⏭ o tarjetas **Anterior / Siguiente**, **un toque** | Canción anterior / siguiente |
| ⏮ / ⏭ o tarjetas, **doble toque** | Retrocede / adelanta **10 s**. Cada toque extra seguido suma otros 10 s. Se muestra un aviso `+10s` / `-10s` en el centro |
| Barra de progreso | Arrastrar para adelantar o retroceder |
| Aleatorio (primer botón, a la izquierda) | Alterna orden aleatorio y secuencial |
| Repetir (último botón, a la derecha) | Alterna repetir la canción actual o seguir con la lista |
| **▶ Reproducir Todo** (Pistas) | Reproduce la lista mostrada desde la primera. Mi Mix tiene su propio **▶ Reproducir** |

Un toque en Siguiente o Anterior espera un instante (0,28 s) para saber si es un doble toque, y recién ahí cambia de canción.

### Automix: mezcla entre canciones

El botón con **dos curvas cruzadas**, arriba a la derecha del reproductor, activa o desactiva Automix. Encendido es un **cuadro cian lleno con brillo** y dice **ON**; apagado es oscuro, en gris, y dice **OFF**. Al tocarlo aparece el aviso "Automix activado: mezcla de 5 s y volumen igualado". La app recuerda si lo dejaste encendido.

Con Automix activado:

**1. Mezcla.** El final de cada canción se mezcla con el inicio de la siguiente: una baja mientras la otra sube, sin silencio entre canciones. La app escucha el final de cada canción para mezclar en el lugar justo:
- Si la canción **se va apagando**, la mezcla empieza cuando ya bajó de volumen, sin esperar a que quede en silencio.
- Si **termina de golpe**, la mezcla es corta (3 s) y no cuenta el silencio que pueda quedar al final.
- La canción que entra **salta su silencio inicial**.

**2. Volumen igualado.** Las canciones descargadas suelen venir con volúmenes muy distintos. Automix mide qué tan fuerte suena cada una y la acerca a un mismo nivel, para no tener que tocar el volumen entre canciones:
- Las canciones muy fuertes se bajan un poco; las muy suaves se suben un poco.
- La corrección tiene un límite (bajar hasta 6 dB, subir hasta 4 dB) para no distorsionar los parlantes.
- En la mezcla, la canción que entra ya llega con su volumen corregido.
- Si eliges una canción que todavía no se había analizado, su volumen se ajusta suave en los primeros segundos. Nunca cambia a mitad de la canción.
- Al apagar Automix, la canción vuelve a su volumen original, también de forma suave.
- Como las canciones más fuertes se bajan, en general puede sonar un poco más bajo que sin Automix: sube el volumen del radio una vez.

**3. En pantalla.** Mientras dura la mezcla, en el **cuadro de la carátula gira un segundo haz láser magenta** y la cápsula de abajo dice `MIXING ⇄`. Al empezar la mezcla, los datos de la canción nueva aparecen al instante y la carátula cambia con un fundido cruzado. Desde ese momento la tarjeta "Siguiente" ya indica la que viene después: si tocas Siguiente durante la mezcla, pasas a esa.

El análisis de cada canción tarda unos segundos y se hace una sola vez mientras la app está abierta. Si no alcanza a terminar, o falla con algún archivo, se mezclan simplemente los últimos 5 segundos y esa canción no cambia de volumen.

- Con **Repetir** activado no mezcla: repite la canción.
- Canciones de menos de 15 s no se mezclan, para que la mezcla no se coma la canción.
- En canciones de más de 10 minutos (mixes de DJ) la mezcla dura solo 2 s.
- **Siguiente / Anterior** con Automix cambian con un fundido corto de 0,5 s. Si la siguiente ya estaba precargada, el cambio es instantáneo aunque el pendrive sea lento.
- **Pausa**, **adelantar** (con la barra o con doble toque) o **Siguiente/Anterior** durante una mezcla la terminan de inmediato.
- En aleatorio también funciona: la siguiente se elige con anticipación.

### ¿Qué suena después?

La cola (lo que usan Anterior y Siguiente) se fija **en el momento en que reproduces algo**:

| Reproduces desde… | La cola es… |
|---|---|
| Pistas, carpeta activa | Esa carpeta |
| Pistas, resultado de búsqueda | La carpeta de esa canción |
| Mi Mix | Mi Mix, en su orden; lo que agregues entra al final en vivo, y al terminar la última se detiene |

Abrir otra carpeta mientras suena música **no** cambia lo que viene después, hasta que reproduzcas algo de la carpeta nueva.

## 5. Volumen

- **`+` / `−`:** sube o baja de 5 en 5 %.
- **Barra vertical:** arrastrar para fijar el volumen.
- **MUTE:** silencia o restaura. En silencio se muestra `MUTE`; al quitarlo vuelve el porcentaje real.
- Si ajustas el volumen estando en silencio, primero se quita el silencio.
- Si el silencio lo puso el sistema o quedó activo al salir con HOME, la app lo detecta al volver.

## 6. Mandos del volante o multimedia (opcional)

El Tucson ix35 de este proyecto **no tiene botones en el volante**. Aun así, la app responde a las teclas multimedia estándar de Android, por si se conecta un control Bluetooth o se usa en otro auto:

| Botón | Acción |
|---|---|
| Play/Pausa | Reproducir / pausar |
| Siguiente / Anterior | Cambiar de canción |
| Volumen + / − | ±5 % |
| Mute | Silenciar / restaurar |

## 7. Mostrar u ocultar el explorador

- **Tocar la lupa** (arriba a la izquierda del reproductor): abre o cierra el explorador. Con el explorador oculto, la lupa queda resaltada para que sepas cómo volver a abrirlo.
- **Jalar en la zona de la lupa:** hacia la izquierda lo oculta, hacia la derecha lo muestra.
- Un toque en el resto de esa zona no hace nada, para evitar toques accidentales al conducir.

## 8. Botón Atrás y HOME

**Atrás** (físico) actúa en este orden:
1. Si hay texto en el buscador, lo borra.
2. Si estás en Pistas o Mi Mix, vuelve a Carpetas USB.
3. Si ya estás en Carpetas, sale al launcher del auto.

**HOME** (en la tira de volumen) sale al launcher como el botón físico del radio. La música sigue sonando.

## 9. Pendrives

- La app escanea al abrirse y **vuelve a escanear sola** cuando conectas o quitas un pendrive (espera ~1,5 s a que el sistema termine de montarlo).
- Si tu radio no avisa de esos cambios, usa **Re-escanear USB** en la barra superior.
- Si quitas el pendrive de la canción que suena, la reproducción se detiene y la carpeta activa se limpia.
- Se recorren hasta 6 niveles de subcarpetas. Se ignoran carpetas ocultas, `Android`, `LOST.DIR` y `System Volume Information`.
- Si el archivo se llama `Artista - Título.mp3`, la app separa artista y título por el guion.

## 10. Duración de las canciones

Tras escanear, la lista muestra la duración real de cada canción:

- Si Android ya conoce la canción, la duración aparece de inmediato.
- Si no, la app la lee del archivo en segundo plano, empezando por las canciones de la lista que estás viendo. Mientras tanto se muestra `--:--`.
- Con muchas canciones, completar todas puede tardar un poco. Puedes usar la app con normalidad mientras tanto.

## 11. Limitaciones conocidas

- **Escaneo automático:** depende de que el radio envíe los avisos de conexión del sistema. Si no lo hace, usa **Re-escanear USB**.
- **Misma ruta en dos pendrives:** dos pendrives con un archivo en la misma ruta interna (por ejemplo `Musica/tema.mp3` en ambos) cuentan como la misma canción en Mi Mix.
- **Marcas ✓ de Mi Mix:** se borran al cerrar la app o al tocar el **▶** de Mi Mix.
- **Indicaciones del GPS y llamadas:** la música todavía no baja sola cuando habla el navegador ni se pausa con una llamada (ver propuesta #2).
- **Al prender el auto** la app arranca de cero: no recuerda la última canción ni el punto donde quedó (ver propuesta #1).
- **Volumen igualado** solo funciona con Automix encendido.
