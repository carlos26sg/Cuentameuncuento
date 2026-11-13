package com.carlossega.cuentameuncuento

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.util.DebugLogger
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.carlossega.cuentameuncuento.audio.MusicaFondo
import com.carlossega.cuentameuncuento.data.prefs.PreferenciasStore
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject

/**
 * Application class con Hilt para Dependency Injection
 */
@HiltAndroidApp
class CuentoApplication : Application(), SingletonImageLoader.Factory {

    @Inject
    lateinit var musicaFondo: MusicaFondo

    @Inject
    lateinit var preferenciasStore: PreferenciasStore

    @Inject
    lateinit var httpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()
        preferenciasStore.borrarPreferenciasAntiguas()
        borrarCachesDeFirestore()
        ProcessLifecycleOwner.get().lifecycle.addObserver(musicaFondo)
    }

    /**
     * Hasta la versión 2.1 los cuentos se leían de Firestore: borra su caché local y la
     * carpeta donde se guardaban los textos, que ya no se usan.
     */
    private fun borrarCachesDeFirestore() {
        File(noBackupFilesDir, "cuentos").takeIf { it.exists() }?.deleteRecursively()
        databaseList().filter { it.startsWith("firestore.") }.forEach { deleteDatabase(it) }
    }

    // Coil comparte el cliente HTTP con el resto de la app y guarda las imágenes
    // en su caché de disco, así que las ilustraciones ya vistas funcionan sin conexión
    override fun newImageLoader(context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { httpClient })) }
            .crossfade(true)
            .apply { if (BuildConfig.DEBUG) logger(DebugLogger()) }
            .build()
}
