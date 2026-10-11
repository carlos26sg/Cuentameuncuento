# Scripts del Proyecto

## Directorio de Scripts

Este directorio contiene scripts de mantenimiento y migración para el proyecto.

## Archivos

### `migracion_firestore.js`
Script de migración de datos de Firestore.
- Migra usuarios de la colección antigua a la nueva
- Genera backups automáticos
- Ver `MIGRACION_GUIA.md` para instrucciones completas

### `MIGRACION_GUIA.md`
Guía detallada paso a paso para ejecutar la migración de datos.

## Uso

```bash
cd scripts/
npm install
node migracion_firestore.js
```

## ⚠️ Seguridad

**NO SUBIR A GIT:**
- `serviceAccountKey.json`
- `backup_usuarios.json` (contiene datos de usuarios)
- `reporte_migracion.json` (puede contener información sensible)

Estos archivos están incluidos en `.gitignore`.
