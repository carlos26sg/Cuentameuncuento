package com.carlossega.cuentameuncuento.ui.ajustes

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.carlossega.cuentameuncuento.data.model.TamanoTexto
import com.carlossega.cuentameuncuento.data.model.VelocidadVoz
import com.carlossega.cuentameuncuento.ui.comun.BotonRedondo
import com.carlossega.cuentameuncuento.ui.comun.LocalMusica
import com.carlossega.cuentameuncuento.ui.comun.endonimo
import com.carlossega.cuentameuncuento.ui.tema.Colores
import com.carlossega.cuentameuncuento.ui.tema.familia

/**
 * Ajustes "para mayores", en un panel que sube desde abajo sobre la biblioteca.
 * Cada cambio se guarda al momento.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesHoja(alCerrar: () -> Unit, viewModel: AjustesViewModel = hiltViewModel()) {
    val preferencias by viewModel.preferencias.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val urlPrivacidad = stringResource(R.string.url_privacidad)
    val actuales = preferencias

    ModalBottomSheet(
        onDismissRequest = alCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Colores.fondo,
        contentColor = Colores.tinta
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 22.dp, end = 22.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.para_mayores), style = MaterialTheme.typography.bodyMedium, color = Colores.tintaSuave)
                    Text(
                        stringResource(R.string.ajustes),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.semantics { heading() }
                    )
                }
                BotonRedondo(Icons.Rounded.Close, stringResource(R.string.cerrar), alCerrar)
            }
            if (actuales == null) return@Column

            Seccion(stringResource(R.string.idioma_cuentos)) {
                Segmentado(Idioma.entries, actuales.idioma, { it.endonimo }, viewModel::cambiarIdioma)
            }
            Seccion(stringResource(R.string.velocidad_voz)) {
                Segmentado(VelocidadVoz.entries, actuales.velocidadVoz, { stringResource(it.nombre) }, viewModel::cambiarVelocidad)
            }
            Seccion(stringResource(R.string.letra_cuento)) {
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LetraCuento.entries.forEach { letra ->
                        val familia = remember(letra) { letra.familia() }
                        val elegida = actuales.letra == letra
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Colores.superficie,
                            border = BorderStroke(2.dp, if (elegida) Colores.tinta else Colores.borde),
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = elegida, role = Role.RadioButton) { viewModel.cambiarLetra(letra) }
                        ) {
                            // La muestra arriba y el nombre debajo: en una fila no cabían en móviles estrechos
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                Text(stringResource(R.string.ejemplo_letra), fontFamily = familia, fontSize = 20.sp)
                                Text(
                                    if (letra == LetraCuento.ANDIKA) stringResource(R.string.letra_recomendada, letra.nombre) else letra.nombre,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Colores.tintaSuave
                                )
                            }
                        }
                    }
                }
            }
            Seccion(stringResource(R.string.tamano_texto)) {
                Segmentado(TamanoTexto.entries, actuales.tamanoTexto, { stringResource(it.nombre) }, viewModel::cambiarTamanoTexto)
            }
            LocalMusica.current?.let { musica ->
                val activa by musica.activa.collectAsState()
                Surface(shape = RoundedCornerShape(16.dp), color = Colores.superficie) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(stringResource(R.string.musica_fondo), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Switch(
                            checked = activa,
                            onCheckedChange = { musica.alternar() },
                            colors = SwitchDefaults.colors(checkedTrackColor = Colores.coral, checkedThumbColor = Colores.superficie)
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {
                    try {
                        contexto.startActivity(Intent(Intent.ACTION_VIEW, urlPrivacidad.toUri()))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(contexto, R.string.sin_navegador, Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(stringResource(R.string.privacidad), style = MaterialTheme.typography.bodyMedium, color = Colores.coral)
                }
                Text(
                    stringResource(R.string.version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Colores.tintaSuave,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        contenido()
    }
}

/** Opciones en una fila, la elegida en blanco sobre un carril crema */
@Composable
private fun <T> Segmentado(opciones: List<T>, elegida: T, etiqueta: @Composable (T) -> String, alElegir: (T) -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = Colores.cremaOscuro) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .padding(5.dp)
                .selectableGroup()
        ) {
            opciones.forEach { opcion ->
                val activa = opcion == elegida
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (activa) Colores.superficie else Colores.cremaOscuro,
                    shadowElevation = if (activa) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .selectable(selected = activa, role = Role.RadioButton) { alElegir(opcion) }
                ) {
                    Text(
                        etiqueta(opcion),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (activa) Colores.tinta else Colores.tintaSuave,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

private val VelocidadVoz.nombre: Int
    get() = when (this) {
        VelocidadVoz.LENTA -> R.string.velocidad_lenta
        VelocidadVoz.NORMAL -> R.string.velocidad_normal
        VelocidadVoz.RAPIDA -> R.string.velocidad_rapida
    }

private val TamanoTexto.nombre: Int
    get() = when (this) {
        TamanoTexto.NORMAL -> R.string.tamano_normal
        TamanoTexto.GRANDE -> R.string.tamano_grande
        TamanoTexto.MUY_GRANDE -> R.string.tamano_muy_grande
    }
