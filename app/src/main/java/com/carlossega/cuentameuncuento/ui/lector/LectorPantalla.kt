package com.carlossega.cuentameuncuento.ui.lector

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.ui.comun.BotonDibujo
import com.carlossega.cuentameuncuento.ui.comun.BotonMusica
import com.carlossega.cuentameuncuento.ui.comun.BotonPergamino
import com.carlossega.cuentameuncuento.ui.comun.PanelPapel
import com.carlossega.cuentameuncuento.ui.comun.SelectorIdioma
import com.carlossega.cuentameuncuento.ui.comun.margenSeguro
import com.carlossega.cuentameuncuento.ui.comun.sombraTexto
import com.carlossega.cuentameuncuento.ui.tema.Colores
import com.carlossega.cuentameuncuento.ui.tema.estiloCuento
import kotlin.math.abs

@Composable
fun LectorPantalla(
    alCerrar: () -> Unit,
    alElegirOtro: () -> Unit,
    viewModel: LectorViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var faltaVoz by rememberSaveable { mutableStateOf(false) }

    // Mientras se lee, la pantalla no se apaga
    val vista = LocalView.current
    DisposableEffect(Unit) {
        vista.keepScreenOn = true
        onDispose { vista.keepScreenOn = false }
    }
    // Al salir de la app la narración se para (Android no deja sonar audio en segundo plano)
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.pausar() }

    LaunchedEffect(Unit) {
        viewModel.mensajes.collect { Toast.makeText(contexto, it, Toast.LENGTH_LONG).show() }
    }
    LaunchedEffect(Unit) {
        viewModel.faltaVoz.collect { faltaVoz = true }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Colores.papel)
    ) {
        when {
            estado.cargando -> CircularProgressIndicator(color = Colores.tinta, modifier = Modifier.align(Alignment.Center))
            estado.error -> PanelPapel(Modifier.align(Alignment.Center)) {
                Text(stringResource(R.string.error_cargar_cuento), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                BotonPergamino(stringResource(R.string.reintentar), viewModel::reintentar)
            }
            maxHeight > maxWidth -> LectorVertical(estado, viewModel, alCerrar, alElegirOtro)
            else -> LectorHorizontal(estado, viewModel, alCerrar, alElegirOtro)
        }
    }

    if (faltaVoz) {
        AlertDialog(
            onDismissRequest = { faltaVoz = false },
            title = { Text(stringResource(R.string.falta_voz)) },
            text = { Text(stringResource(R.string.voz_idioma_no_disponible)) },
            confirmButton = {
                TextButton(onClick = {
                    faltaVoz = false
                    try {
                        contexto.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(contexto, R.string.sin_instalador_voz, Toast.LENGTH_LONG).show()
                    }
                }) { Text(stringResource(R.string.instalar)) }
            },
            dismissButton = { TextButton(onClick = { faltaVoz = false }) { Text(stringResource(R.string.ahora_no)) } },
            containerColor = Colores.papel
        )
    }
}

/** Horizontal: la ilustración ocupa toda la pantalla y el texto va abajo, sobre un degradado */
@Composable
private fun LectorHorizontal(
    estado: LectorUiState,
    viewModel: LectorViewModel,
    alCerrar: () -> Unit,
    alElegirOtro: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Ilustracion(estado.imagenActual, ContentScale.Crop, viewModel, Modifier.fillMaxSize())
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x99000000), Color(0xCC000000))))
        )
        Column(
            Modifier
                .fillMaxSize()
                .margenSeguro()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            BarraSuperior(estado, viewModel, alCerrar)
            Spacer(Modifier.weight(1f))
            if (estado.idiomaTraduccion != null) {
                Text(
                    text = estado.textoTraducido,
                    style = estiloCuento(18).copy(shadow = sombraTexto),
                    color = Colores.traduccion,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotonDibujo(R.drawable.back, stringResource(R.string.anterior), viewModel::anterior, tamano = 64.dp, activo = estado.hayAnterior)
                BotonNarrar(estado.narrando, viewModel::alternarNarracion, tamano = 64)
                TextoCuento(
                    estado = estado,
                    viewModel = viewModel,
                    colorTexto = Color.White,
                    colorPalabra = Colores.palabraSobreIlustracion,
                    fondoPalabra = Color.Transparent,
                    conSombra = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                )
                BotonDibujo(R.drawable.next, stringResource(R.string.siguiente), viewModel::siguiente, tamano = 64.dp, activo = estado.haySiguiente)
            }
            Progreso(estado, Modifier.padding(top = 8.dp))
        }
        FinDelCuento(estado, viewModel, alElegirOtro, Modifier.align(Alignment.Center))
    }
}

