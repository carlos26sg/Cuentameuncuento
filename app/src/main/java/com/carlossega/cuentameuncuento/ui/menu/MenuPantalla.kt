package com.carlossega.cuentameuncuento.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Lectura
import com.carlossega.cuentameuncuento.ui.comun.BotonMusica
import com.carlossega.cuentameuncuento.ui.comun.BotonPergamino
import com.carlossega.cuentameuncuento.ui.comun.FondoIlustrado
import com.carlossega.cuentameuncuento.ui.comun.TituloPantalla
import com.carlossega.cuentameuncuento.ui.comun.margenSeguro

/**
 * Menú principal: un único botón para ir a los cuentos y, si hay uno a medias, otro para
 * seguir leyéndolo. Ya no hay "Salir": en Android se sale con el gesto de volver.
 */
@Composable
fun MenuPantalla(
    alAbrirCuentos: () -> Unit,
    alSeguirLeyendo: (Lectura) -> Unit,
    alAbrirAjustes: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val seguir by viewModel.seguirLeyendo.collectAsStateWithLifecycle()

    FondoIlustrado(R.drawable.fondo_menu) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .margenSeguro()
                .padding(16.dp)
        ) {
            val vertical = maxHeight > maxWidth
            TituloPantalla(
                texto = stringResource(R.string.bienvenido),
                modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 8.dp)
            )
            BotonPergamino(
                texto = stringResource(R.string.ajustes),
                onClick = alAbrirAjustes,
                ancho = 160.dp,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            // En vertical los botones van centrados; en horizontal, a la izquierda como en el dibujo
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = if (vertical) Alignment.CenterHorizontally else Alignment.Start,
                modifier = Modifier
                    .align(if (vertical) Alignment.Center else Alignment.CenterStart)
                    .padding(start = if (vertical) 0.dp else 48.dp)
            ) {
                BotonPergamino(
                    texto = stringResource(R.string.cuentos),
                    onClick = alAbrirCuentos,
                    ancho = 260.dp
                )
                AnimatedVisibility(
                    visible = seguir != null,
                    enter = fadeIn() + slideInHorizontally { -it / 3 }
                ) {
                    seguir?.let {
                        BotonPergamino(
                            texto = stringResource(R.string.seguir_leyendo),
                            descripcion = stringResource(R.string.seguir_con, it.titulo),
                            onClick = { alSeguirLeyendo(it.lectura) },
                            ancho = 260.dp
                        )
                    }
                }
            }

            Box(Modifier.align(Alignment.BottomStart)) {
                BotonMusica()
            }
        }
    }
}
