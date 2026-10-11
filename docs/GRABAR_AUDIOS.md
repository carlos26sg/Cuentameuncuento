# 🎙️ Guía para Grabar Audios Reales de los Cuentos

## 📋 Visión General

En lugar de Text-to-Speech robótico, grabar audios reales de voz humana para cada cuento.

---

## 🎯 Por Qué Hacerlo

### Ventajas:
- ✅ **Calidad profesional** - Voz natural y expresiva
- ✅ **Mejor experiencia** - Los niños prefieren voz humana
- ✅ **Sin costos recurrentes** - Una vez grabado, es gratis
- ✅ **Funciona offline** - No requiere internet

### Desventajas:
- ❌ **Tiempo inicial** - Grabar cada cuento lleva tiempo
- ❌ **Equipo necesario** - Micrófono decente
- ❌ **Difícil actualizar** - Agregar cuentos requiere nueva grabación

---

## 🛠️ Equipo Necesario

### Opción Económica (~30-50€):
- **Micrófono USB**: Blue Snowball Ice / Fifine K669
- **Software gratuito**: Audacity
- **Habitación silenciosa**

### Opción Profesional (~150-300€):
- **Micrófono**: Audio-Technica AT2020 / Blue Yeti
- **Interfaz de audio**: Focusrite Scarlett Solo
- **Software**: Audacity (gratis) o Adobe Audition

### Mínimo aceptable:
- **Smartphone moderno** con app de grabación
- **Habitación silenciosa** sin eco
- **Editor de audio gratuito**: Audacity (PC) o WavePad (móvil)

---

## 📝 Preparación

### 1. Preparar los Textos

Exporta cada cuento desde Firestore:

```javascript
// Script para exportar cuentos desde Firestore
const db = admin.firestore();

async function exportarCuentos() {
  const cuentos = await db.collection('cuentos').get();
  
  for (const doc of cuentos.docs) {
    const id = doc.id;
    const titulo_esp = doc.data().titulo_esp;
    const txt_url = doc.data().txt_esp;
    
    // Descargar el .txt desde la URL
    const response = await fetch(txt_url);
    const texto = await response.text();
    
    // Guardar como archivo
    fs.writeFileSync(`cuentos/${id}_esp.txt`, texto);
  }
}
```

### 2. Limpiar los Textos

Cada línea del cuento tiene formato: `texto/numero_imagen`

```
Había una vez un pájaro amarillo/1
que vivía en un árbol grande/2
```

**Crear versión limpia solo con texto:**
```
Había una vez un pájaro amarillo
que vivía en un árbol grande
```

---

## 🎬 Proceso de Grabación

### Paso 1: Configurar Audacity

1. Descargar: https://www.audacityteam.org/
2. Configuración recomendada:
   - **Sample Rate**: 44100 Hz
   - **Format**: 16-bit PCM
   - **Mono** (para voz es suficiente)

### Paso 2: Grabar

**Consejos:**
- 🎤 Micrófono a 15-20cm de la boca
- 🔇 Habitación silenciosa (apaga ventiladores, AC)
- 📖 Lee con expresión, como si le contaras a un niño
- ⏸️ Pausas naturales entre frases
- 🎭 Voces diferentes para personajes (opcional pero genial)

**Técnica:**
```
1. Grabar TODO el cuento de corrido
2. Si te equivocas, haz pausa y repite la frase
3. No borres nada durante la grabación
4. Edita después
```

### Paso 3: Editar

**En Audacity:**

1. **Eliminar silencios largos**:
   - Effect → Truncate Silence
   - Threshold: -40 dB
   - Duration: > 0.5s

2. **Normalizar volumen**:
   - Effect → Normalize
   - -3 dB (deja margen para no distorsionar)

3. **Quitar ruido de fondo**:
   - Graba 3 segundos de silencio al inicio
   - Selecciona ese silencio → Effect → Noise Reduction → Get Noise Profile
   - Selecciona todo → Effect → Noise Reduction → OK

4. **Cortar en segmentos**:
   - Cada frase = un archivo separado
   - Nombrar: `pajaro_amarillo_esp_001.mp3`, `pajaro_amarillo_esp_002.mp3`...

### Paso 4: Exportar

**Formato recomendado: MP3**
- Bitrate: 128 kbps (suficiente para voz)
- Constant Bitrate (CBR)

---

## 📁 Estructura de Archivos

