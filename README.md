# Cuéntame un cuento

App Android de cuentos infantiles en español, catalán, inglés, francés, alemán, gallego y euskera. Cada cuento tiene una portada
animada y se puede **leer** pasando páginas o **escuchar** con la voz del sistema (texto a voz),
con la palabra que suena resaltada, traducción opcional e ilustraciones a pantalla completa.
Funciona en vertical y en horizontal, recuerda el cuento a medias y permite marcar favoritos.

## Stack

| | |
|---|---|
| Lenguaje | Kotlin 2.3 (Kotlin integrado de AGP 9) |
| Build | AGP 9.3.3 · Gradle 9.7.1 · JDK 17 · catálogo de versiones en `gradle/libs.versions.toml` |
| SDK | minSdk 26 · targetSdk 37 · compileSdk 37 |
| UI | Jetpack Compose + Material 3, Navigation Compose (una sola Activity), ViewModel + StateFlow |
| Letras | Fredoka (títulos y botones); Andika (por defecto), Atkinson Hyperlegible o Anna para el texto de los cuentos. Licencias OFL en `docs/licencias/` |
| DI | Hilt |
| Datos | Catálogo JSON incluido en la app y actualizable desde GitHub Pages; DataStore (preferencias locales) |
| Red / imágenes | OkHttp + Coil 3 (caché en disco) · kotlinx.serialization |
| Calidad | Crashlytics, lint, tests unitarios |

## Estructura

```
com.carlossega.cuentameuncuento
├── CuentoApplication
├── audio/                  ← música de fondo (foco de audio) y narrador (TextToSpeech)
├── data/
│   ├── catalogo/           ← catálogo de cuentos: modelo, fuentes (app / guardado / web) y repositorio
│   ├── model/              ← Cuento, LineaCuento, Idioma, Preferencias, Lectura (cuento a medias)
│   ├── precarga/           ← descarga anticipada de imágenes nuevas
│   ├── prefs/              ← DataStore (preferencias, solo en el dispositivo)
│   └── repository/         ← interfaces
├── di/                     ← módulos Hilt
└── ui/
    ├── MainActivity        ← única Activity (el alias .SplashActivity del manifest es el icono del launcher)
    ├── Navegacion          ← rutas: Biblioteca → Ficha → Lector (Ajustes es un panel de la Biblioteca)
    ├── tema/               ← colores, letras (Fredoka, Andika) y tamaño del texto
    ├── comun/              ← paisaje vectorial, botones redondos, corazón, menú de idiomas
    └── biblioteca/  ficha/  lector/  ajustes/   ← cada pantalla con su ViewModel
```

La interfaz no usa imágenes propias: el paisaje se dibuja en código y los iconos son los de
Material. Las únicas imágenes son las de los cuentos (`contenido/`).

## Contenido de los cuentos

Los cuentos están en el propio repositorio, en `contenido/cuentos/<id>/`:

- `cuento.json`: título, descripción y líneas en los siete idiomas a la vez (`esp`, `cat`, `eng`, `fra`, `deu`, `glg`, `eus`)
  (así la traducción siempre va alineada).
- `portada.jpg|png|webp` y `img_<N>.png|jpg|webp`: las ilustraciones originales.

```json
{
  "id": "los_tres_cerditos",
  "edad": 3,
  "titulo": { "esp": "Los tres cerditos", "cat": "Els tres porquets", "eng": "The three little pigs" },
  "descripcion": { "esp": "…", "cat": "…", "eng": "…" },
  "lineas": [
    { "imagen": "1", "esp": "Había una vez…", "cat": "Hi havia una vegada…", "eng": "There were once…" },
    { "imagen": null, "esp": "…", "cat": "…", "eng": "…" },
    { "imagen": "10", "esp": "FIN", "cat": "FI", "eng": "THE END" }
  ]
}
```

`imagen` es la ilustración que empieza en esa línea (`null` = sigue la anterior). La primera
línea debe tener ilustración y la última es `FIN` / `FI` / `THE END` / `ENDE` / `AMAIERA`.

`creditos` (opcional, en todos los idiomas) aparece en la ficha del cuento. Es obligatorio en los
cuentos de [StoryWeaver](https://storyweaver.org.in) (licencia CC BY 4.0): autoría, ilustración,
traducción, editorial, licencia y qué se ha cambiado.

### Añadir o cambiar un cuento

```bash
pip install -r herramientas/requirements.txt
python herramientas/contenido.py
```

El script valida todos los cuentos, convierte las imágenes a WebP (portadas en miniatura,
ilustraciones a 1600 px como máximo) y genera `catalogo.json` en:

- `app/src/main/assets/contenido/`: lo que va dentro de la app (funciona sin conexión).
- `build/contenido-web/contenido/`: lo que se publica en la rama `gh-pages` para que las
  apps ya instaladas reciban los cambios sin publicar una versión nueva.

La app usa el catálogo más reciente entre el incluido y el último descargado, y una vez por
sesión comprueba si hay uno nuevo en
`https://carlos26sg.github.io/Cuentameuncuento/contenido/catalogo.json`.

## Compilar y probar

```bash
./gradlew assembleDebug          # APK de depuración
./gradlew testDebugUnitTest      # tests unitarios
./gradlew lintDebug              # lint (falla si hay errores)
./gradlew bundleRelease          # AAB para Google Play (con R8)
```

El AAB se genera en `app/build/outputs/bundle/release/`. Para firmarlo usa
*Build ▸ Generate Signed App Bundle* en Android Studio con tu clave de subida
(no subas nunca el keystore al repositorio; `.gitignore` ya lo excluye).

## Antes de publicar

- La app no tiene usuarios ni guarda nada de los niños en servidores.
- Play Console: público objetivo con menores de 13 años → política de Familias. En Seguridad
  de los datos solo hay que declarar Crashlytics (registros de fallos y diagnóstico).
- Política de privacidad: `privacy-policy.html` (publicada en GitHub Pages) y `PRIVACY_POLICY.md`.
- Subir `versionCode` en `app/build.gradle.kts`.

La documentación de sesiones anteriores está en `docs/historial/`.
