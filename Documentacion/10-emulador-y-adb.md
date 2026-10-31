# Emulador y ADB: la forma de probar (única, la rápida)

Esta es **la única forma documentada** de levantar el emulador `radio-tucson`, instalar el APK y medir. Está pensada para que una persona o un asistente (Claude, Gemini) la siga paso a paso desde una terminal **sin esperar de más y sin trabarse**. Sirve igual para JH Music y para JH Launcher (solo cambia el APK y el paquete).

Todo se hace desde **Git Bash** (el `bash` del asistente). Los pasos están en orden; cada bloque se pega tal cual.

## Reglas que evitan los dos problemas de siempre

1. **`export MSYS_NO_PATHCONV=1` antes de cualquier `adb` con rutas del teléfono.** Git Bash convierte `/sdcard/...` en `C:/Program Files/Git/sdcard/...` y el comando falla o apunta a una ruta que no existe (`adb push` "funciona" pero la música nunca llega).
2. **El emulador se abre en segundo plano con `( ... &)` y se espera con un bucle que consulta `sys.boot_completed`.** No usar `adb wait-for-device` (devuelve antes de que Android termine de arrancar y luego los comandos fallan) ni scripts con `pause` (se quedan esperando una tecla).
3. **Los enlaces de los pendrives (`udisk1`, `udisk2`) se pierden cada vez que arranca el emulador.** Si la app dice "0 canciones", casi siempre es porque faltan; se crean en el paso 3.

4. **`MSYS_NO_PATHCONV=1` rompe `gradlew`** ("Could not find or load main class org.gradle.wrapper.GradleWrapperMain"). Los dos `build.sh` ya hacen `unset MSYS_NO_PATHCONV` solos, así que `bash build.sh` funciona con la variable puesta. Si se llama a `./gradlew` a mano, hacerlo en otra terminal o con `unset MSYS_NO_PATHCONV` antes.

## Datos fijos de este equipo

| Qué | Valor |
|---|---|
| SDK de Android | `D:\JH-PROGRAMAS\android-sdk` |
| AVD | `radio-tucson` (Android 8.1 / API 27, 1024 × 600 a 160 dpi) |
| Música de prueba | Ya está en el emulador: `/sdcard/MUSICA` (= USB 2) y `/sdcard/USB` (= USB 1). **No hace falta copiar nada.** |
| Paquetes | JH Music `com.jh.caraudio` · JH Launcher `com.jh.launcher` |

## Paso 1. Preparar la terminal

```bash
export MSYS_NO_PATHCONV=1
export PATH="/d/JH-PROGRAMAS/android-sdk/emulator:/d/JH-PROGRAMAS/android-sdk/platform-tools:$PATH"
adb devices -l
```

Si ya aparece `emulator-5554  device`, el emulador está abierto: **saltar al paso 3**.

## Paso 2. Abrir el emulador y esperar a que arranque

```bash
(emulator -avd radio-tucson -gpu swiftshader -no-skin -no-snapshot -no-boot-anim >/dev/null 2>&1 &)
for i in $(seq 1 50); do
  b=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
  [ "$b" = "1" ] && break
  sleep 4
done
adb devices -l
```

Termina cuando `boot_completed` vale `1` (suele tardar menos de un minuto). Si el asistente tiene un límite de tiempo por comando, ejecutar el bloque en segundo plano y esperar el aviso.

## Paso 3. Enlazar los pendrives (en cada arranque del emulador)

```bash
adb root
adb wait-for-device
adb shell 'ln -s /storage/emulated/0/USB /mnt/udisk1; ln -s /storage/emulated/0/USB /storage/udisk1; ln -s /storage/emulated/0/MUSICA /mnt/udisk2; ln -s /storage/emulated/0/MUSICA /storage/udisk2'
adb shell ls -la /mnt/udisk1 /mnt/udisk2
```

Si los enlaces ya existen, `ln` avisa "File exists": no es un error.

## Paso 4. Compilar, instalar y abrir

```bash
bash build.sh                                   # genera JH-MUSIC-v<versión>.apk (release) en la raíz del repo
adb install -r JH-MUSIC-v2.9.8.apk              # cambiar el nombre al de la versión actual
adb shell am start -W -n com.jh.caraudio/.MainActivity
```

