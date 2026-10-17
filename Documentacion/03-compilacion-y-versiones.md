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

La forma más simple, desde la raíz del repositorio en Git Bash:

```bash
bash build.sh
```

`build.sh` fija `JAVA_HOME` en el JDK 11, entra a `android/`, ejecuta `./gradlew clean assembleDebug` y muestra el APK generado.

También se puede hacer a mano, desde `android/`:

```bash
# Git Bash
export JAVA_HOME="/c/Program Files/Java/jdk-11.0.30"
./gradlew clean assembleDebug
```

```powershell
# PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-11.0.30"
.\gradlew.bat clean assembleDebug
```

Al terminar `assembleDebug`, la tarea `copyApkToRoot` (en `android/app/build.gradle`):

1. Borra de la raíz del repositorio el APK de la versión anterior (`JH-MUSIC-v*.apk`).
2. Copia el nuevo como **`JH-MUSIC-v<versionName>.apk`** en la raíz.

El APK también queda en `android/app/build/outputs/apk/debug/`.

> Es un build **debug**, firmado con la clave de depuración de la PC. Se instala encima de versiones anteriores firmadas con la misma clave. Si se compila en otra PC, habrá que desinstalar la versión anterior del radio antes de instalar.

## 3. Subir la versión

Hay que cambiar tres lugares:

1. `android/app/build.gradle`:
   ```groovy
   versionCode 28        // entero: +1 en cada versión
   versionName "2.6.1"   // lo que se ve y el nombre del APK
   ```
2. `android/app/src/main/res/layout/activity_main.xml`: la etiqueta `android:text="v2.6.1"` de la barra superior.
3. `index.html` y `android/app/src/main/assets/index.html`: `<span id="appVersionBadge">v2.6.1</span>`.

El `versionCode` debe subir siempre. Si no sube, Android no reconoce la instalación como actualización.

Luego se compila con `bash build.sh` (o `clean assembleDebug`) y se hace commit del código y del APK nuevo. Los APK anteriores quedan en el historial de git.

Cada APK que se prueba en el radio lleva una versión nueva, para distinguirlo en la barra superior de la app.

## 4. Instalar en el radio

1. Copia `JH-MUSIC-v<versión>.apk` a un pendrive.
2. En el radio, ábrelo con el explorador de archivos y acepta instalar de origen desconocido si lo pide.
3. La primera vez, acepta el permiso de almacenamiento: sin él la app no ve los pendrives.

Si después de actualizar sigue apareciendo el ícono anterior, reinicia el radio: algunos launchers guardan el ícono en caché.

## 5. Convenciones del repositorio

- **Ramas:** `master` es la principal; el trabajo de Automix se hace en `hotfix/automix` (también existe `mix`). El repositorio no tiene remoto configurado.
- **Mensajes de commit:** en español, con prefijo `feat:`, `fix:` o `build:`.
- **El APK se versiona en git** junto con el código, para tener siempre a mano el de cada versión. Mientras se prueba en el auto, se hacen commits solo del código; el APK se agrega en el commit final de la versión, junto con el borrado del APK anterior.
- **Simulador web:** si se cambia `index.html`, `app.js` o `style.css` en la raíz, copiarlos también a `android/app/src/main/assets/`.
