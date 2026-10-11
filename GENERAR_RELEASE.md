# Guía Rápida para Generar Release Build

## Paso 1: Crear Keystore (Solo primera vez)

Si aún no tienes un keystore, créalo:

```bash
keytool -genkey -v -keystore cuentameuncuento.keystore -alias cuentameuncuento -keyalg RSA -keysize 2048 -validity 10000
```

Te pedirá:
- **Password del keystore**: (anótalo, lo necesitarás)
- **Password del key**: (puede ser el mismo)
- Nombre, organización, etc.

**⚠️ IMPORTANTE: Guarda el keystore y las contraseñas en lugar seguro. Sin él no podrás actualizar la app.**

---

## Paso 2: Configurar Signing en build.gradle

Ya está casi listo en tu `app/build.gradle`, solo necesitas agregar las contraseñas.

---

## Paso 3: Generar el Build

```bash
# Para Google Play Store (recomendado)
./gradlew bundleRelease

# O si prefieres APK
./gradlew assembleRelease
```

**Salida estará en:**
- Bundle: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`

---

## Paso 4: Subir a Play Console

1. Ve a https://play.google.com/console
2. Selecciona tu app o crea una nueva
3. En "Production" o "Testing", click "Create new release"
4. Sube el AAB
5. Completa la información requerida
6. Agrega la URL de tu política de privacidad:
   ```
   https://carlos26sg.github.io/Cuentameuncuento/privacy-policy.html
   ```

---

¿Necesitas ayuda con algún paso específico?
