package com.carlossega.cuentameuncuento.ui.tema

import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.LetraCuento

/** Colores de papel y tinta, a juego con los botones de pergamino dibujados a mano */
object Colores {
    val papel = Color(0xFFF8EFD9)
    val papelTranslucido = Color(0xE6F8EFD9)
    val papelOscuro = Color(0xFFEADFC6)
    val borde = Color(0xFFC9A46A)
    val tinta = Color(0xFF4A2C12)
    val tintaSuave = Color(0xFF6B5440)
    val corazon = Color(0xFFD7263D)

    /** Palabra que suena mientras narra: sobre la ilustración (texto blanco) y sobre papel */
    val palabraSobreIlustracion = Color(0xFFFFC94D)
    val palabraSobrePapel = Color(0xFFFFE08A)
    val traduccion = Color(0xFFDCF812)
}

/** Letra manuscrita de la app: títulos y botones */
val Anna = FontFamily(Font(R.font.anna))

fun LetraCuento.familia(): FontFamily = FontFamily(Font(fuente))

/** Letra que ha elegido el usuario para el texto de los cuentos (Ajustes) */
val LocalLetraCuento = staticCompositionLocalOf { FontFamily(Font(R.font.andika)) }

private val tipografia = Typography().let { base ->
    base.copy(
        displayMedium = base.displayMedium.copy(fontFamily = Anna),
        headlineLarge = base.headlineLarge.copy(fontFamily = Anna),
        headlineMedium = base.headlineMedium.copy(fontFamily = Anna),
        headlineSmall = base.headlineSmall.copy(fontFamily = Anna),
        titleLarge = base.titleLarge.copy(fontFamily = Anna, fontSize = 26.sp),
        titleMedium = base.titleMedium.copy(fontFamily = Anna, fontSize = 20.sp),
        labelLarge = base.labelLarge.copy(fontFamily = Anna, fontSize = 22.sp),
        bodyLarge = base.bodyLarge.copy(fontSize = 18.sp),
        bodyMedium = base.bodyMedium.copy(fontSize = 16.sp)
    )
}

@Composable
fun CuentameTheme(letra: LetraCuento, content: @Composable () -> Unit) {
    val familia = remember(letra) { letra.familia() }
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Colores.tinta,
            onPrimary = Colores.papel,
            secondary = Colores.borde,
            background = Colores.papel,
            onBackground = Colores.tinta,
            surface = Colores.papel,
            onSurface = Colores.tinta,
            surfaceVariant = Colores.papelOscuro,
            onSurfaceVariant = Colores.tintaSuave,
            outline = Colores.borde
        ),
        typography = tipografia
    ) {
        CompositionLocalProvider(LocalLetraCuento provides familia, content = content)
    }
}

/** Estilo del texto de los cuentos, con la letra elegida en Ajustes */
@Composable
fun estiloCuento(tamano: Int = 24): TextStyle =
    MaterialTheme.typography.bodyLarge.copy(
        fontFamily = LocalLetraCuento.current,
        fontSize = tamano.sp,
        lineHeight = (tamano * 1.4f).sp
    )

/** true si el usuario ha desactivado las animaciones en los ajustes de accesibilidad del sistema */
@Composable
fun movimientoReducido(): Boolean {
    val contexto = LocalContext.current
    return remember {
        Settings.Global.getFloat(contexto.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
