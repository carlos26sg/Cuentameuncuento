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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.ui.comun.BotonPrincipal
import com.carlossega.cuentameuncuento.ui.comun.BotonRedondo
import com.carlossega.cuentameuncuento.ui.comun.BotonSecundario
import com.carlossega.cuentameuncuento.ui.comun.BotonTexto
import com.carlossega.cuentameuncuento.ui.comun.MenuIdiomas
import com.carlossega.cuentameuncuento.ui.comun.endonimo
import com.carlossega.cuentameuncuento.ui.comun.margenSeguro
import com.carlossega.cuentameuncuento.ui.comun.sigla
import com.carlossega.cuentameuncuento.ui.tema.Colores
import com.carlossega.cuentameuncuento.ui.tema.LocalTamanoTexto
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
            .background(Colores.fondo)
    ) {
        when {
            estado.cargando -> CircularProgressIndicator(color = Colores.coral, modifier = Modifier.align(Alignment.Center))
            estado.error -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(stringResource(R.string.error_cargar_cuento), style = MaterialTheme.typography.titleMedium)
                BotonPrincipal(stringResource(R.string.reintentar), Icons.Rounded.Refresh, viewModel::reintentar)
            }
            maxHeight > maxWidth -> LectorVertical(estado, viewModel, alCerrar, alElegirOtro)
            else -> LectorHorizontal(estado, viewModel, alCerrar, alElegirOtro)
        }
    }

    if (faltaVoz) {
        AlertDialog(
            onDismissRequest = { faltaVoz = false },
            title = { Text(stringResource(R.string.falta_voz), style = MaterialTheme.typography.titleLarge) },
            text = { Text(stringResource(R.string.voz_idioma_no_disponible), style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = {
                    faltaVoz = false
                    try {
                        contexto.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(contexto, R.string.sin_instalador_voz, Toast.LENGTH_LONG).show()
                    }
                }) { Text(stringResource(R.string.instalar), style = MaterialTheme.typography.labelMedium, color = Colores.coral) }
            },
            dismissButton = {
                TextButton(onClick = { faltaVoz = false }) {
                    Text(stringResource(R.string.ahora_no), style = MaterialTheme.typography.labelMedium, color = Colores.tinta)
                }
            },
            containerColor = Colores.fondo
        )
    }
}

/** Vertical: barra arriba, ilustración enmarcada, texto grande y los botones abajo */
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
        BarraSuperior(estado, viewModel, alCerrar, conTitulo = true, sobreIlustracion = false)
        Box(
            Modifier
                .fillMaxWidth()
                .weight(0.9f)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Colores.cremaOscuro)
        ) {
            Ilustracion(estado.imagenActual, viewModel, Modifier.fillMaxSize())
            FinDelCuento(estado, viewModel, alElegirOtro, Modifier.align(Alignment.Center))
        }
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 12.dp)
        ) {
            TextoCuento(estado, viewModel, Colores.tinta, tamano = 27, modifier = Modifier.fillMaxWidth())
            Traduccion(estado, Colores.tintaSuave, Modifier.padding(top = 12.dp))
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 20.dp)
        ) {
            Progreso(estado, conNumero = true)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                BotonPagina(adelante = false, estado.hayAnterior, viewModel::anterior, 64.dp)
                BotonNarrar(estado.narrando, viewModel::alternarNarracion, 84.dp)
                BotonPagina(adelante = true, estado.haySiguiente, viewModel::siguiente, 64.dp)
            }
        }
    }
}

