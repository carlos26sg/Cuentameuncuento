package com.carlossega.cuentameuncuento.ui.comun

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

/** Ilustración de fondo que llena la pantalla */
@Composable
fun FondoIlustrado(@DrawableRes fondo: Int, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(fondo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        content()
    }
}

/** Botón con el pergamino dibujado a mano */
@Composable
fun BotonPergamino(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ancho: Dp = 200.dp,
    descripcion: String = texto
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .sizeIn(minWidth = ancho, minHeight = 64.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = descripcion }
    ) {
        Image(
            painter = painterResource(R.drawable.pergamino),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = Colores.tinta,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
    }
}

/** Botón con uno de los dibujos de la app (flechas, play, equis...). Mínimo 48 dp de zona táctil */
@Composable
fun BotonDibujo(
    @DrawableRes dibujo: Int,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tamano: Dp = 56.dp,
    activo: Boolean = true
) {
    Image(
        painter = painterResource(dibujo),
        contentDescription = descripcion,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(tamano)
            .alpha(if (activo) 1f else 0.35f)
            .clickable(enabled = activo, role = Role.Button, onClick = onClick)
    )
}

/** Interruptor de la música de fondo (mismo estado en todas las pantallas) */
@Composable
fun BotonMusica(modifier: Modifier = Modifier) {
    val musica = LocalMusica.current ?: return
    val activa by musica.activa.collectAsState()
    BotonDibujo(
        dibujo = if (activa) R.drawable.audio else R.drawable.sin_sonido,
        descripcion = stringResource(if (activa) R.string.silenciar_musica else R.string.activar_musica),
        onClick = musica::alternar,
        modifier = modifier,
        tamano = 52.dp
    )
}

/** Panel de papel con borde de pergamino */
@Composable
fun PanelPapel(
    modifier: Modifier = Modifier,
    color: Color = Colores.papel,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color,
        contentColor = Colores.tinta,
        border = BorderStroke(2.dp, Colores.borde),
        shadowElevation = 4.dp
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

/** Título de pantalla en letra manuscrita, marcado como encabezado para TalkBack */
@Composable
fun TituloPantalla(texto: String, modifier: Modifier = Modifier, color: Color = Color.Black) {
    Text(
        text = texto,
        style = MaterialTheme.typography.headlineMedium,
        color = color,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.semantics { heading() }
    )
}

/** Sombra para escribir en blanco sobre ilustraciones */
val sombraTexto = Shadow(color = Color.Black, offset = Offset(3f, 3f), blurRadius = 6f)

val Idioma.nombre: Int
    get() = when (this) {
        Idioma.ESPANOL -> R.string.idioma_espanol
        Idioma.CATALAN -> R.string.idioma_catalan
        Idioma.INGLES -> R.string.idioma_ingles
    }

/**
 * Bandera que abre la lista de idiomas. Con [conNinguno] la primera opción es "sin idioma"
 * (para desactivar la traducción).
 */
@Composable
fun SelectorIdioma(
    seleccionado: Idioma?,
    descripcion: String,
    onSeleccion: (Idioma?) -> Unit,
    modifier: Modifier = Modifier,
    conNinguno: Boolean = false
) {
    var abierto by rememberSaveable { mutableStateOf(false) }
    val opciones = remember(conNinguno) { if (conNinguno) listOf(null) + Idioma.entries else Idioma.entries }
    val nombreActual = stringResource(seleccionado?.nombre ?: R.string.sin_traduccion)
    Box(modifier) {
        BotonDibujo(
            dibujo = seleccionado?.bandera ?: R.drawable.sin_bandera,
            descripcion = "$descripcion: $nombreActual",
            onClick = { abierto = true },
            tamano = 60.dp
        )
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            containerColor = Colores.papel
        ) {
            opciones.forEach { idioma ->
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(idioma?.nombre ?: R.string.sin_traduccion),
                            style = MaterialTheme.typography.titleMedium,
                            color = Colores.tinta
                        )
                    },
                    leadingIcon = {
                        Image(
                            painter = painterResource(idioma?.bandera ?: R.drawable.sin_bandera),
                            contentDescription = null,
                            modifier = Modifier.width(48.dp)
                        )
                    },
                    onClick = {
                        abierto = false
                        onSeleccion(idioma)
                    }
                )
            }
        }
    }
}