```
audios/
├── pajaro_amarillo/
│   ├── esp/
│   │   ├── 001.mp3  (línea 1)
│   │   ├── 002.mp3  (línea 2)
│   │   └── ...
│   ├── cat/
│   │   └── ...
│   └── eng/
│       └── ...
├── caperucita_roja/
│   └── ...
```

---

## 💾 Almacenamiento

### Opción 1: Firebase Storage (Recomendado)
```kotlin
// Subir audios a Firebase Storage
val storageRef = Firebase.storage.reference
val audioRef = storageRef.child("audios/pajaro_amarillo/esp/001.mp3")

audioRef.putFile(localFile)
    .addOnSuccessListener {
        audioRef.downloadUrl.addOnSuccessListener { uri ->
            // Guardar URL en Firestore
        }
    }
```

**Costos**: ~$0.026 por GB/mes (muy barato)

### Opción 2: Embeberlos en la App
- Poner los MP3 en `app/src/main/res/raw/`
- ⚠️ Aumenta mucho el tamaño del APK
- Solo viable para pocos cuentos

---

## 🔧 Modificar el Código

### 1. Cambiar ReproductorCuento.kt

```kotlin
// En lugar de TTS, usar MediaPlayer
private var mediaPlayer: MediaPlayer? = null

private fun reproducirLineaConAudio(lineaIndex: Int) {
    try {
        val audioUrl = obtenerAudioUrl(nombreCuento, idioma, lineaIndex)
        
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioUrl)
            setOnPreparedListener {
                start()
            }
            setOnCompletionListener {
                avanzarLinea()
                reproducirLineaConAudio(contadorLineas)
            }
            prepareAsync()
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error reproduciendo audio: ${e.message}")
        // Fallback a TTS si falla el audio
        iniciarTextToSpeech()
    }
}

private suspend fun obtenerAudioUrl(cuento: String, idioma: String, linea: Int): String {
    // Obtener URL desde Firestore o construirla
    val audioPath = "audios/$cuento/$idioma/${linea.toString().padStart(3, '0')}.mp3"
    
    return withContext(Dispatchers.IO) {
        val storageRef = Firebase.storage.reference.child(audioPath)
        storageRef.downloadUrl.await().toString()
    }
}
```

### 2. Actualizar Firestore

Agregar campo en cada cuento:
```json
{
  "id": "pajaro_amarillo",
  "titulo_esp": "El Pájaro Amarillo",
  "txt_esp": "https://...",
  "tiene_audio": true,  // ← NUEVO
  "audio_lineas": 15    // ← NUEVO (número de archivos de audio)
}
```

---

## ⏱️ Estimación de Tiempo

Por cuento (aprox. 15 líneas):
- Preparación: 10 min
- Grabación: 15-20 min
- Edición: 20-30 min
- Subida: 5 min

**Total: ~1 hora por cuento** (mejora con práctica)

---

## 🎭 Consejo PRO: Actores/Narradore

### DIY (Hazlo tú):
- ✅ Gratis
- ✅ Control total
- ❌ Calidad variable

### Contratar Narrador en Fiverr:
- Costo: 20-50€ por cuento
- Buscar: "Spanish voice over children stories"
- ✅ Calidad profesional
- ✅ Rápido

### Colaborar con Escuela de Locución:
- Contactar escuelas locales
- Estudiantes graban como práctica
- ✅ Gratis o barato
- ✅ Calidad decente

---

## 📊 Comparación Final

| Aspecto | TTS Actual | Audios Grabados |
|---------|-----------|-----------------|
| **Calidad** | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Costo** | Gratis | Tiempo/Dinero inicial |
| **Mantenimiento** | Cero | Bajo |
| **Funciona offline** | ✅ | ✅ |
| **Nuevos cuentos** | Inmediato | Requiere grabación |
| **Expresividad** | ❌ | ✅✅✅ |

---

## 🎯 Recomendación

**Para v2.0 (ahora)**: Publicar con TTS actual

**Para v2.1 (próxima actualización)**: 
1. Grabar 2-3 cuentos más populares
2. Agregar selector: "Audio grabado" vs "Voz automática"
3. Ir agregando más cuentos gradualmente

**Estrategia híbrida**:
- Cuentos populares → Audio grabado profesional
- Cuentos nuevos/experimentales → TTS automático
- Usuario elige en configuración

---

*Documentación creada: 2026*
*Estado: Pendiente de implementación (post v2.0)*
