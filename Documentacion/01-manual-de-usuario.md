# Manual de usuario — JH MUSIC

## 1. La pantalla

La pantalla tiene tres zonas:

```
┌──────┬───────────────────────────────┬──────────────────────┐
│ Tira │  EXPLORADOR                   │  REPRODUCTOR         │
│  de  │  [Carpetas USB][Pistas][Fav.] │  Carátula            │
│volu- │  Filtros / Buscador           │  Título y artista    │
│ men  │  Lista                        │  Anterior | Siguiente│
│      │  Estado        [▶ Reproducir] │  Barra de progreso   │
│      │                               │ Alea ⏮  ⏯  ⏭  Rep   │
└──────┴───────────────────────────────┴──────────────────────┘
```

- **Barra superior:** marca del auto, versión de la app, saludo, reloj y botón **Re-escanear USB**.
- **Tira de volumen (izquierda):** HOME, MUTE, `+`, porcentaje, `−` y barra vertical.
- **Explorador (centro):** pestañas, buscador y listas. Se puede ocultar para dejar solo el reproductor.
- **Reproductor (derecha):** la canción que suena y sus controles.

## 2. Pestañas

### Carpetas USB

Muestra los pendrives conectados como un árbol: `USB 1 (udisk1)`, `USB 2 (udisk2)` y sus carpetas.

- **Tocar una carpeta** solo la expande o contrae para explorar. No cambia lo que está en Pistas.
- **Botón `[N ▶]`** de una carpeta: abre esa carpeta en la pestaña Pistas. `N` es la cantidad de canciones que están **directamente** en esa carpeta (sin contar subcarpetas), y es exactamente lo que se abre.
- **Botón contraer todo** (junto a las pestañas): cierra todas las carpetas del árbol.
- Solo aparecen los pendrives que están conectados. Si quitas uno, desaparece solo.
- Aquí el buscador busca **carpetas** por nombre.

### Pistas

Muestra las canciones de la **carpeta activa**.

- Al abrir la app, Pistas está vacía (`Pistas (0)`) hasta que elijas una carpeta con `[N ▶]` o reproduzcas una canción desde una búsqueda.
- El número de la pestaña, `Pistas (N)`, es siempre el de la carpeta activa. Una búsqueda no lo cambia.
- Aquí el buscador busca canciones por título, artista, álbum, carpeta o ruta.

### Favoritos

Lista las canciones marcadas con la estrella ★. Al reproducir desde aquí, Anterior y Siguiente recorren los favoritos (no la carpeta de la canción).

Los favoritos **se guardan en el radio**: se conservan al cerrar la app, al re-escanear y aunque el pendrive se conecte en otro puerto USB (se reconocen por su ruta dentro del pendrive). Si quitas el pendrive, sus favoritos no aparecen, pero vuelven al conectarlo de nuevo.

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

### Búsqueda y cambio de pestaña

- Si buscas en Pistas, vas a otra pestaña sin hacer nada y vuelves, la búsqueda sigue ahí con su texto y su chip.
- Si en Carpetas abres otra carpeta con `[N ▶]`, la búsqueda anterior se descarta y Pistas muestra la carpeta nueva.
- Si al volver a Pistas no hay búsqueda pendiente, se elige el chip de la carpeta activa.

### Reproducir desde una búsqueda

Al reproducir una canción de los resultados, **su carpeta pasa a ser la carpeta activa**: cambia el número de Pistas y, al limpiar el buscador, verás las canciones de esa carpeta.

## 4. Reproducción

### En la lista de canciones

| Gesto | Resultado |
|---|---|
| Tocar el **número** de la izquierda | Reproduce de inmediato |
| **Doble toque** en la fila | Reproduce de inmediato |
| **Un toque** en la fila | Expande el nombre completo para leerlo (otro toque lo contrae) |
| Tocar la **estrella** | Marca o desmarca como favorito |

