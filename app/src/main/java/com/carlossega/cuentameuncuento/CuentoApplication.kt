package com.carlossega.cuentameuncuento

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class con Hilt para Dependency Injection
 */
@HiltAndroidApp
class CuentoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configurar Crashlytics
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        // Log de inicio
        android.util.Log.d("CuentoApplication", "App iniciada correctamente")
    }
}
