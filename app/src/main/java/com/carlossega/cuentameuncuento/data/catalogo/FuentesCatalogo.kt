package com.carlossega.cuentameuncuento.data.catalogo

import android.content.Context
import com.carlossega.cuentameuncuento.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * De dónde sale el catálogo. Separado de [CatalogoCuentos] para poder probarlo sin Android.
 */
interface FuentesCatalogo {
    /** El catálogo que viene dentro de la app */
    suspend fun incluido(): String

    /** El último catálogo descargado, si hay alguno */
    suspend fun guardado(): String?

    suspend fun guardar(texto: String)

    /** Descarga el catálogo publicado */
    suspend fun descargar(): String

    /** URL desde la que cargar una imagen del catálogo */
    fun url(ruta: String, incluida: Boolean): String
}

/**
 * El catálogo incluido está en `assets/contenido/` y el publicado en GitHub Pages
 * (rama gh-pages del repositorio), que sirve los ficheros por CDN y sin tokens.
 */
class FuentesCatalogoAndroid @Inject constructor(
    @ApplicationContext private val context: Context,
    httpClient: OkHttpClient,
    @IoDispatcher private val io: CoroutineDispatcher
) : FuentesCatalogo {

    // Cliente propio con caché HTTP: si el catálogo no ha cambiado, el servidor responde
    // "304 sin cambios" y no se vuelve a descargar
    private val cliente = httpClient.newBuilder()
        .cache(Cache(File(context.cacheDir, "catalogo_http"), TAMANO_CACHE))
        .build()

    private val ficheroGuardado = File(context.noBackupFilesDir, "catalogo.json")

    override suspend fun incluido(): String = withContext(io) {
        context.assets.open("$CARPETA/catalogo.json").bufferedReader().use { it.readText() }
    }

    override suspend fun guardado(): String? = withContext(io) {
        ficheroGuardado.takeIf { it.exists() }?.readText()
    }

    override suspend fun guardar(texto: String) = withContext(io) {
        // Primero a un temporal: si la app se cierra a medias no queda un catálogo cortado
        val temporal = File(ficheroGuardado.parentFile, "${ficheroGuardado.name}.tmp")
        temporal.writeText(texto)
        if (!temporal.renameTo(ficheroGuardado)) {
            ficheroGuardado.delete()
            temporal.renameTo(ficheroGuardado)
        }
    }

    override suspend fun descargar(): String = withContext(io) {
        cliente.newCall(Request.Builder().url("$URL_PUBLICADA/catalogo.json").build()).execute().use {
            if (!it.isSuccessful) throw IOException("HTTP ${it.code} al descargar el catálogo")
            it.body.string()
        }
    }

    override fun url(ruta: String, incluida: Boolean): String =
        if (incluida) "file:///android_asset/$CARPETA/$ruta" else "$URL_PUBLICADA/$ruta"

    private companion object {
        const val CARPETA = "contenido"
        const val URL_PUBLICADA = "https://carlos26sg.github.io/Cuentameuncuento/contenido"
        const val TAMANO_CACHE = 2L * 1024 * 1024
    }
}
