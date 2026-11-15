package com.carlossega.cuentameuncuento.ui.biblioteca

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.Lectura
import com.carlossega.cuentameuncuento.ui.ajustes.AjustesHoja
import com.carlossega.cuentameuncuento.ui.comun.BotonCorazon
import com.carlossega.cuentameuncuento.ui.comun.BotonPrincipal
import com.carlossega.cuentameuncuento.ui.comun.BotonRedondo
import com.carlossega.cuentameuncuento.ui.comun.BotonTexto
import com.carlossega.cuentameuncuento.ui.comun.FondoPaisaje
import com.carlossega.cuentameuncuento.ui.comun.MenuIdiomas
import com.carlossega.cuentameuncuento.ui.comun.endonimo
import com.carlossega.cuentameuncuento.ui.comun.margenSeguro
import com.carlossega.cuentameuncuento.ui.comun.sigla
import com.carlossega.cuentameuncuento.ui.tema.Colores

/**
 * Pantalla de inicio: la biblioteca. Sin menú intermedio, se ven directamente los cuentos;
 * arriba, el paisaje y el cuento a medias.
 */
@Composable
fun BibliotecaPantalla(
    alElegir: (cuentoId: String, idioma: Idioma) -> Unit,
    alSeguirLeyendo: (Lectura) -> Unit,
    viewModel: BibliotecaViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val seguir by viewModel.seguirLeyendo.collectAsStateWithLifecycle()
    var ajustesAbiertos by rememberSaveable { mutableStateOf(false) }
    val idioma = when (val e = estado) {
        is BibliotecaUiState.Lista -> e.idioma
        is BibliotecaUiState.Vacia -> e.idioma
        is BibliotecaUiState.Error -> e.idioma
        BibliotecaUiState.Cargando -> null
    }

    LazyVerticalGrid(
        // Dos columnas en un móvil en vertical; más en horizontal y en tablets
        columns = GridCells.Adaptive(minSize = 156.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.fondo)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }, key = "cabecera") {
            Cabecera(
                idioma = idioma,
                seguir = seguir,
                alCambiarIdioma = viewModel::cambiarIdioma,
                alAbrirAjustes = { ajustesAbiertos = true },
                alSeguirLeyendo = alSeguirLeyendo
            )
        }
        when (val e = estado) {
            BibliotecaUiState.Cargando -> item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Colores.coral)
                }
            }
            is BibliotecaUiState.Vacia -> item(span = { GridItemSpan(maxLineSpan) }) {
                Mensaje(stringResource(R.string.sin_cuentos))
            }
            is BibliotecaUiState.Error -> item(span = { GridItemSpan(maxLineSpan) }) {
                Mensaje(stringResource(R.string.error_cargar_cuentos)) {
                    BotonPrincipal(stringResource(R.string.reintentar), Icons.Rounded.Refresh, viewModel::reintentar)
                }
            }
            is BibliotecaUiState.Lista -> {
                item(span = { GridItemSpan(maxLineSpan) }, key = "titulo") {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            stringResource(R.string.todos_los_cuentos),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .weight(1f)
                                .semantics { heading() }
                        )
                        Text(
                            pluralStringResource(R.plurals.n_cuentos, e.cuentos.size, e.cuentos.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Colores.tintaSuave
                        )
                    }
                }
                items(e.cuentos, key = { it.cuento.id }) { item ->
                    TarjetaCuento(
                        item = item,
                        alPulsar = { alElegir(item.cuento.id, e.idioma) },
                        alAlternarFavorito = { viewModel.alternarFavorito(item.cuento.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (ajustesAbiertos) {
        AjustesHoja(alCerrar = { ajustesAbiertos = false })
    }
}

@Composable
private fun Cabecera(
    idioma: Idioma?,
    seguir: SeguirLeyendo?,
    alCambiarIdioma: (Idioma) -> Unit,
    alAbrirAjustes: () -> Unit,
    alSeguirLeyendo: (Lectura) -> Unit
) {
    var menuIdiomas by rememberSaveable { mutableStateOf(false) }
    // El paisaje llega hasta los bordes (la cuadrícula tiene margen); el contenido respeta el notch
    Box(Modifier.sangrado(20.dp)) {
        FondoPaisaje(
            Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
        Column(
            Modifier
                .fillMaxWidth()
                .margenSeguro()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.app_name_dos_lineas),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                if (idioma != null) {
                    Box {
                        BotonTexto(
                            texto = idioma.sigla,
                            descripcion = stringResource(R.string.idioma_cuentos) + ": " + idioma.endonimo,
                            onClick = { menuIdiomas = true }
                        )
                        MenuIdiomas(
                            abierto = menuIdiomas,
                            alCerrar = { menuIdiomas = false },
                            seleccionado = idioma,
                            opciones = Idioma.entries,
                            alElegir = { elegido ->
                                menuIdiomas = false
                                elegido?.let(alCambiarIdioma)
                            }
                        )
                    }
                }
                BotonRedondo(Icons.Rounded.Settings, stringResource(R.string.ajustes), alAbrirAjustes)
            }
            Spacer(Modifier.height(if (seguir != null) 70.dp else 150.dp))
            if (seguir != null) {
                TarjetaSeguir(seguir, alSeguirLeyendo)
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

/** "Seguir leyendo": el cuento a medias, con su progreso */
@Composable
private fun TarjetaSeguir(seguir: SeguirLeyendo, alSeguirLeyendo: (Lectura) -> Unit) {
    val cuento = seguir.cuento
    val pagina = seguir.lectura.linea + 1
    val progreso = stringResource(R.string.progreso, pagina, cuento.paginas)
    val descripcion = stringResource(R.string.seguir_con, cuento.titulo) + ". " + progreso
    Surface(
        onClick = { alSeguirLeyendo(seguir.lectura) },
        shape = RoundedCornerShape(22.dp),
        color = Colores.superficie,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = cuento.imagen,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
                    .semantics { this.contentDescription = descripcion }
            ) {
                Text(stringResource(R.string.seguir_leyendo), style = MaterialTheme.typography.bodySmall, color = Colores.tintaSuave)
                Text(cuento.titulo, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { pagina.toFloat() / cuento.paginas.coerceAtLeast(1) },
                    color = Colores.coral,
                    trackColor = Colores.cremaOscuro,
                    drawStopIndicator = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                Spacer(Modifier.height(4.dp))
                Text(progreso, style = MaterialTheme.typography.bodySmall, color = Colores.tintaSuave)
            }
            Surface(shape = RoundedCornerShape(26.dp), color = Colores.coral, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
            }
        }
    }
}

/** Portada grande con el corazón encima, y debajo el título, la edad y los minutos */
@Composable
private fun TarjetaCuento(
    item: CuentoEnLista,
    alPulsar: () -> Unit,
    alAlternarFavorito: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cuento = item.cuento
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = alPulsar)
    ) {
        Box {
            AsyncImage(
                model = cuento.imagen,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(6.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Colores.cremaOscuro)
            )
            BotonCorazon(
                favorito = item.favorito,
                alAlternar = alAlternarFavorito,
                tamano = 40.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
        Text(cuento.titulo, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(datosCuento(cuento), style = MaterialTheme.typography.bodySmall, color = Colores.tintaSuave)
    }
}

/** Ensancha el elemento [margen] por cada lado, para salirse del margen de la cuadrícula */
private fun Modifier.sangrado(margen: Dp): Modifier = layout { medible, restricciones ->
    val extra = margen.roundToPx()
    val ancho = restricciones.maxWidth + extra * 2
    val colocable = medible.measure(restricciones.copy(minWidth = ancho, maxWidth = ancho))
    layout(restricciones.maxWidth, colocable.height) { colocable.place(-extra, 0) }
}

@Composable
fun datosCuento(cuento: Cuento): String {
    val minutos = stringResource(R.string.minutos, cuento.minutos)
    return cuento.edad?.let { stringResource(R.string.edad, it) + " · " + minutos } ?: minutos
}

@Composable
private fun Mensaje(texto: String, accion: (@Composable () -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Text(texto, style = MaterialTheme.typography.titleMedium)
        accion?.invoke()
    }
}
