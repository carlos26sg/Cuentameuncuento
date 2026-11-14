package com.carlossega.cuentameuncuento.ui.portada

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.ui.comun.BotonDibujo
import com.carlossega.cuentameuncuento.ui.comun.BotonMusica
import com.carlossega.cuentameuncuento.ui.comun.BotonPergamino
import com.carlossega.cuentameuncuento.ui.comun.margenSeguro
import com.carlossega.cuentameuncuento.ui.comun.sombraTexto
import com.carlossega.cuentameuncuento.ui.tema.Anna
import com.carlossega.cuentameuncuento.ui.tema.Colores
import com.carlossega.cuentameuncuento.ui.tema.estiloCuento
import com.carlossega.cuentameuncuento.ui.tema.movimientoReducido
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Portada de cada cuento: su primera ilustración con un zoom lento, los colores sacados
 * del propio dibujo, estrellitas que flotan y el título apareciendo. Desde aquí se elige
 * leerlo o escucharlo.
 */
@Composable
fun PortadaPantalla(
    alVolver: () -> Unit,
    alEmpezar: (cuentoId: String, idioma: Idioma, escuchar: Boolean, linea: Int) -> Unit,
    viewModel: PortadaViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val favorito by viewModel.favorito.collectAsStateWithLifecycle()
    val lineaGuardada by viewModel.lineaGuardada.collectAsStateWithLifecycle()
    val color = colorDominante(estado.imagen)
    val quieto = movimientoReducido()

    // La entrada se anima una sola vez, no al girar la pantalla
    var mostrado by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(estado.titulo) { if (estado.titulo.isNotEmpty()) mostrado = true }

    Box(
        Modifier
            .fillMaxSize()
            .background(color)
    ) {
        IlustracionConZoom(estado.imagen, quieto)
        // Velo con el color del cuento para que el texto se lea sobre cualquier dibujo
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0f to Color.Transparent, 0.35f to Color.Transparent, 1f to color.copy(alpha = 0.92f)))
        )
        if (!quieto) Estrellitas(Colores.palabraSobreIlustracion)

        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .margenSeguro()
                .padding(16.dp)
        ) {
            val vertical = maxHeight > maxWidth
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                BotonDibujo(R.drawable.atras, stringResource(R.string.atras), alVolver)
                BotonMusica(Modifier.padding(start = 8.dp))
                Spacer(Modifier.weight(1f))
                BotonCorazon(favorito, viewModel::alternarFavorito)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = 760.dp)
                    .padding(bottom = if (vertical) 32.dp else 8.dp)
            ) {
                AnimatedVisibility(
                    visible = mostrado,
                    enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { it / 2 }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = estado.titulo,
                            style = TextStyle(fontFamily = Anna, fontSize = if (vertical) 44.sp else 52.sp, shadow = sombraTexto),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.semantics { heading() }
                        )
                        Text(
                            text = estado.descripcion,
                            style = estiloCuento(18).copy(shadow = sombraTexto),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                        )
                    }
                }
                AnimatedVisibility(
                    visible = mostrado,
                    enter = fadeIn(tween(500, delayMillis = 450)) +
                        scaleIn(spring(dampingRatio = 0.55f, stiffness = 300f), initialScale = 0.6f)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 20.dp)
                    ) {
                        val linea = lineaGuardada
                        if (linea != null) {
                            BotonPergamino(
                                texto = stringResource(R.string.seguir_donde_lo_deje),
                                onClick = { alEmpezar(viewModel.cuentoId, viewModel.idioma, false, linea) },
                                ancho = 240.dp
                            )
                        }
                        BotonPergamino(
                            texto = stringResource(R.string.leer),
                            onClick = { alEmpezar(viewModel.cuentoId, viewModel.idioma, false, 0) },
                            ancho = 180.dp
                        )
                        BotonPergamino(
                            texto = stringResource(R.string.escuchar),
                            onClick = { alEmpezar(viewModel.cuentoId, viewModel.idioma, true, 0) },
                            ancho = 180.dp
                        )
                    }
                }
            }
        }
    }
}

/** Ilustración a pantalla completa con un zoom y un desplazamiento muy lentos (efecto Ken Burns) */
@Composable
private fun IlustracionConZoom(imagen: String?, quieto: Boolean) {
    val transicion = rememberInfiniteTransition(label = "zoom")
    val avance by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(16_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "avance"
    )
    val t = if (quieto) 0f else avance
    AsyncImage(
        model = imagen,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = 1.05f + 0.12f * t
                scaleY = 1.05f + 0.12f * t
                translationX = (t - 0.5f) * size.width * 0.04f
            }
    )
}

/** Estrellitas que suben despacio y titilan, con el color dorado de la palabra narrada */
@Composable
private fun Estrellitas(color: Color) {
    val estrellas = remember { List(18) { Estrella(Random.nextFloat(), Random.nextFloat(), 2f + Random.nextFloat() * 5f, Random.nextFloat()) } }
    val transicion = rememberInfiniteTransition(label = "estrellas")
    val tiempo by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(12_000, easing = LinearEasing)),
        label = "tiempo"
    )
    Canvas(Modifier.fillMaxSize()) {
        estrellas.forEach { e ->
            val y = ((e.y - tiempo + 1f) % 1f) * size.height
            val x = e.x * size.width + sin((tiempo + e.fase) * 2 * PI).toFloat() * 12f
            val brillo = 0.35f + 0.65f * ((sin((tiempo * 4 + e.fase) * 2 * PI).toFloat() + 1f) / 2f)
            drawCircle(color.copy(alpha = brillo * 0.8f), radius = e.radio * density, center = Offset(x, y))
        }
    }
}

private class Estrella(val x: Float, val y: Float, val radio: Float, val fase: Float)

@Composable
private fun BotonCorazon(favorito: Boolean, alAlternar: () -> Unit) {
    IconToggleButton(
        checked = favorito,
        onCheckedChange = { alAlternar() },
        modifier = Modifier
            .size(64.dp)
            .background(Colores.papelTranslucido, MaterialTheme.shapes.extraLarge)
    ) {
        Icon(
            imageVector = if (favorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(if (favorito) R.string.quitar_favorito else R.string.anadir_favorito),
            tint = Colores.corazon,
            modifier = Modifier.size(36.dp)
        )
    }
}

/** Color oscuro y vivo sacado de la ilustración, para teñir la portada de cada cuento */
@Composable
private fun colorDominante(imagen: String?): Color {
    val contexto = LocalContext.current
    val color by produceState(Colores.tinta, imagen) {
        if (imagen == null) return@produceState
        val resultado = SingletonImageLoader.get(contexto).execute(
            ImageRequest.Builder(contexto).data(imagen).allowHardware(false).size(160).build()
        )
        val bitmap = (resultado as? SuccessResult)?.image?.toBitmap() ?: return@produceState
        val paleta = withContext(Dispatchers.Default) { Palette.from(bitmap).generate() }
        val elegido = paleta.getDarkVibrantColor(paleta.getDarkMutedColor(paleta.getDominantColor(0)))
        if (elegido != 0) value = Color(elegido)
    }
    return color
}
