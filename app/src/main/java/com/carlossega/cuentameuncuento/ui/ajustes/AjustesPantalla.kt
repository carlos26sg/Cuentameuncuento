package com.carlossega.cuentameuncuento.ui.ajustes

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carlossega.cuentameuncuento.BuildConfig
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LetraCuento
import com.carlossega.cuentameuncuento.data.model.VelocidadVoz
import com.carlossega.cuentameuncuento.ui.comun.BotonDibujo
import com.carlossega.cuentameuncuento.ui.comun.BotonPergamino
import com.carlossega.cuentameuncuento.ui.comun.FondoIlustrado
import com.carlossega.cuentameuncuento.ui.comun.LocalMusica
import com.carlossega.cuentameuncuento.ui.comun.PanelPapel
import com.carlossega.cuentameuncuento.ui.comun.TituloPantalla
import com.carlossega.cuentameuncuento.ui.comun.margenSeguro
import com.carlossega.cuentameuncuento.ui.comun.nombre
import com.carlossega.cuentameuncuento.ui.tema.Colores
import com.carlossega.cuentameuncuento.ui.tema.familia

@Composable
fun AjustesPantalla(alVolver: () -> Unit, viewModel: AjustesViewModel = hiltViewModel()) {
    val preferencias by viewModel.preferencias.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val urlPrivacidad = stringResource(R.string.url_privacidad)

    FondoIlustrado(R.drawable.fondo_menu) {
        Column(
            Modifier
                .fillMaxSize()
                .margenSeguro()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotonDibujo(R.drawable.atras, stringResource(R.string.atras), alVolver)
                TituloPantalla(stringResource(R.string.ajustes), Modifier.padding(start = 12.dp))
            }
            val actuales = preferencias ?: return@Column
            PanelPapel(
                Modifier
                    .padding(top = 8.dp)
                    .widthIn(max = 900.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Seccion(stringResource(R.string.idioma_cuentos)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.selectableGroup()
                        ) {
                            Idioma.entries.forEach { idioma ->
                                Opcion(seleccionada = actuales.idioma == idioma, alElegir = { viewModel.cambiarIdioma(idioma) }) {
                                    Image(painterResource(idioma.bandera), contentDescription = null, modifier = Modifier.size(48.dp, 36.dp))
                                    Text(stringResource(idioma.nombre), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }

                    Seccion(stringResource(R.string.velocidad_voz)) {
                        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                            VelocidadVoz.entries.forEachIndexed { i, velocidad ->
                                SegmentedButton(
                                    selected = actuales.velocidadVoz == velocidad,
                                    onClick = { viewModel.cambiarVelocidad(velocidad) },
                                    shape = SegmentedButtonDefaults.itemShape(i, VelocidadVoz.entries.size),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = Colores.papelOscuro,
                                        activeContentColor = Colores.tinta,
                                        inactiveContainerColor = Colores.papel,
                                        inactiveContentColor = Colores.tinta,
                                        activeBorderColor = Colores.borde,
                                        inactiveBorderColor = Colores.borde
                                    )
                                ) {
                                    Text(stringResource(velocidad.nombre), style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }

                    Seccion(stringResource(R.string.letra_cuento)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.selectableGroup()
                        ) {
                            LetraCuento.entries.forEach { letra ->
                                val familia = remember(letra) { letra.familia() }
                                Opcion(seleccionada = actuales.letra == letra, alElegir = { viewModel.cambiarLetra(letra) }) {
                                    Column {
                                        Text(stringResource(R.string.ejemplo_letra), fontFamily = familia, fontSize = 22.sp)
                                        Text(letra.nombre, style = MaterialTheme.typography.bodyMedium, color = Colores.tintaSuave)
                                    }
                                }
                            }
                        }
                    }

                    LocalMusica.current?.let { musica ->
                        val activa by musica.activa.collectAsState()
                        Seccion(stringResource(R.string.musica)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(if (activa) R.string.silenciar_musica else R.string.activar_musica),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = activa,
                                    onCheckedChange = { musica.alternar() },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = Colores.tinta,
                                        checkedThumbColor = Colores.papel
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        BotonPergamino(
                            texto = stringResource(R.string.privacidad),
                            onClick = {
                                try {
                                    contexto.startActivity(Intent(Intent.ACTION_VIEW, urlPrivacidad.toUri()))
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(contexto, R.string.sin_navegador, Toast.LENGTH_SHORT).show()
                                }
                            },
                            ancho = 180.dp
                        )
                        Text(
                            text = stringResource(R.string.version, BuildConfig.VERSION_NAME),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Colores.tintaSuave,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.padding(vertical = 10.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .semantics { heading() }
        )
        contenido()
    }
}

/** Opción que se elige tocándola; la elegida lleva un borde grueso */
@Composable
private fun Opcion(seleccionada: Boolean, alElegir: () -> Unit, contenido: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (seleccionada) Colores.papelOscuro else Colores.papel,
        border = BorderStroke(if (seleccionada) 3.dp else 1.dp, if (seleccionada) Colores.tinta else Colores.borde),
        modifier = Modifier.selectable(selected = seleccionada, onClick = alElegir, role = Role.RadioButton)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            contenido()
        }
    }
}

private val VelocidadVoz.nombre: Int
    get() = when (this) {
        VelocidadVoz.LENTA -> R.string.velocidad_lenta
        VelocidadVoz.NORMAL -> R.string.velocidad_normal
        VelocidadVoz.RAPIDA -> R.string.velocidad_rapida
    }