/** Horizontal: la ilustración ocupa la pantalla y el texto va en una cápsula, como un subtítulo */
@Composable
private fun LectorHorizontal(
    estado: LectorUiState,
    viewModel: LectorViewModel,
    alCerrar: () -> Unit,
    alElegirOtro: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1A2E))
    ) {
        Ilustracion(estado.imagenActual, viewModel, Modifier.fillMaxSize())
        Box(
            Modifier
                .fillMaxSize()
                .margenSeguro()
        ) {
            BarraSuperior(estado, viewModel, alCerrar, conTitulo = false, sobreIlustracion = true)
            BotonPagina(
                adelante = false, estado.hayAnterior, viewModel::anterior, 52.dp,
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp),
                translucido = true
            )
            BotonPagina(
                adelante = true, estado.haySiguiente, viewModel::siguiente, 52.dp,
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                translucido = true
            )
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Colores.capsula,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 22.dp, start = 84.dp, end = 84.dp)
                    .widthIn(max = 720.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 12.dp, end = 22.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    BotonNarrar(estado.narrando, viewModel::alternarNarracion, 60.dp)
                    Column(Modifier.padding(start = 16.dp)) {
                        TextoCuento(estado, viewModel, Color.White, tamano = 22, maxLineas = 3)
                        Traduccion(estado, Color.White.copy(alpha = 0.8f), Modifier.padding(top = 6.dp), alineacion = TextAlign.Start)
                    }
                }
            }
            FinDelCuento(estado, viewModel, alElegirOtro, Modifier.align(Alignment.Center))
        }
        Progreso(
            estado, conNumero = false,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun BarraSuperior(
    estado: LectorUiState,
    viewModel: LectorViewModel,
    alCerrar: () -> Unit,
    conTitulo: Boolean,
    sobreIlustracion: Boolean
) {
    var menuTraduccion by rememberSaveable { mutableStateOf(false) }
    val tamano = LocalTamanoTexto.current
    val fondo = if (sobreIlustracion) Colores.superficie.copy(alpha = 0.92f) else Colores.superficie
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        BotonRedondo(Icons.Rounded.Close, stringResource(R.string.cerrar_cuento), alCerrar, fondo = fondo)
        Text(
            text = if (conTitulo) estado.titulo else "",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        Box {
            val actual = estado.idiomaTraduccion
            BotonRedondo(
                Icons.Rounded.Translate,
                stringResource(R.string.traduccion) + ": " + (actual?.endonimo ?: stringResource(R.string.sin_traduccion)),
                { menuTraduccion = true },
                fondo = if (actual != null) Colores.resaltado else fondo
            )
            MenuIdiomas(
                abierto = menuTraduccion,
                alCerrar = { menuTraduccion = false },
                seleccionado = actual,
                opciones = Idioma.entries.filter { it != viewModel.idioma },
                alElegir = {
                    menuTraduccion = false
                    viewModel.seleccionarTraduccion(it)
                },
                conNinguno = true
            )
        }
        BotonTexto("Aa", stringResource(R.string.tamano_texto), { viewModel.cambiarTamanoTexto(tamano) })
    }
}

/**
 * Ilustración de la línea actual: nítida y entera, sobre una copia desenfocada de sí misma
 * que rellena el hueco (así las imágenes pequeñas no se ven borrosas al estirarlas).
 * Funde con la anterior al cambiar y deja pasar página tocando los lados o deslizando.
 */
@Composable
private fun Ilustracion(url: String?, viewModel: LectorViewModel, modifier: Modifier) {
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
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = actual,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(24.dp)
                    .alpha(0.75f)
            )
            AsyncImage(
                model = actual,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
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
    color: Color,
    tamano: Int,
    modifier: Modifier = Modifier,
    maxLineas: Int = Int.MAX_VALUE
) {
    val texto = estado.lineaActual?.texto.orEmpty()
    val idioma = viewModel.idioma
    val anotado = remember(texto, estado.palabra, idioma) { textoAnotado(texto, idioma, estado.palabra) }
    val siguiente = stringResource(R.string.siguiente)
    val anterior = stringResource(R.string.anterior)
    val progreso = stringResource(R.string.progreso, estado.indice + 1, estado.lineas.size)
    val accionSiguiente by rememberUpdatedState(viewModel::siguiente)
    val accionAnterior by rememberUpdatedState(viewModel::anterior)

    Text(
        text = anotado,
        style = estiloCuento(tamano),
        color = color,
        textAlign = if (maxLineas == Int.MAX_VALUE) TextAlign.Center else TextAlign.Start,
        maxLines = maxLineas,
        overflow = TextOverflow.Ellipsis,
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

private fun textoAnotado(texto: String, idioma: Idioma, palabra: IntRange?): AnnotatedString = buildAnnotatedString {
    append(texto)
    addStyle(SpanStyle(localeList = LocaleList(idioma.etiquetaLocale)), 0, texto.length)
    if (palabra != null && palabra.first >= 0 && palabra.last < texto.length) {
        addStyle(SpanStyle(color = Colores.tinta, background = Colores.resaltado), palabra.first, palabra.last + 1)
    }
}

/** La misma línea en el idioma de la traducción, con su sigla delante */
@Composable
private fun Traduccion(estado: LectorUiState, color: Color, modifier: Modifier, alineacion: TextAlign = TextAlign.Center) {
    val idioma = estado.idiomaTraduccion ?: return
    val texto = buildAnnotatedString {
        append(idioma.sigla + "  ")
        addStyle(SpanStyle(color = color.copy(alpha = 0.7f)), 0, idioma.sigla.length)
        val inicio = length
        append(estado.textoTraducido)
        addStyle(SpanStyle(localeList = LocaleList(idioma.etiquetaLocale)), inicio, length)
    }
    Text(
        texto,
        style = estiloCuento(16),
        color = color,
        textAlign = alineacion,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun BotonNarrar(narrando: Boolean, alPulsar: () -> Unit, tamano: Dp) {
    BotonRedondo(
        icono = if (narrando) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
        descripcion = stringResource(if (narrando) R.string.pausar else R.string.escuchar),
        onClick = alPulsar,
        tamano = tamano,
        tamanoIcono = tamano * 0.45f,
        fondo = Colores.coral,
        colorIcono = Color.White,
        sombra = 6.dp
    )
}

@Composable
private fun BotonPagina(
    adelante: Boolean,
    activo: Boolean,
    alPulsar: () -> Unit,
    tamano: Dp,
    modifier: Modifier = Modifier,
    translucido: Boolean = false
) {
    BotonRedondo(
        icono = if (adelante) Icons.AutoMirrored.Rounded.ArrowForward else Icons.AutoMirrored.Rounded.ArrowBack,
        descripcion = stringResource(if (adelante) R.string.siguiente else R.string.anterior),
        onClick = alPulsar,
        tamano = tamano,
        tamanoIcono = tamano * 0.42f,
        fondo = if (translucido) Colores.superficie.copy(alpha = 0.78f) else Colores.superficie,
        borde = if (translucido) null else BorderStroke(2.dp, if (activo) Colores.tinta else Colores.borde),
        activo = activo,
        sombra = 0.dp,
        modifier = modifier
    )
}

/** Barra de progreso: el niño ve cuánto queda del cuento */
@Composable
private fun Progreso(estado: LectorUiState, conNumero: Boolean, modifier: Modifier = Modifier) {
    if (estado.lineas.isEmpty()) return
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        LinearProgressIndicator(
            progress = { (estado.indice + 1f) / estado.lineas.size },
            color = Colores.coral,
            trackColor = if (conNumero) Colores.cremaOscuro else Color.White.copy(alpha = 0.3f),
            drawStopIndicator = {},
            gapSize = 0.dp,
            modifier = Modifier
                .weight(1f)
                .height(if (conNumero) 6.dp else 5.dp)
                .clip(RoundedCornerShape(3.dp))
        )
        if (conNumero) {
            Spacer(Modifier.width(10.dp))
            Text(
                "${estado.indice + 1} / ${estado.lineas.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = Colores.tintaSuave
            )
        }
    }
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
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Colores.fondo,
            shadowElevation = 12.dp,
            modifier = Modifier
                .padding(20.dp)
                .widthIn(max = 460.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    estado.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = Colores.tinta
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BotonSecundario(stringResource(R.string.empezar_de_nuevo), Icons.Rounded.Refresh, viewModel::empezarDeNuevo)
                    BotonPrincipal(stringResource(R.string.otro_cuento), Icons.AutoMirrored.Rounded.ArrowForward, alElegirOtro)
                }
            }
        }
    }
}
