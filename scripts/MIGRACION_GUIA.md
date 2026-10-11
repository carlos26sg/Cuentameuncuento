# Migración de Datos - Guía Paso a Paso

## ⚠️ IMPORTANTE: Leer completamente antes de ejecutar

Esta migración es **OBLIGATORIA** antes de publicar la actualización en Google Play Store.

---

## ¿Por qué es necesaria?

La versión antigua almacenaba:
- Email y contraseña (cifrada inseguramente) en Firestore
- Colección: `usuario`
- Documento ID: el email del usuario

La versión nueva usa:
- Firebase Anonymous Authentication (sin contraseñas)
- Colección: `usuarios`  
- Documento ID: el UID de Firebase Auth

**Sin migración**: Los usuarios perderán sus preferencias (nombre, idioma, favoritos)

---

## Prerrequisitos

1. **Node.js instalado**
   ```bash
   node --version  # Debe ser v14+
   ```

2. **Firebase CLI**
   ```bash
   npm install -g firebase-tools
   firebase login
   ```

3. **Service Account Key**
   - Ve a Firebase Console → Project Settings → Service Accounts
   - Click "Generate new private key"
   - Guarda como `serviceAccountKey.json` en la carpeta `scripts/`

---

## Pasos de Migración

### 1. Preparación

```bash
cd scripts/
npm install firebase-admin
```

### 2. Backup Manual (Recomendado)

Antes de ejecutar el script, haz un backup manual en Firebase Console:
1. Firestore Database → Export
2. Selecciona la colección `usuario`
3. Guarda el backup en Cloud Storage

### 3. Ejecutar Migración

```bash
node migracion_firestore.js
```

**Salida esperada:**
```
🚀 Iniciando migración de datos...

📖 Leyendo usuarios antiguos...
✅ Encontrados 25 usuarios

💾 Creando backup...
✅ Backup guardado en backup_usuarios.json

🔄 Migrando usuarios...
  Migrando: Carlos
  ✅ Migrado con UID: xYz123...
  ...

📊 REPORTE DE MIGRACIÓN
==================================================
Total usuarios: 25
✅ Exitosos: 25
❌ Fallidos: 0

✅ MIGRACIÓN COMPLETADA
```

### 4. Verificación

1. **En Firebase Console:**
   - Ve a Firestore Database
   - Verifica que existe la colección `usuarios`
   - Comprueba que cada documento tiene los campos correctos

2. **En Authentication:**
   - Ve a Firebase Authentication
   - Verifica que se crearon usuarios anónimos
   - Número de usuarios = número migrado

3. **Probar la app:**
   - Instala la versión actualizada
   - Verifica que los datos se cargan correctamente
   - Prueba guardar cambios en el perfil

### 5. Limpieza (Solo después de verificar)

Una vez confirmado que todo funciona:

1. **Eliminar colección antigua:**
   ```bash
   # En Firebase Console, elimina manualmente la colección "usuario"
   # O usa este comando (CUIDADO):
   firebase firestore:delete usuario --recursive
   ```

2. **Eliminar archivos temporales:**
   ```bash
   rm serviceAccountKey.json  # IMPORTANTE: No subir a Git
   ```

---

## Archivos Generados

- `backup_usuarios.json` - Backup completo antes de migrar
- `reporte_migracion.json` - Resumen de la migración
- `serviceAccountKey.json` - **NO SUBIR A GIT** (agregado a .gitignore)

---

## Problemas Comunes

### Error: "Cannot find module 'firebase-admin'"
**Solución:** 
```bash
npm install firebase-admin
```

### Error: "Service account key not found"
**Solución:** Descarga la clave desde Firebase Console y guárdala como `serviceAccountKey.json`

### Error: "Permission denied"
**Solución:** Asegúrate de que el service account tiene permisos de admin en Firebase

### Algunos usuarios fallan
**Solución:** Revisa `reporte_migracion.json` para ver los errores específicos. Puedes migrarlos manualmente.

---

## Comunicación a Usuarios

Después de la migración, considera notificar a los usuarios:

**Opción A: Notificación en la app**
- Mostrar un diálogo al abrir la app por primera vez
- "Hemos mejorado la seguridad. Por favor, revisa tu perfil."

**Opción B: Email/Push notification**
- "Actualización importante: Nueva versión con seguridad mejorada"
- "Tus cuentos favoritos se mantienen, pero verifica tu perfil"

---

## Rollback (Si algo sale mal)

Si necesitas volver atrás:

1. **No publiques la actualización en Play Store**
2. **Restaura el backup:**
   ```javascript
   // restore_backup.js
   const backup = require('./backup_usuarios.json');
   backup.forEach(async (usuario) => {
     await db.collection('usuario').doc(usuario.id).set(usuario);
   });
   ```

3. **Elimina usuarios de Auth si es necesario:**
   - Firebase Console → Authentication → Borrar usuarios creados

---

## Checklist Final

Antes de publicar en Play Store:

- [ ] Migración ejecutada exitosamente
- [ ] Backup guardado y verificado
- [ ] Datos verificados en Firebase Console
- [ ] App probada con datos migrados
- [ ] Colección antigua eliminada
- [ ] Service account key eliminado (no en Git)
- [ ] Reporte de migración revisado
- [ ] Comunicación a usuarios preparada

---

## Soporte

Si encuentras problemas durante la migración, revisa:
1. Los logs del script
2. `reporte_migracion.json`
3. Firebase Console logs
4. Crashlytics (si ya está configurado)

---

*Última actualización: 2026*
