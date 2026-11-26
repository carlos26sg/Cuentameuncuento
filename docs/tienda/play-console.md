# Play Console: respuestas para la versión 3.0

Textos de la ficha en los 7 idiomas: `docs/tienda/ficha.json` (título, descripción corta,
descripción larga y novedades; todos dentro de los límites de caracteres).

## Contenido de la aplicación

### Política de privacidad
- URL: `https://carlos26sg.github.io/Cuentameuncuento/privacy-policy.html`

### Anuncios
- ¿Contiene anuncios? **No**.

### Acceso a la aplicación
- **Toda la funcionalidad está disponible sin restricciones** (no hay inicio de sesión).

### Público objetivo y contenido
- Grupos de edad: **5 años o menos** y **6-8 años**. (No marcar edades superiores: la app es
  para peques y así se aplica la política de Familias, que es lo que queremos.)
- ¿Puede atraer a niños sin quererlo? No aplica: va dirigida a niños.
- Cumplimiento de la política de Familias: sí. La app no tiene anuncios, compras ni cuentas,
  y los enlaces externos pasan por un control parental.

### Seguridad de los datos
La única librería que envía datos es **Firebase Crashlytics** (informes de fallos).
- ¿Recoge o comparte datos de usuario? **Sí** (solo los de Crashlytics).
- ¿Datos cifrados en tránsito? **Sí**.
- ¿Los usuarios pueden pedir que se eliminen? **Sí**, escribiendo al correo de la política de
  privacidad.
- Tipos de datos (todos **recogidos**, **no compartidos**, **obligatorios**, finalidad
  **funcionamiento de la app** y **analíticas**):
  - Información y rendimiento de la app › **Registros de fallos**
  - Información y rendimiento de la app › **Diagnósticos**
  - Dispositivo u otros identificadores › **Otros identificadores** (el identificador de
    instalación que usa Crashlytics para agrupar los fallos; no es el ID de publicidad)
- Ya **no** hay Analytics ni Firestore en esta versión: si en el formulario anterior aparecían,
  hay que quitarlos.

Comprueba en la lista de Google "SDK autocertificados para Familias" que Firebase
Crashlytics sigue apareciendo (lo estaba en 2025). Si no, se puede quitar Crashlytics.

### Clasificación de contenido (cuestionario IARC)
- Categoría: **Referencia, noticias o educación** (es una app de lectura, no un juego).
- Violencia: hay **violencia fantástica muy leve** propia de los cuentos clásicos (el lobo se
  quema con el agua al bajar por la chimenea; el pirata perdió una pierna, contado sin
  detalle). Responde "sí" a violencia de dibujos animados/fantasía leve, "no" a todo lo
  realista, sangre, miedo intenso, lenguaje, sexo, drogas, apuestas.
- Interacción entre usuarios, compartir ubicación, compras digitales: **no**.
- Resultado esperable: PEGI 3 o PEGI 7. Cualquiera de los dos es correcto.

### Categoría de la ficha
- Categoría: **Educación** (o **Libros y obras de consulta**). Etiquetas: cuentos, lectura,
  idiomas.
- Correo de contacto: el mismo de la política de privacidad.

## Publicación
1. **Pruebas internas**: sube el AAB firmado (versión 3.0, versionCode 9) y pruébalo
   instalándolo desde Play.
2. **Producción**: crea la versión con las "novedades" de `ficha.json`, activa la
   **publicación gestionada** (Panel de publicación › Publicación gestionada) y envíala a
   revisión **el miércoles 25 como muy tarde**.
3. Cuando Google la apruebe, pulsa **Publicar** el viernes 27. Recomendado: despliegue
   escalonado al 20 % y, si Crashlytics no muestra fallos en un día, al 100 %.
4. No borres Firestore ni Storage: las versiones antiguas instaladas los usan hasta que se
   actualicen.
