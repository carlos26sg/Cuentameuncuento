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
        ProcessLifecycleOwner.get().lifecycle.addObserver(musicaFondo)
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
