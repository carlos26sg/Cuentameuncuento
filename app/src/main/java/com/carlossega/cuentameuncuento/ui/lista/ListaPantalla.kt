package com.carlossega.cuentameuncuento.ui.lista

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.ui.comun.BotonDibujo
import com.carlossega.cuentameuncuento.ui.comun.BotonPergamino
import com.carlossega.cuentameuncuento.ui.comun.FondoIlustrado
import com.carlossega.cuentameuncuento.ui.comun.PanelPapel
import com.carlossega.cuentameuncuento.ui.comun.SelectorIdioma
import com.carlossega.cuentameuncuento.ui.comun.TituloPantalla
import com.carlossega.cuentameuncuento.ui.comun.margenSeguro
import com.carlossega.cuentameuncuento.ui.tema.Colores
import com.carlossega.cuentameuncuento.ui.tema.LocalLetraCuento

@Composable
fun ListaPantalla(
    alVolver: () -> Unit,
    alElegir: (cuentoId: String, idioma: Idioma) -> Unit,
    viewModel: ListaViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val idioma = when (val e = estado) {
        is ListaUiState.Lista -> e.idioma
        is ListaUiState.Vacia -> e.idioma
        is ListaUiState.Error -> e.idioma
        ListaUiState.Cargando -> null
    }

    FondoIlustrado(R.drawable.fondo_menu) {
        Column(
            Modifier
                .fillMaxSize()
                .margenSeguro()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotonDibujo(R.drawable.atras, stringResource(R.string.atras), alVolver)
                TituloPantalla(
                    texto = stringResource(R.string.selecciona),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )
                if (idioma != null) {
                    SelectorIdioma(
                        seleccionado = idioma,
                        descripcion = stringResource(R.string.idioma_cuentos),
                        onSeleccion = { it?.let(viewModel::cambiarIdioma) }
                    )
                }
            }

            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // En móviles estrechos, portada algo menor para que quepa el título entero
                val portada = if (maxWidth < 600.dp) 96.dp else 120.dp
                when (val e = estado) {
                    ListaUiState.Cargando -> CircularProgressIndicator(color = Colores.tinta)
                    is ListaUiState.Vacia -> Mensaje(stringResource(R.string.sin_cuentos))
                    is ListaUiState.Error -> Mensaje(stringResource(R.string.error_cargar_cuentos)) {
                        BotonPergamino(stringResource(R.string.reintentar), viewModel::reintentar)
                    }
                    is ListaUiState.Lista -> LazyVerticalGrid(
                        // Una columna en un móvil en vertical, dos o más en horizontal y en tablets
                        columns = GridCells.Adaptive(minSize = 360.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(e.cuentos, key = { it.cuento.id }) { item ->
                            TarjetaCuento(
                                item = item,
                                portada = portada,
                                alPulsar = { alElegir(item.cuento.id, e.idioma) },
                                alAlternarFavorito = { viewModel.alternarFavorito(item.cuento.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Tarjeta de papel con la portada grande: muchos niños eligen el cuento por el dibujo */
@Composable
private fun TarjetaCuento(
    item: CuentoEnLista,
    portada: Dp,
    alPulsar: () -> Unit,
    alAlternarFavorito: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaccion = remember { MutableInteractionSource() }
    val pulsada by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(if (pulsada) 0.96f else 1f, label = "pulsar")

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Colores.papel,
        border = BorderStroke(2.dp, Colores.borde),
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .scale(escala)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(interactionSource = interaccion, indication = null, role = Role.Button, onClick = alPulsar)
                .padding(12.dp)
        ) {
            AsyncImage(
                model = item.cuento.imagen,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(portada)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Colores.papelOscuro)
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = item.cuento.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = Colores.tinta,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.cuento.descripcion,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = LocalLetraCuento.current),
                    color = Colores.tintaSuave,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            BotonFavorito(item.favorito, alAlternarFavorito)
        }
    }
}

@Composable
private fun BotonFavorito(favorito: Boolean, alAlternar: () -> Unit) {
    val escala by animateFloatAsState(if (favorito) 1.15f else 1f, label = "corazon")
    IconToggleButton(
        checked = favorito,
        onCheckedChange = { alAlternar() },
        modifier = Modifier.size(56.dp)
    ) {
        Icon(
            imageVector = if (favorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(if (favorito) R.string.quitar_favorito else R.string.anadir_favorito),
            tint = Colores.corazon,
            modifier = Modifier
                .size(32.dp)
                .scale(escala)
        )
    }
}

@Composable
private fun Mensaje(texto: String, accion: (@Composable () -> Unit)? = null) {
    PanelPapel(Modifier.widthIn(max = 420.dp)) {
        Text(
            text = texto,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        if (accion != null) {
            Spacer(Modifier.height(16.dp))
            Box(Modifier.align(Alignment.CenterHorizontally)) { accion() }
        }
    }
}
