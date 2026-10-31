package com.carlossega.cuentameuncuento.di

import com.carlossega.cuentameuncuento.audio.Narrador
import com.carlossega.cuentameuncuento.audio.TtsNarrador
import com.carlossega.cuentameuncuento.data.precarga.CoilPrecargaImagenes
import com.carlossega.cuentameuncuento.data.precarga.PrecargaImagenes
import com.carlossega.cuentameuncuento.data.repository.CuentoRepository
import com.carlossega.cuentameuncuento.data.repository.FirestoreCuentoRepository
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
import com.carlossega.cuentameuncuento.data.prefs.PreferenciasStore
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** Scope que vive lo mismo que la app, para trabajo en segundo plano que no depende de una pantalla */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

/**
 * Módulo Hilt con las dependencias de toda la aplicación
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindCuentoRepository(impl: FirestoreCuentoRepository): CuentoRepository

    @Binds
    abstract fun bindPreferenciasRepository(impl: PreferenciasStore): PreferenciasRepository

    @Binds
    abstract fun bindPrecargaImagenes(impl: CoilPrecargaImagenes): PrecargaImagenes
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class AudioModule {

    // Sin scope: cada ViewModel del reproductor tiene su propio motor de voz
    @Binds
    abstract fun bindNarrador(impl: TtsNarrador): Narrador
}
