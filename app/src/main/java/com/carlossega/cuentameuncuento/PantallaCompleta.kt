package com.carlossega.cuentameuncuento

import android.view.View
import android.view.WindowManager

/**
 * Utilidad para manejar pantalla completa (ocultar barras del sistema)
 */
class PantallaCompleta {

    fun pantallaCompleta(decorView: View) {
        decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }
}
