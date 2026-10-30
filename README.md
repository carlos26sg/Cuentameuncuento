# Cuéntame un cuento

App Android de cuentos infantiles en español, catalán e inglés. Se pueden **leer** línea a línea
o **escuchar** con la voz del sistema (texto a voz), con traducción opcional debajo de cada línea
e ilustraciones a pantalla completa.

## Stack

| | |
|---|---|
| Lenguaje | Kotlin 2.3 (Kotlin integrado de AGP 9) |
| Build | AGP 9.3.3 · Gradle 9.7.1 · JDK 17 · catálogo de versiones en `gradle/libs.versions.toml` |
| SDK | minSdk 26 · targetSdk 37 · compileSdk 37 |
| UI | Views + ViewBinding, ViewModel + StateFlow |
| DI | Hilt |
| Datos | Firestore y Storage (solo lectura de cuentos), DataStore (preferencias locales) |
| Red / imágenes | OkHttp + Coil 3 (caché en disco) |
| Calidad | Crashlytics, Analytics (configurado para apps infantiles), lint, tests unitarios |

## Estructura

```
com.carlossega.cuentameuncuento
├── SplashActivity          ← icono del launcher (no mover de paquete)
├── CuentoApplication
├── audio/                  ← música de fondo (foco de audio) y narrador (TextToSpeech)
├── data/
│   ├── model/              ← Cuento, LineaCuento, Idioma, Modo, Preferencias
│   ├── prefs/              ← DataStore (preferencias, solo en el dispositivo)
│   └── repository/         ← interfaces + implementación Firestore
├── di/                     ← módulos Hilt
└── ui/
    ├── common/             ← pantalla completa, botón de música, adapter de banderas
    ├── menu/  seleccion/  reproductor/  ajustes/
```

## Datos en Firestore

- `cuentos/{id}`: `id`, `imagen` (portada), `titulo_esp|cat|eng`, `desc_…`, `txt_…` (URL del texto)
- `imagenes/{id}`: `img_1`, `img_2`… (URL de cada ilustración)

Cada línea del fichero de texto es `texto/númeroDeImagen`; la última es `FIN`, `FI` o `THE END`.

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

- La app no tiene usuarios: no guarda nada de los niños en servidores. Las reglas de Firestore
  solo necesitan permitir **lectura** de `cuentos` e `imagenes` (y ninguna escritura).
- Play Console: público objetivo con menores de 13 años → política de Familias. En Seguridad
  de los datos solo hay que declarar Analytics y Crashlytics (datos de uso y diagnóstico).
- Política de privacidad: `privacy-policy.html` (publicada en GitHub Pages) y `PRIVACY_POLICY.md`.
- Subir `versionCode` en `app/build.gradle.kts`.

La documentación de sesiones anteriores está en `docs/historial/`.
