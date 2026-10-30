# Reglas de R8 para la versión release.
# Firebase, Hilt, Coil, OkHttp y DataStore ya incluyen sus propias reglas,
# así que aquí solo hace falta lo específico de la app.

# Crashlytics: conservar números de línea para que los informes de errores sean legibles
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
