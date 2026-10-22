# Biblioteca de música y carátulas — JH MUSIC

Cómo está organizada la música que se copia a los pendrives (`D:\musica` en la laptop) y cómo se hacen las carátulas para que se vean bien en la app. Organizado el 18 y 19 de octubre de 2026.

## 1. Carpetas

`D:\musica` es la fuente: se copia completa al pendrive y también la usa VirtualDJ.

| Carpeta | Qué tiene |
|---|---|
| `01 Cumbia andina y chicha` … `14 Regional Mexicano` | Canciones sueltas por género (01 Cumbia andina y chicha, 02 Cumbia y tecnocumbia, 03 Sanjuanito y zapateo, 04 Rocola y pasillo, 05 Baladas y romántica, 06 Vallenato, 07 Merengue, 08 Salsa, 09 Reggaetón y pop latino, 10 Rock, 11 Inglés, 12 Popular y despecho, 13 Bachata, 14 Regional Mexicano) |
| `Mix` | Mixes, mosaicos y enganchados |
| Carpetas de artista (`Byron Caicedo`, `Angel guaraca`, `Gerardo Moran`, `Americo`, …) | Canciones de ese artista |
| `30 Listas` | Listas armadas por otras personas que se mantienen como están (`uncle Angel`, `JH PENDRIVE`) |
| `SAMPLER JH`, `*.subfolders` | De VirtualDJ (samples y listas); la app los ignora o no tienen música |

**Canciones nuevas:** si el artista tiene carpeta, van ahí; si no, a la carpeta de su género; los mixes, a `Mix`.

## 2. Nombres de archivo

- **Canción suelta:** `Artista - Título.mp3`. Si es una edición propia (por ejemplo, con silencios recortados en mp3cut): `Artista - Título (JH edit).mp3`.
- **Mixes y en vivo:** conservan su nombre, solo sin basura.
- **Siempre se quita la basura:** nombres de páginas y herramientas (`[YT2mp3.info]`, `(mp3cut.net)`, `scloudtomp3downloader`, `SoundLoadMate.com`, `Mp3Converter.net`), códigos de YouTube (`[gcxSCWn0H1A]`), `(Video Oficial)`, `(Letra)`, `HD`, `(320kbps)`, números sueltos al inicio y `1` / `(1)` de copia al final, emojis y espacios dobles.
- La app muestra el **título y artista de las etiquetas** del MP3 si los tiene; el nombre del archivo es lo que se ve en el explorador de Windows, en VirtualDJ y en la app cuando no hay etiquetas.

## 3. Carátulas

La app muestra la carátula guardada **dentro** del MP3 (etiqueta ID3 `APIC`, tipo "portada"). Las de la biblioteca se guardaron así:

| Tipo | Origen |
|---|---|
| **Canciones sueltas** | Carátula **oficial** de Deezer o iTunes, cuadrada, de 1000×1000 o más. Se aceptó solo si el artista y el título coinciden con el archivo, o si Shazam escuchó la canción y confirmó al artista. **No se editan.** |
| **Mixes** | Carátula propia **JH MUSIC** (ver abajo). |

Formato guardado: JPEG de 1000×1000, calidad 90 (unos 100 KB), etiqueta ID3 v2.3.

### Carátula JH MUSIC de los mixes

```
┌──────────────────────────── 1000 ────────────────────────┐
│                                        ┌──────────────┐  │
│        NOMBRE DEL MIX  (turquesa)      │ libre: aquí  │  │  y < 105
│                                        │ va MP3 320K  │  │
│                                        └──────────────┘  │
│                   JH        (contorno cian, brillo)      │
│                 MUSIC       (blanco, brillo neón)        │
│                                                          │
│      Artista · Artista · Artista  (blanco, Inter)        │
│               ┌──────────────────────────┐               │
│               │ libre: aquí va PLAYING   │               │  y > 905
└───────────────┴──────────────────────────┴───────────────┘
                 x 220 … 780
```

- **Fondo** azul muy oscuro con líneas diagonales cian y partículas brillantes (siempre las mismas para el mismo mix).
- **Arriba:** el nombre del mix en Bebas Neue turquesa. **Centro:** "JH" en contorno cian y "MUSIC" en blanco con brillo. **Abajo:** los artistas en Inter blanca; cada artista va entero en una línea (no se corta "Las / Ñañas").
- Los textos largos se achican solos para caber en su espacio.
- **Zonas libres** (solo fondo): la esquina de arriba a la derecha (x > 720, y < 105), donde la app pone `MP3 320K`, y el centro de abajo (x 220–780, y > 905), donde queda la mitad de la cápsula PLAYING. Si cambia dónde pone la app esas dos cosas, hay que rehacer estas carátulas.
- Los artistas de cada mix salieron de lo que Shazam escuchó en 8 partes del mix y del nombre del archivo, revisados a mano.

## 4. VirtualDJ

VirtualDJ guarda los cues, el beatgrid, el BPM y la tonalidad de cada canción **por la ruta del archivo** en `D:\VirtualDJ\database.xml`. Al mover o renombrar canciones hay que actualizar esa base, con VirtualDJ **cerrado** y con copia antes.

> **Importante:** `database.xml` y las listas `.vdjfolder` usan saltos de línea de Windows (**CRLF**). Si se reescriben con LF, aunque el XML sea válido, VirtualDJ al abrir lo da por dañado, lo aparta como `D:\VirtualDJ\Backup\<fecha> broken database.xml` y empieza una base vacía: se pierden todas las marcas. Pasó el 19/10/2026 y se recuperó desde la copia. Editar siempre en binario, cambiando solo el texto necesario, y verificar que no queden LF sueltos.

## 5. Copias y papelera

| Ruta | Qué es |
|---|---|
| `D:\_Respaldo musica 2026-10-18` | Copia completa de `D:\musica` y de `database.xml` antes de reorganizar |
| `D:\_Respaldo caratulas 2026-10-19` | Los 481 MP3 antes de guardarles la carátula |
| `D:\_Papelera JH` | Lo que salió de la biblioteca (duplicados, ediciones descartadas, `100% BALADAS DEL AYER`), sin borrar; y `diario-reorganizacion.tsv`, con cada movimiento (origen → destino) |
| `D:\VirtualDJ\database.xml.antes-*` | Versiones anteriores de la base de VirtualDJ |

El detalle de todo lo que se hizo está en `D:\reporte-musica.md`.

## 6. Después de cambiar la biblioteca

1. Copiar `D:\musica` de nuevo al pendrive (borrando la versión vieja).
2. **Mi Mix** de la app guarda las canciones por su ruta dentro del pendrive: si cambiaron nombres o carpetas, esas canciones salen de Mi Mix y hay que volver a agregarlas.