La canción que suena aparece resaltada, solo en la lista donde realmente está.

### Cuadro de la carátula

- **Si la canción trae carátula:** se ve la imagen completa, con las barras animadas abajo sobre una sombra suave y la etiqueta de formato (`MP3 320K`) arriba.
- **Si no trae:** se muestra el disco con las barras animadas debajo.
- **Tocar el cuadro** pausa o reanuda.

### Controles del reproductor

| Control | Función |
|---|---|
| ⏯ (botón central) o **tocar la carátula** | Reproducir / pausar |
| ⏮ / ⏭ o tarjetas **Anterior / Siguiente** | Canción anterior / siguiente |
| Barra de progreso | Arrastrar para adelantar o retroceder |
| Aleatorio (primer botón, a la izquierda) | Alterna orden aleatorio y secuencial |
| Repetir (último botón, a la derecha) | Alterna repetir la canción actual o seguir con la lista |
| **▶ Reproducir Todo** (Pistas y Favoritos) | Reproduce la lista mostrada desde la primera |

### Automix: mezcla entre canciones

El botón con **dos curvas cruzadas**, debajo de la lupa, activa o desactiva Automix. Encendido se ve en cian, y la etiqueta del reproductor dice `● REPRODUCIENDO · AUTOMIX`. La app recuerda si lo dejaste encendido.

Con Automix activado, el final de cada canción se mezcla con el inicio de la siguiente: una baja mientras la otra sube, sin silencio entre canciones.

La app escucha el final de cada canción para mezclar en el lugar justo:
- Si la canción **se va apagando**, la mezcla empieza cuando ya bajó de volumen, sin esperar a que quede en silencio.
- Si **termina de golpe**, la mezcla es corta (3 s) y no cuenta el silencio que pueda quedar al final.
- La canción que entra **salta su silencio inicial**.

Mientras dura la mezcla, el **cuadro de la carátula muestra un borde azul neón que late**. Al empezar la mezcla, el título, el artista y los tiempos de la canción que sale se apagan suavemente y aparecen los de la nueva; el cuadro de la carátula no se apaga. Desde ese momento la pantalla muestra la canción nueva, y la tarjeta "Siguiente" ya indica la que viene después: si tocas Siguiente durante la mezcla, pasas a esa.

Ese análisis tarda unos segundos al empezar cada canción. Si no alcanza a terminar, o falla con algún archivo, se mezclan simplemente los últimos 5 segundos.

- Con **Repetir** activado no mezcla: repite la canción.
- Canciones de menos de 15 s no se mezclan, para que la mezcla no se coma la canción.
- En canciones de más de 10 minutos (mixes de DJ) la mezcla dura solo 2 s.
- **Pausa**, **adelantar** con la barra o **Siguiente/Anterior** durante una mezcla la terminan de inmediato.
- En aleatorio también funciona: la siguiente se elige con anticipación.

### ¿Qué suena después?

La cola (lo que usan Anterior y Siguiente) se fija **en el momento en que reproduces algo**:

| Reproduces desde… | La cola es… |
|---|---|
| Pistas, carpeta activa | Esa carpeta |
| Pistas, resultado de búsqueda | La carpeta de esa canción |
| Favoritos | La lista de favoritos mostrada |

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

- **Tocar la lupa** (arriba del reproductor): abre o cierra el explorador.
- **Jalar en la zona de la lupa:** hacia la izquierda lo oculta, hacia la derecha lo muestra.
- Un toque en el resto de esa zona no hace nada, para evitar toques accidentales al conducir.

## 8. Botón Atrás y HOME

**Atrás** (físico) actúa en este orden:
1. Si hay texto en el buscador, lo borra.
2. Si estás en Pistas o Favoritos, vuelve a Carpetas USB.
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
- **Favoritos con la misma ruta:** dos pendrives con un archivo en la misma ruta interna (por ejemplo `Musica/tema.mp3` en ambos) comparten la marca de favorito.
