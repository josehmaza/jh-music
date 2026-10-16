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

> Los favoritos se mantienen mientras la app está abierta (incluso al re-escanear), pero **se pierden al cerrar la app**.

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

### Controles del reproductor

| Control | Función |
|---|---|
| ⏯ (botón central) o **tocar la carátula** | Reproducir / pausar |
| ⏮ / ⏭ o tarjetas **Anterior / Siguiente** | Canción anterior / siguiente |
| Barra de progreso | Arrastrar para adelantar o retroceder |
| Aleatorio (primer botón, a la izquierda) | Alterna orden aleatorio y secuencial |
| Repetir (último botón, a la derecha) | Alterna repetir la canción actual o seguir con la lista |
| **▶ Reproducir Todo** (Pistas y Favoritos) | Reproduce la lista mostrada desde la primera |

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

## 6. Mandos del volante

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

## 10. Limitaciones conocidas

- **Favoritos** no se guardan al cerrar la app.
- **Duración en la lista:** las canciones encontradas recorriendo carpetas muestran `03:30` como duración aproximada. La duración real aparece en el reproductor al sonar.
- **Escaneo automático:** depende de que el radio envíe los avisos de conexión del sistema.
