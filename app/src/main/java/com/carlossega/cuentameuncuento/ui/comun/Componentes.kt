package com.carlossega.cuentameuncuento.ui.comun

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.audio.MusicaFondo
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.ui.tema.Colores

/** Música de fondo, compartida por todas las pantallas */
val LocalMusica = staticCompositionLocalOf<MusicaFondo?> { null }

/** Margen para no quedar debajo del notch ni de las barras del sistema */
fun Modifier.margenSeguro(): Modifier = safeDrawingPadding()

/** Nombre del idioma en su propio idioma: así lo reconoce quien lo habla */
val Idioma.endonimo: String
    get() = when (this) {
        Idioma.ESPANOL -> "Español"
        Idioma.CATALAN -> "Català"
        Idioma.INGLES -> "English"
        Idioma.FRANCES -> "Français"
        Idioma.ALEMAN -> "Deutsch"
        Idioma.GALLEGO -> "Galego"
        Idioma.EUSKERA -> "Euskara"
    }

val Idioma.sigla: String
    get() = when (this) {
        Idioma.ESPANOL -> "ES"
        Idioma.CATALAN -> "CA"
        Idioma.INGLES -> "EN"
        Idioma.FRANCES -> "FR"
        Idioma.ALEMAN -> "DE"
        Idioma.GALLEGO -> "GL"
        Idioma.EUSKERA -> "EU"
    }

/**
 * Paisaje de la biblioteca: cielo, sol, nubes y colinas dibujados en vectorial, así se
 * ve nítido en cualquier pantalla y no ocupa espacio en la app.
 */
@Composable
fun FondoPaisaje(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(Colores.cielo)
        // Sol y nubes a escala de un móvil en vertical: en horizontal o en tablet no crecen sin límite
        val e = minOf(w, h * 1.56f) / 390f
        drawCircle(Colores.sol, radius = 34 * e, center = Offset(w * 0.815f, h * 0.28f))
        fun nube(x: Float, y: Float, escala: Float) {
            drawOval(Color.White, Offset(x - 34 * escala, y - 14 * escala), Size(68 * escala, 28 * escala))
            drawOval(Color.White, Offset(x - 2 * escala, y - 24 * escala), Size(48 * escala, 32 * escala))
        }
        nube(w * 0.18f, h * 0.37f, e)
        nube(w * 0.58f, h * 0.49f, e * 0.75f)
        val lejana = Path().apply {
            moveTo(0f, h * 0.76f)
            cubicTo(w * 0.18f, h * 0.60f, w * 0.36f, h * 0.64f, w * 0.54f, h * 0.74f)
            cubicTo(w * 0.72f, h * 0.82f, w * 0.87f, h * 0.68f, w, h * 0.64f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(lejana, Colores.colinaLejana)
        val cercana = Path().apply {
            moveTo(0f, h * 0.86f)
            cubicTo(w * 0.23f, h * 0.74f, w * 0.44f, h * 0.82f, w * 0.64f, h * 0.86f)
            cubicTo(w * 0.80f, h * 0.90f, w * 0.92f, h * 0.80f, w, h * 0.82f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(cercana, Colores.colinaCercana)
        val suelo = Path().apply {
            moveTo(0f, h * 0.944f)
            cubicTo(w * 0.31f, h * 0.89f, w * 0.67f, h * 0.96f, w, h * 0.91f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(suelo, Colores.fondo)
    }
}

/** Botón redondo con icono. 44 dp como mínimo (zona táctil cómoda) */
@Composable
fun BotonRedondo(
    icono: ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tamano: Dp = 44.dp,
    tamanoIcono: Dp = 22.dp,
    fondo: Color = Colores.superficie,
    colorIcono: Color = Colores.tinta,
    borde: BorderStroke? = null,
    activo: Boolean = true,
    sombra: Dp = 3.dp
) {
    Surface(
        onClick = onClick,
        enabled = activo,
        shape = CircleShape,
        color = fondo,
        border = borde,
        shadowElevation = if (activo) sombra else 0.dp,
        modifier = modifier.size(tamano)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icono,
                contentDescription = descripcion,
                tint = if (activo) colorIcono else colorIcono.copy(alpha = 0.35f),
                modifier = Modifier.size(tamanoIcono)
            )
        }
    }
}

/** Botón de texto redondo, para el selector de idioma y el "Aa" del lector */
@Composable
fun BotonTexto(texto: String, descripcion: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = Colores.superficie,
        shadowElevation = 3.dp,
        modifier = modifier
            .heightIn(min = 44.dp)
            .semantics(mergeDescendants = true) { role = Role.Button }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 14.dp)) {
            Text(
                texto,
                style = MaterialTheme.typography.labelMedium,
                color = Colores.tinta,
                modifier = Modifier.semantics { this.contentDescription = descripcion }
            )
        }
    }
}