`-W` imprime `TotalTime` (arranque en frío en ms). Para el launcher: `com.jh.launcher/.MainActivity`.

Si la app arranca con 0 canciones: repetir el paso 3 y reiniciarla con `adb shell am force-stop com.jh.caraudio` y de nuevo `am start`.

## Paso 5. Ver la pantalla y manejarla

```bash
adb exec-out screencap -p > captura.png         # captura (abrirla con la herramienta de imágenes)
adb shell input tap 790 530                     # tocar (x y en píxeles de 1024 × 600): botón Play
adb shell input keyevent 85                     # Play / Pausa      (87 = siguiente, 88 = anterior)
adb shell input swipe 800 300 200 300 300       # deslizar
```

## Paso 6. Medir el rendimiento (con música sonando)

Primero poner a sonar una canción (tocar Play) y luego:

```bash
P=com.jh.caraudio
adb shell dumpsys gfxinfo $P reset
sleep 15
adb shell dumpsys gfxinfo $P | grep -E "Total frames|Janky|50th|90th|95th"
adb shell top -n 1 -b -p $(adb shell pidof $P | tr -d '\r') | tail -2
adb shell dumpsys meminfo $P | grep -E "TOTAL "
```

Cómo leerlo:

| Dato | Qué significa |
|---|---|
| `Total frames rendered` ÷ segundos | Cuadros dibujados por segundo. Con la pantalla quieta debería ser casi 0; sonando, lo menos posible |
| `90th percentile` | Si pasa de 16 ms, la interfaz se ve a tirones |
| `Janky frames` | Cuadros que no llegaron a tiempo |
| `%CPU` de `top` | Carga del proceso |

**Recuerda:** el emulador usa la CPU de la PC, mucho más rápida que la del radio. Los números absolutos en el auto serán peores; lo que vale es **comparar antes y después** de un cambio. Confirmar siempre en el radio real.

## Mediciones de referencia (emulador, 2 corridas por versión, mismo método del paso 6)

| Dato | Original (debug) | Actual (release + correcciones) | Cambio |
|---|---|---|---|
| Cuadros/s en **pausa** | 54 | **21** | **-62 %** |
| Cuadros/s **sonando** | 53 | **23** | **-57 %** |
| Memoria (PSS) | 24,3 MB | 22,0 MB | -9 % |
| Arranque en frío | ~1,18 s | ~1,0-1,2 s | sin cambio claro |
| CPU del proceso | 16-28 % | 0-8 % | muy ruidoso, pero a la baja |

Qué se corrigió para llegar ahí (de mayor a menor efecto):

1. **El `ListView` del árbol de carpetas** usaba la marquesina *nativa* de Android (`ellipsize="marquee"` + `setSelected(true)`), que se anima en **cada VSYNC** (~57 repintados por segundo, incluso en pausa) mientras algún nombre de carpeta no cabe. Ahora usa `CarMarqueeTextView`.
2. **Rejilla común `FrameGrid`:** marquesinas, pastilla, ondas y ecualizador repintan alineados a la misma rejilla de 50 ms. Antes cada uno tenía su propio temporizador y nunca coincidían (4 animaciones de 20 FPS sumaban ~60 repintados por segundo).
3. Las marquesinas no repintan durante sus pausas (inicio y final del ciclo).
4. APK **release** en vez de debug.

**Cómo se encontró (útil si vuelve a pasar):** ocultar vistas por ID y medir cuadros descarta zonas; un `OnPreDrawListener` temporal que recorre las vistas **visibles** y registra cuáles tienen la marca `PFLAG_INVALIDATED` (campo `mPrivateFlags`, vía reflexión, válido en Android 8.1) dice exactamente quién pide cada repintado. Ignorar las vistas `GONE`: conservan la marca y dan falsos positivos.

## Probar en el radio real

Conectar el radio por ADB (cable o red) y repetir los pasos 4 a 6: el paso 1 vale igual, los pasos 2 y 3 **no** se hacen (el radio ya tiene sus pendrives en `/storage/udisk1` y `/storage/udisk2`).
