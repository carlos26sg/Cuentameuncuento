package com.carlossega.cuentameuncuento.ui.ficha

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.ui.comun.BotonCorazon
import com.carlossega.cuentameuncuento.ui.comun.BotonPrincipal
import com.carlossega.cuentameuncuento.ui.comun.BotonRedondo
import com.carlossega.cuentameuncuento.ui.comun.BotonSecundario
import com.carlossega.cuentameuncuento.ui.comun.Etiqueta
import com.carlossega.cuentameuncuento.ui.comun.margenSeguro
import com.carlossega.cuentameuncuento.ui.comun.sigla
import com.carlossega.cuentameuncuento.ui.tema.Colores
import com.carlossega.cuentameuncuento.ui.tema.movimientoReducido

/**
 * Ficha de cada cuento: la cabecera se tiñe con su propia ilustración desenfocada, la
 * portada se ve nítida como un libro, y debajo los datos, la descripción y un vistazo a sus
 * ilustraciones. "Leer" y "Escuchar" quedan siempre a mano abajo.
 */
@Composable
fun FichaPantalla(
    alVolver: () -> Unit,
    alEmpezar: (cuentoId: String, idioma: Idioma, escuchar: Boolean, linea: Int) -> Unit,
    viewModel: FichaViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val favorito by viewModel.favorito.collectAsStateWithLifecycle()
    val lineaGuardada by viewModel.lineaGuardada.collectAsStateWithLifecycle()
    val cuento = estado.cuento
    val quieto = movimientoReducido()

    // La entrada se anima una sola vez, no al girar la pantalla
    var mostrada by rememberSaveable { mutableStateOf(quieto) }
    LaunchedEffect(cuento) { if (cuento != null) mostrada = true }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Colores.fondo)
    ) {
        val horizontal = maxWidth > maxHeight

        // Cabecera teñida: la ilustración del cuento, muy desenfocada
        Box(
            Modifier
                .fillMaxWidth()
                .height(if (horizontal) maxHeight else 380.dp)
        ) {
            AsyncImage(
                model = estado.fondo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(28.dp)
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(0.45f to Colores.fondo.copy(alpha = 0.15f), 1f to Colores.fondo))
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .margenSeguro()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                BotonRedondo(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.atras), alVolver)
                Spacer(Modifier.weight(1f))
                BotonCorazon(favorito, viewModel::alternarFavorito)
            }

            if (cuento != null) {
                AnimatedVisibility(
                    visible = mostrada,
                    enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 12 },
                    modifier = Modifier.weight(1f)
                ) {
                    if (horizontal) {
                        // Horizontal: portada a la izquierda, datos a la derecha
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp)
                        ) {
                            Portada(cuento.imagen, Modifier.align(Alignment.CenterVertically))
                            Column(
                                Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Datos(estado, horizontal = true)
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp)
                        ) {
                            Portada(cuento.imagen)
                            Spacer(Modifier.height(18.dp))
                            Datos(estado, horizontal = false)
                        }
                    }
                }

                Acciones(
                    lineaGuardada = lineaGuardada,
                    alLeer = { linea -> alEmpezar(viewModel.cuentoId, viewModel.idioma, false, linea) },
                    alEscuchar = { alEmpezar(viewModel.cuentoId, viewModel.idioma, true, 0) }
                )
            }
        }
    }
}

/** La portada nítida, con sombra, como un libro */
@Composable
private fun Portada(url: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(196.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(Colores.cremaOscuro)
    )
}

@Composable
private fun ColumnScope.Datos(estado: FichaUiState, horizontal: Boolean) {
    val cuento = estado.cuento ?: return
    val alineacion = if (horizontal) Alignment.Start else Alignment.CenterHorizontally
    Text(
        cuento.titulo,
        style = MaterialTheme.typography.displaySmall,
        textAlign = if (horizontal) TextAlign.Start else TextAlign.Center,
        modifier = Modifier
            .align(alineacion)
            .semantics { heading() }
    )
    Spacer(Modifier.height(10.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, if (horizontal) Alignment.Start else Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        cuento.edad?.let { Etiqueta(stringResource(R.string.edad, it)) }
        Etiqueta(stringResource(R.string.minutos, cuento.minutos))
        Etiqueta(pluralStringResource(R.plurals.paginas, cuento.paginas, cuento.paginas))
        Etiqueta(Idioma.entries.joinToString(" · ") { it.sigla })
    }
    Spacer(Modifier.height(12.dp))
    Text(
        cuento.descripcion,
        style = MaterialTheme.typography.bodyLarge,
        color = Colores.tintaSuave,
        textAlign = if (horizontal) TextAlign.Start else TextAlign.Center,
        modifier = Modifier
            .widthIn(max = 560.dp)
            .align(alineacion)
    )
    if (estado.vistazo.isNotEmpty()) {
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.un_vistazo),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .semantics { heading() }
        )
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            estado.vistazo.forEach { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(150.dp)
                        .height(104.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Colores.cremaOscuro)
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
}

/** "Leer" y "Escuchar" siempre a mano, y "Seguir" si el cuento está a medias */
@Composable
private fun Acciones(lineaGuardada: Int?, alLeer: (Int) -> Unit, alEscuchar: () -> Unit) {
    Surface(color = Colores.fondo, shadowElevation = 8.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (lineaGuardada != null) {
                TextButton(onClick = { alLeer(lineaGuardada) }) {
                    Text(
                        stringResource(R.string.seguir_por_pagina, lineaGuardada + 1),
                        style = MaterialTheme.typography.labelMedium,
                        color = Colores.coral
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.widthIn(max = 560.dp)
            ) {
                BotonPrincipal(
                    stringResource(R.string.leer),
                    Icons.AutoMirrored.Rounded.MenuBook,
                    { alLeer(0) },
                    Modifier.weight(1f)
                )
                BotonSecundario(
                    stringResource(R.string.escuchar),
                    Icons.Rounded.Headphones,
                    alEscuchar,
                    Modifier.weight(1f)
                )
            }
        }
    }
}
