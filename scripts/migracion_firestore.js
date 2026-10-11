/**
 * Script de Migración de Datos Firestore
 *
 * IMPORTANTE: Ejecutar ANTES de publicar la actualización en Play Store
 *
 * Este script migra los datos de la colección antigua "usuario" (con contraseñas)
 * a la nueva colección "usuarios" (sin contraseñas, con Firebase Auth)
 *
 * PRERREQUISITOS:
 * 1. Tener acceso admin a Firebase Console
 * 2. Node.js instalado
 * 3. Firebase CLI instalado: npm install -g firebase-tools
 * 4. Autenticado: firebase login
 *
 * PASOS PARA EJECUTAR:
 * 1. Instalar dependencias: npm install firebase-admin
 * 2. Descargar service account key desde Firebase Console
 * 3. Guardar como serviceAccountKey.json en este directorio
 * 4. Ejecutar: node migracion_firestore.js
 *
 * QUÉ HACE:
 * - Lee todos los documentos de la colección "usuario"
 * - Para cada usuario:
 *   - Crea un usuario anónimo en Firebase Auth
 *   - Copia nombre, idioma, favorito, modo_fav
 *   - NO copia email ni contraseña (obsoletos)
 *   - Guarda en la nueva colección "usuarios" con el UID de Auth
 * - Genera un reporte de migración
 *
 * SEGURIDAD:
 * - NO elimina datos antiguos (hacerlo manualmente después de verificar)
 * - Guarda backup en backup_usuarios.json antes de migrar
 *
 * DESPUÉS DE LA MIGRACIÓN:
 * 1. Verificar en Firebase Console que los datos están correctos
 * 2. Probar la app con usuarios migrados
 * 3. Eliminar manualmente la colección "usuario" antigua
 * 4. Notificar a usuarios que deben volver a configurar su perfil
 */

const admin = require('firebase-admin');
const fs = require('fs');

// Inicializar Firebase Admin
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const auth = admin.auth();

async function migrarDatos() {
  console.log('🚀 Iniciando migración de datos...\n');

  try {
    // 1. Leer todos los usuarios antiguos
    console.log('📖 Leyendo usuarios antiguos de la colección "usuario"...');
    const usuariosSnapshot = await db.collection('usuario').get();

    if (usuariosSnapshot.empty) {
      console.log('⚠️  No se encontraron usuarios para migrar');
      return;
    }

    const usuariosAntiguos = [];
    usuariosSnapshot.forEach(doc => {
      usuariosAntiguos.push({
        id: doc.id,
        ...doc.data()
      });
    });

    console.log(`✅ Encontrados ${usuariosAntiguos.length} usuarios\n`);

    // 2. Backup
    console.log('💾 Creando backup...');
    fs.writeFileSync(
      'backup_usuarios.json',
      JSON.stringify(usuariosAntiguos, null, 2)
    );
    console.log('✅ Backup guardado en backup_usuarios.json\n');

    // 3. Migrar cada usuario
    console.log('🔄 Migrando usuarios...\n');
    const resultados = {
      exitosos: 0,
      fallidos: 0,
      errores: []
    };

    for (const usuario of usuariosAntiguos) {
      try {
        console.log(`  Migrando: ${usuario.nombre || usuario.mail || usuario.id}`);

        // Crear usuario anónimo en Firebase Auth
        const userRecord = await auth.createUser({
          disabled: false
        });

        // Crear documento en nueva colección
        const nuevoUsuario = {
          nombre: usuario.nombre || '',
          idioma: usuario.idioma || 'esp',
          favorito: usuario.favorito || '',
          modo_fav: usuario.modo_fav || '',
          migrado_desde: usuario.id,
          migrado_fecha: admin.firestore.FieldValue.serverTimestamp()
        };

        await db.collection('usuarios').doc(userRecord.uid).set(nuevoUsuario);

        console.log(`  ✅ Migrado con UID: ${userRecord.uid}`);
        resultados.exitosos++;

      } catch (error) {
        console.log(`  ❌ Error: ${error.message}`);
        resultados.fallidos++;
        resultados.errores.push({
          usuario: usuario.id,
          error: error.message
        });
      }
    }

    // 4. Reporte final
    console.log('\n📊 REPORTE DE MIGRACIÓN');
    console.log('='.repeat(50));
    console.log(`Total usuarios: ${usuariosAntiguos.length}`);
    console.log(`✅ Exitosos: ${resultados.exitosos}`);
    console.log(`❌ Fallidos: ${resultados.fallidos}`);

    if (resultados.errores.length > 0) {
      console.log('\nErrores:');
      resultados.errores.forEach(e => {
        console.log(`  - ${e.usuario}: ${e.error}`);
      });
    }

    // Guardar reporte
    fs.writeFileSync(
      'reporte_migracion.json',
      JSON.stringify(resultados, null, 2)
    );
    console.log('\n💾 Reporte guardado en reporte_migracion.json');

    console.log('\n✅ MIGRACIÓN COMPLETADA\n');
    console.log('⚠️  IMPORTANTE:');
    console.log('1. Verifica los datos en Firebase Console');
    console.log('2. Prueba la app antes de eliminar datos antiguos');
    console.log('3. Elimina manualmente la colección "usuario" después de verificar');
    console.log('4. Los usuarios deberán reconfigurar su perfil al actualizar\n');

  } catch (error) {
    console.error('❌ Error fatal en la migración:', error);
    process.exit(1);
  }
}

// Ejecutar migración
migrarDatos()
  .then(() => process.exit(0))
  .catch(error => {
    console.error('Error:', error);
    process.exit(1);
  });
