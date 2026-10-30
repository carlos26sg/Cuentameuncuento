package com.carlossega.cuentameuncuento.ui.common

import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding

/**
 * Pantalla completa compatible con Android 15+ (edge-to-edge obligatorio):
 * oculta las barras del sistema (se muestran deslizando desde el borde) y
 * aplica a [raiz] el margen necesario para no quedar debajo del notch.
 * El fondo de [raiz] sigue llegando hasta los bordes.
 */
fun ComponentActivity.pantallaCompleta(raiz: View) {
    enableEdgeToEdge()
    WindowCompat.getInsetsController(window, window.decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        hide(WindowInsetsCompat.Type.systemBars())
    }

    val izquierda = raiz.paddingLeft
    val arriba = raiz.paddingTop
    val derecha = raiz.paddingRight
    val abajo = raiz.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(raiz) { vista, insets ->
        val margen = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        vista.updatePadding(
            left = izquierda + margen.left,
            top = arriba + margen.top,
            right = derecha + margen.right,
            bottom = abajo + margen.bottom
        )
        insets
    }
}