/** Vertical: la ilustración arriba y el texto debajo, sobre papel */
@Composable
private fun LectorVertical(
    estado: LectorUiState,
    viewModel: LectorViewModel,
    alCerrar: () -> Unit,
    alElegirOtro: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .margenSeguro()
    ) {
        Box(Modifier.padding(horizontal = 12.dp)) { BarraSuperior(estado, viewModel, alCerrar) }
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Ilustracion(estado.imagenActual, ContentScale.Fit, viewModel, Modifier.fillMaxSize())
            FinDelCuento(estado, viewModel, alElegirOtro, Modifier.align(Alignment.Center))
        }
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                TextoCuento(
                    estado = estado,
                    viewModel = viewModel,
                    colorTexto = Colores.tinta,
                    colorPalabra = Colores.tinta,
                    fondoPalabra = Colores.palabraSobrePapel,
                    conSombra = false,
                    modifier = Modifier.fillMaxWidth()
                )
                if (estado.idiomaTraduccion != null) {
                    Text(
                        text = estado.textoTraducido,
                        style = estiloCuento(18),
                        color = Colores.tintaSuave,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                BotonDibujo(R.drawable.back, stringResource(R.string.anterior), viewModel::anterior, tamano = 72.dp, activo = estado.hayAnterior)
                BotonNarrar(estado.narrando, viewModel::alternarNarracion, tamano = 80)
                BotonDibujo(R.drawable.next, stringResource(R.string.siguiente), viewModel::siguiente, tamano = 72.dp, activo = estado.haySiguiente)
            }
            Progreso(estado, Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun BarraSuperior(estado: LectorUiState, viewModel: LectorViewModel, alCerrar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        BotonDibujo(R.drawable.equis, stringResource(R.string.cerrar_cuento), alCerrar)
        BotonMusica(Modifier.padding(start = 8.dp))
        Spacer(Modifier.weight(1f))
        SelectorIdioma(
            seleccionado = estado.idiomaTraduccion,
            descripcion = stringResource(R.string.traduccion),
            onSeleccion = viewModel::seleccionarTraduccion,
            conNinguno = true
        )
    }
}

/**
 * Ilustración de la línea actual. Funde con la anterior al cambiar (también si ya estaba en
 * memoria) y deja pasar página tocando los lados o deslizando.
 */
@Composable
private fun Ilustracion(url: String?, escala: ContentScale, viewModel: LectorViewModel, modifier: Modifier) {
    val umbral = with(LocalDensity.current) { 60.dp.toPx() }
    Crossfade(
        targetState = url,
        animationSpec = tween(450),
        label = "ilustracion",
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { posicion ->
                    when {
                        posicion.x < size.width / 3f -> viewModel.anterior()
                        posicion.x > size.width * 2f / 3f -> viewModel.siguiente()
                    }
                }
            }
            .pointerInput(Unit) {
                var arrastre = 0f
                detectHorizontalDragGestures(
                    onDragStart = { arrastre = 0f },
                    onDragEnd = {
                        if (abs(arrastre) > umbral) {
                            if (arrastre < 0) viewModel.siguiente() else viewModel.anterior()
                        }
                    }
                ) { _, delta -> arrastre += delta }
            }
    ) { actual ->
        AsyncImage(
            model = actual,
            contentDescription = null,
            contentScale = escala,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Texto de la línea actual, con la palabra que suena resaltada. Se marca con su idioma para
 * que TalkBack lo pronuncie bien, y TalkBack lo anuncia al pasar de página (pero no mientras
 * narra la app, para no hablar los dos a la vez).
 */
@Composable
private fun TextoCuento(
    estado: LectorUiState,
    viewModel: LectorViewModel,
    colorTexto: Color,
    colorPalabra: Color,
    fondoPalabra: Color,
    conSombra: Boolean,
    modifier: Modifier
) {
    val texto = estado.lineaActual?.texto.orEmpty()
    val idioma = viewModel.idioma
    val anotado = remember(texto, estado.palabra, idioma) {
        textoAnotado(texto, idioma, estado.palabra, colorPalabra, fondoPalabra)
    }
    val siguiente = stringResource(R.string.siguiente)
    val anterior = stringResource(R.string.anterior)
    val progreso = stringResource(R.string.progreso, estado.indice + 1, estado.lineas.size)
    val accionSiguiente by rememberUpdatedState(viewModel::siguiente)
    val accionAnterior by rememberUpdatedState(viewModel::anterior)

    Text(
        text = anotado,
        style = estiloCuento(if (conSombra) 26 else 28).let { if (conSombra) it.copy(shadow = sombraTexto) else it },
        color = colorTexto,
        textAlign = TextAlign.Center,
        modifier = modifier.semantics {
            stateDescription = progreso
            if (!estado.narrando) liveRegion = LiveRegionMode.Polite
            customActions = listOf(
                CustomAccessibilityAction(siguiente) { accionSiguiente(); true },
                CustomAccessibilityAction(anterior) { accionAnterior(); true }
            )
        }
    )
}

private fun textoAnotado(
    texto: String,
    idioma: Idioma,
    palabra: IntRange?,
    colorPalabra: Color,
    fondoPalabra: Color
): AnnotatedString = buildAnnotatedString {
    append(texto)
    addStyle(SpanStyle(localeList = LocaleList(idioma.etiquetaLocale)), 0, texto.length)
    if (palabra != null && palabra.first >= 0 && palabra.last < texto.length) {
        addStyle(SpanStyle(color = colorPalabra, background = fondoPalabra), palabra.first, palabra.last + 1)
    }
}

@Composable
private fun BotonNarrar(narrando: Boolean, alPulsar: () -> Unit, tamano: Int) {
    BotonDibujo(
        dibujo = if (narrando) R.drawable.pause else R.drawable.play,
        descripcion = stringResource(if (narrando) R.string.pausar else R.string.escuchar),
        onClick = alPulsar,
        tamano = tamano.dp
    )
}

/** Barra de progreso: el niño ve cuánto queda del cuento */
@Composable
private fun Progreso(estado: LectorUiState, modifier: Modifier) {
    if (estado.lineas.isEmpty()) return
    LinearProgressIndicator(
        progress = { (estado.indice + 1f) / estado.lineas.size },
        color = Colores.palabraSobreIlustracion,
        trackColor = Colores.papelOscuro.copy(alpha = 0.6f),
        drawStopIndicator = {},
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
    )
}

/** Al llegar a "FIN": empezar de nuevo o elegir otro cuento */
@Composable
private fun FinDelCuento(estado: LectorUiState, viewModel: LectorViewModel, alElegirOtro: () -> Unit, modifier: Modifier) {
    AnimatedVisibility(
        visible = estado.terminado && !estado.narrando,
        enter = fadeIn(tween(600, delayMillis = 400)),
        exit = fadeOut(),
        modifier = modifier
    ) {
        PanelPapel(Modifier.widthIn(max = 520.dp), color = Colores.papelTranslucido) {
            Text(
                text = estado.titulo,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                BotonPergamino(stringResource(R.string.empezar_de_nuevo), viewModel::empezarDeNuevo, ancho = 200.dp)
                BotonPergamino(stringResource(R.string.otro_cuento), alElegirOtro, ancho = 200.dp)
            }
        }
    }
}
