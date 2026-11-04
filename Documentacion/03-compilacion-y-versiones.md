# Compilación y versiones — JH MUSIC

## 1. Requisitos

| Herramienta | Versión |
|---|---|
| JDK | **11** (`C:\Program Files\Java\jdk-11.0.30`). Android Gradle Plugin 7.4 no funciona con Java 8 |
| Gradle | Wrapper incluido (`android/gradlew`), Gradle 7.5 |
| Android Gradle Plugin | 7.4.2 |
| Android SDK | Plataforma 33, con la ruta en `android/local.properties` (`sdk.dir=...`) |

`local.properties` no se sube a git; cada PC debe tener el suyo.

## 2. Generar el APK

La forma más simple, desde la carpeta `jh-music/` en Git Bash:

```bash
bash build.sh
```

`build.sh` fija `JAVA_HOME` en el JDK 11, entra a `android/`, ejecuta `./gradlew clean assembleRelease` y muestra el APK generado.

También se puede hacer a mano, desde `android/`:

```bash
# Git Bash
export JAVA_HOME="/c/Program Files/Java/jdk-11.0.30"
./gradlew clean assembleRelease
```

```powershell
# PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-11.0.30"
.\gradlew.bat clean assembleRelease
```

Al terminar `assembleRelease`, la tarea `copyApkToRoot` (en `android/app/build.gradle`):

1. Borra de `jh-music/` **todos** los APK `JH-MUSIC-v*.apk`, también los que tengan otro sufijo en el nombre. Así siempre queda uno solo, el de la versión actual.
2. Copia el nuevo como **`JH-MUSIC-v<versionName>.apk`** en `jh-music/`.

El APK también queda en `android/app/build/outputs/apk/release/`.

> Es un build **release** (el sistema lo ejecuta optimizado; el debug arrancaba la pantalla hasta 10 veces más lento), pero firmado con la **clave de depuración** de la PC (`signingConfig signingConfigs.debug`). Se instala encima de versiones anteriores firmadas con la misma clave. El aviso de lint `ExpiredTargetSdkVersion` está desactivado porque es una exigencia de Google Play que no aplica a una app instalada a mano. Si se compila en otra PC, habrá que desinstalar la versión anterior del radio antes de instalar.

## 3. Subir la versión

Hay que cambiar tres lugares:

1. `android/app/build.gradle`:
   ```groovy
   versionCode 42         // entero: +1 en cada versión
   versionName "2.10.1"   // lo que se ve y el nombre del APK
   ```
2. `android/app/src/main/res/layout/activity_main.xml`: la etiqueta `android:text="v2.10.1"` de la barra superior (oculta; la versión que se ve sale del APK, al pie del menú ☰).
3. `index.html` y `android/app/src/main/assets/index.html`: `<span id="appVersionBadge">v2.10.1</span>`.

El `versionCode` debe subir siempre. Si no sube, Android no reconoce la instalación como actualización.

Luego se compila con `bash build.sh` (o `clean assembleRelease`), se anota la versión en el [historial](04-historial-de-cambios.md) y se hace commit del código. El APK **no** va en git: se publica en una **Release** de GitHub (ver §5).

Cada APK que se prueba en el radio lleva una versión nueva, para distinguirlo: se ve al pie del menú ☰ (`JH MUSIC v2.10.1`).

## 4. Instalar en el radio

1. Copia `JH-MUSIC-v<versión>.apk` a un pendrive (se descarga de las [Releases](https://github.com/josehmaza/jh-music/releases) o sale de `build.sh`).
2. En el radio, ábrelo con el explorador de archivos y acepta instalar de origen desconocido si lo pide. Se instala **encima** de la versión anterior: no se pierde Mi Mix ni la configuración.
3. La primera vez, acepta el permiso de almacenamiento: sin él la app no ve los pendrives.

Si el radio dice **«App no instalada»**, **no desinstales** la versión anterior para forzarla: se perdería Mi Mix. Suele pasar con un APK firmado en otra PC (§2) o con un `versionCode` que no subió; hay que revisar eso primero.

Si después de actualizar sigue apareciendo el ícono anterior, reinicia el radio: algunos launchers guardan el ícono en caché.

## 5. Convenciones del repositorio

- **Repositorio:** [josehmaza/jh-music](https://github.com/josehmaza/jh-music) en GitHub, rama única `main`.
- **Mensajes de commit:** en español, con prefijo `feat:`, `fix:`, `perf:`, `docs:` o `build:`.
- **El APK no se versiona en git** (`*.apk` está en `.gitignore`). Cada versión se publica como **Release** de GitHub con la etiqueta `v<versión>` (por ejemplo `v2.10.0`) y el APK adjunto, con notas de qué cambió y cómo instalar.
- **Simulador web:** si se cambia `index.html`, `app.js` o `style.css` de `jh-music/`, copiarlos también a `android/app/src/main/assets/`.
- **Saltos de línea:** git convierte a CRLF en Windows (`core.autocrlf=true`); los scripts que reescriban archivos deben respetarlo.