/** Acción principal (coral): "Leer" */
@Composable
fun BotonPrincipal(texto: String, icono: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Colores.coral, contentColor = Color.White),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = modifier.height(60.dp)
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

/** Acción secundaria (contorno): "Escuchar" */
@Composable
fun BotonSecundario(texto: String, icono: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(2.dp, Colores.tinta),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Colores.superficie, contentColor = Colores.tinta),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = modifier.height(60.dp)
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

/** Dato corto en una píldora blanca: "3+ años", "≈ 6 min"… */
@Composable
fun Etiqueta(texto: String, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(14.dp), color = Colores.superficie, shadowElevation = 1.dp, modifier = modifier) {
        Text(
            texto,
            style = MaterialTheme.typography.bodyMedium,
            color = Colores.tinta,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/** Corazón de favorito, con un pequeño rebote al marcarlo */
@Composable
fun BotonCorazon(favorito: Boolean, alAlternar: () -> Unit, modifier: Modifier = Modifier, tamano: Dp = 44.dp) {
    val escala by animateFloatAsState(if (favorito) 1.15f else 1f, label = "corazon")
    // Círculo blanco opaco con sombra propia: con fondo translúcido la sombra se veía como un halo
    IconToggleButton(
        checked = favorito,
        onCheckedChange = { alAlternar() },
        shape = CircleShape,
        colors = IconToggleButtonColors(
            containerColor = Colores.superficie,
            contentColor = Colores.corazon,
            disabledContainerColor = Colores.superficie,
            disabledContentColor = Colores.corazon,
            checkedContainerColor = Colores.superficie,
            checkedContentColor = Colores.corazon
        ),
        modifier = modifier
            .size(tamano)
            .shadow(3.dp, CircleShape, clip = true)
    ) {
        Icon(
            imageVector = if (favorito) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = stringResource(if (favorito) R.string.quitar_favorito else R.string.anadir_favorito),
            modifier = Modifier
                .size(tamano * 0.5f)
                .scale(escala)
        )
    }
}

/**
 * Menú con los idiomas escritos en su propio idioma (sin banderas: una bandera es un país,
 * no un idioma). Con [conNinguno] la primera opción es "sin traducción".
 */
@Composable
fun MenuIdiomas(
    abierto: Boolean,
    alCerrar: () -> Unit,
    seleccionado: Idioma?,
    opciones: List<Idioma>,
    alElegir: (Idioma?) -> Unit,
    conNinguno: Boolean = false
) {
    DropdownMenu(expanded = abierto, onDismissRequest = alCerrar, containerColor = Colores.superficie) {
        if (conNinguno) {
            OpcionIdioma(stringResource(R.string.sin_traduccion), seleccionado == null) { alElegir(null) }
        }
        opciones.forEach { idioma ->
            OpcionIdioma(idioma.endonimo, seleccionado == idioma) { alElegir(idioma) }
        }
    }
}

@Composable
private fun OpcionIdioma(texto: String, elegida: Boolean, alPulsar: () -> Unit) {
    DropdownMenuItem(
        text = {
            Text(
                texto,
                style = MaterialTheme.typography.titleMedium,
                color = if (elegida) Colores.coral else Colores.tinta
            )
        },
        onClick = alPulsar,
        modifier = Modifier.heightIn(min = 48.dp)
    )
}
