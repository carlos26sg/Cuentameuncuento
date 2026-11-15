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
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.LetraCuento
import com.carlossega.cuentameuncuento.data.model.TamanoTexto

/** Paleta: fondo crema, tinta morada oscura y coral para las acciones */
object Colores {
    val fondo = Color(0xFFFFF6E9)
    val superficie = Color(0xFFFFFFFF)
    val cremaOscuro = Color(0xFFEFE4D2)
    val borde = Color(0xFFE3D5C0)
    val tinta = Color(0xFF2A2240)
    val tintaSuave = Color(0xFF6B6178)
    val coral = Color(0xFFEE6A4A)
    val corazon = Color(0xFFE0465E)
    val resaltado = Color(0xFFFFE08A)
    val capsula = Color(0xC71E1A2E)

    // Paisaje de la biblioteca
    val cielo = Color(0xFFCBEAF5)
    val sol = Color(0xFFFFD36B)
    val colinaLejana = Color(0xFF9ED39B)
    val colinaCercana = Color(0xFF6DBB7A)
}

/** Fredoka: títulos y botones (redondeada y amable; fuente variable) */
val Fredoka = FontFamily(
    Font(R.font.fredoka, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.fredoka, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600)))
)

/** Andika: textos de la interfaz, pensada para niños que aprenden a leer */
val Andika = FontFamily(Font(R.font.andika))

fun LetraCuento.familia(): FontFamily = FontFamily(Font(fuente))

/** Letra y tamaño que ha elegido el usuario para el texto de los cuentos */
val LocalLetraCuento = staticCompositionLocalOf { Andika }
val LocalTamanoTexto = staticCompositionLocalOf { TamanoTexto.NORMAL }

private val tipografia = Typography().let { base ->
    base.copy(
        displaySmall = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp),
        headlineSmall = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 25.sp, lineHeight = 30.sp),
        titleLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 26.sp),
        titleMedium = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
        titleSmall = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 19.sp),
        labelLarge = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 19.sp),
        labelMedium = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
        bodyLarge = TextStyle(fontFamily = Andika, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = Andika, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontFamily = Andika, fontSize = 12.sp, lineHeight = 16.sp)
    )
}

@Composable
fun CuentameTheme(letra: LetraCuento, tamano: TamanoTexto, content: @Composable () -> Unit) {
    val familia = remember(letra) { letra.familia() }
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Colores.coral,
            onPrimary = Color.White,
            secondary = Colores.tinta,
            background = Colores.fondo,
            onBackground = Colores.tinta,
            surface = Colores.fondo,
            onSurface = Colores.tinta,
            surfaceContainerLow = Colores.fondo,
            surfaceVariant = Colores.cremaOscuro,
            onSurfaceVariant = Colores.tintaSuave,
            outline = Colores.borde
        ),
        typography = tipografia
    ) {
        CompositionLocalProvider(
            LocalLetraCuento provides familia,
            LocalTamanoTexto provides tamano,
            content = content
        )
    }
}

/** Estilo del texto de los cuentos: letra y tamaño elegidos en Ajustes */
@Composable
fun estiloCuento(tamanoBase: Int): TextStyle {
    val tamano = tamanoBase * LocalTamanoTexto.current.factor
    return TextStyle(
        fontFamily = LocalLetraCuento.current,
        fontSize = tamano.sp,
        lineHeight = (tamano * 1.42f).sp
    )
}

/** true si el usuario ha desactivado las animaciones en los ajustes de accesibilidad del sistema */
@Composable
fun movimientoReducido(): Boolean {
    val contexto = LocalContext.current
    return remember {
        Settings.Global.getFloat(contexto.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
