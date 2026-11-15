package com.carlossega.cuentameuncuento.ui

import androidx.lifecycle.SavedStateHandle
import com.carlossega.cuentameuncuento.FakeCuentoRepository
import com.carlossega.cuentameuncuento.FakeNarrador
import com.carlossega.cuentameuncuento.FakePrecargaImagenes
import com.carlossega.cuentameuncuento.FakePreferenciasRepository
import com.carlossega.cuentameuncuento.MainDispatcherRule
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.audio.FalloNarrador
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.Lectura
import com.carlossega.cuentameuncuento.data.model.LetraCuento
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.data.model.TamanoTexto
import com.carlossega.cuentameuncuento.data.model.VelocidadVoz
import com.carlossega.cuentameuncuento.data.precarga.PrecargaCuentos
import com.carlossega.cuentameuncuento.lineasDePrueba
import com.carlossega.cuentameuncuento.ui.ajustes.AjustesViewModel
import com.carlossega.cuentameuncuento.ui.lector.LectorViewModel
import com.carlossega.cuentameuncuento.ui.biblioteca.BibliotecaUiState
import com.carlossega.cuentameuncuento.ui.biblioteca.BibliotecaViewModel
import com.carlossega.cuentameuncuento.ui.ficha.FichaViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class BibliotecaViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = FakeCuentoRepository().apply {
        cuentos[Idioma.ESPANOL] = listOf(Cuento("c1", "El dragón", "", ""), Cuento("c2", "El zorro", "", ""))
        cuentos[Idioma.INGLES] = listOf(Cuento("c1", "The dragon", "", ""))
    }
    private val preferencias = FakePreferenciasRepository()

    private fun crear(preferencias: FakePreferenciasRepository = this.preferencias) =
        BibliotecaViewModel(repo, preferencias, PrecargaCuentos(repo, FakePrecargaImagenes())).also { vm ->
            // stateIn(WhileSubscribed) solo trabaja si alguien lo escucha
            kotlinx.coroutines.CoroutineScope(mainRule.dispatcher).launch { vm.uiState.collect {} }
            kotlinx.coroutines.CoroutineScope(mainRule.dispatcher).launch { vm.seguirLeyendo.collect {} }
        }

    @Test
    fun cargaLosCuentosDelIdiomaGuardado_yLoCambiaEnAjustes() {
        val vm = crear()
        assertThat((vm.uiState.value as BibliotecaUiState.Lista).cuentos).hasSize(2)

        vm.cambiarIdioma(Idioma.INGLES)

        val estado = vm.uiState.value as BibliotecaUiState.Lista
        assertThat(estado.idioma).isEqualTo(Idioma.INGLES)
        assertThat(estado.cuentos.map { it.cuento.titulo }).containsExactly("The dragon")
        assertThat(preferencias.preferencias.value.idioma).isEqualTo(Idioma.INGLES)
    }

    @Test
    fun losFavoritosSalenPrimero_peroNoSaltanAlMarcarlos() {
        val vm = crear()

        vm.alternarFavorito("c2")

        // Se marca sin moverse, para que la portada no desaparezca de la vista
        val ahora = (vm.uiState.value as BibliotecaUiState.Lista).cuentos
        assertThat(ahora.map { it.cuento.id }).containsExactly("c1", "c2").inOrder()
        assertThat(ahora.last().favorito).isTrue()

        // La próxima vez que se abre la biblioteca, el favorito sale primero
        val otraVez = crear()
        assertThat((otraVez.uiState.value as BibliotecaUiState.Lista).cuentos.map { it.cuento.id })
            .containsExactly("c2", "c1").inOrder()
    }

    @Test
    fun sinCuentos_vacia_yErrorConReintento() {
        val vm = crear()
        vm.cambiarIdioma(Idioma.CATALAN)
        assertThat(vm.uiState.value).isInstanceOf(BibliotecaUiState.Vacia::class.java)

        repo.fallar = true
        vm.reintentar()
        assertThat(vm.uiState.value).isInstanceOf(BibliotecaUiState.Error::class.java)

        repo.fallar = false
        vm.cambiarIdioma(Idioma.ESPANOL)
        assertThat(vm.uiState.value).isInstanceOf(BibliotecaUiState.Lista::class.java)
    }

    @Test
    fun ofreceSeguirElCuentoAMedias() {
        val preferencias = FakePreferenciasRepository(lecturaInicial = Lectura("c1", Idioma.ESPANOL, 5))
        val vm = crear(preferencias)

        assertThat(vm.seguirLeyendo.value?.cuento?.titulo).isEqualTo("El dragón")
        assertThat(vm.seguirLeyendo.value?.lectura?.linea).isEqualTo(5)

        preferencias.lectura.value = null
        assertThat(vm.seguirLeyendo.value).isNull()
    }
}

class FichaViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = FakeCuentoRepository().apply {
        cuentos[Idioma.ESPANOL] = listOf(Cuento("c1", "El dragón", "Un dragón simpático", "portada", edad = 3, paginas = 6, minutos = 2))
        lineas["c1" to Idioma.ESPANOL] = lineasDePrueba("Uno/2\nDos/5\nTres/7\nCuatro/9\nCinco/11\nFIN/")
        imagenes = mapOf("2" to "img2", "5" to "img5", "7" to "img7", "9" to "img9", "11" to "img11")
    }

    private fun crear(preferencias: FakePreferenciasRepository) = FichaViewModel(
        repo,
        preferencias,
        SavedStateHandle(mapOf("cuento" to "c1", "idioma" to "esp"))
    ).also { vm ->
        kotlinx.coroutines.CoroutineScope(mainRule.dispatcher).launch { vm.favorito.collect {} }
        kotlinx.coroutines.CoroutineScope(mainRule.dispatcher).launch { vm.lineaGuardada.collect {} }
    }

    @Test
    fun muestraElCuento_fondoYUnVistazoRepartido() {
        val vm = crear(FakePreferenciasRepository())

        assertThat(vm.uiState.value.cuento?.titulo).isEqualTo("El dragón")
        assertThat(vm.uiState.value.cuento?.edad).isEqualTo(3)
        assertThat(vm.uiState.value.fondo).isEqualTo("img2")
        // Tres ilustraciones repartidas, sin la del fondo
        assertThat(vm.uiState.value.vistazo).containsExactly("img5", "img7", "img9").inOrder()
    }

    @Test
    fun favoritoYLineaGuardadaDeEsteCuento() {
        val preferencias = FakePreferenciasRepository(lecturaInicial = Lectura("c1", Idioma.ESPANOL, 3))
        val vm = crear(preferencias)

        assertThat(vm.lineaGuardada.value).isEqualTo(3)
        assertThat(vm.favorito.value).isFalse()
        vm.alternarFavorito()
        assertThat(vm.favorito.value).isTrue()

        // Otro cuento a medias no cuenta
        preferencias.lectura.value = Lectura("c2", Idioma.ESPANOL, 3)
        assertThat(vm.lineaGuardada.value).isNull()
    }
}

class LectorViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val narrador = FakeNarrador()
    private val precarga = FakePrecargaImagenes()
    private var preferencias = FakePreferenciasRepository()
    private val repo = FakeCuentoRepository().apply {
        cuentos[Idioma.ESPANOL] = listOf(Cuento("c1", "El dragón", "", ""))
        lineas["c1" to Idioma.ESPANOL] = lineasDePrueba("Uno/1\nDos\nTres/2\nFIN/")
        lineas["c1" to Idioma.INGLES] = lineasDePrueba("One/1\nTwo\nThree/2\nTHE END/")
        imagenes = mapOf("1" to "https://img/1", "2" to "https://img/2")
    }

    private fun crear(escuchar: Boolean = false, linea: Int = -1, estado: Map<String, Any> = emptyMap()) = LectorViewModel(
        repo,
        preferencias,
        precarga,
        narrador,
        SavedStateHandle(mapOf("cuento" to "c1", "idioma" to "esp", "escuchar" to escuchar, "linea" to linea) + estado)
    )

    @Test
    fun avanzaYRetrocede_sinPasarDelFinal() {
        val vm = crear()
        assertThat(vm.uiState.value.titulo).isEqualTo("El dragón")
        assertThat(vm.uiState.value.lineaActual?.texto).isEqualTo("Uno")
        assertThat(vm.uiState.value.hayAnterior).isFalse()

        repeat(10) { vm.siguiente() }
        assertThat(vm.uiState.value.lineaActual?.texto).isEqualTo("FIN")
        assertThat(vm.uiState.value.terminado).isTrue()

        vm.anterior()
        assertThat(vm.uiState.value.lineaActual?.texto).isEqualTo("Tres")
    }

    @Test
    fun laIlustracionSeMantieneHastaLaSiguiente() {
        val vm = crear()
        assertThat(vm.uiState.value.imagenActual).isEqualTo("https://img/1")
        vm.siguiente()
        assertThat(vm.uiState.value.imagenActual).isEqualTo("https://img/1")
        vm.siguiente()
        assertThat(vm.uiState.value.imagenActual).isEqualTo("https://img/2")
    }

    @Test
    fun traduccion_vaAlineadaConLaLineaActual() {
        val vm = crear()
        vm.siguiente()

        vm.seleccionarTraduccion(Idioma.INGLES)
        assertThat(vm.uiState.value.textoTraducido).isEqualTo("Two")

        vm.seleccionarTraduccion(null)
        assertThat(vm.uiState.value.textoTraducido).isEmpty()
    }

    @Test
    fun recuperaLaLineaTrasMatarElProceso_oEmpiezaPorLaGuardada() {
        assertThat(crear(estado = mapOf("indice" to 2)).uiState.value.lineaActual?.texto).isEqualTo("Tres")
        assertThat(crear(linea = 1).uiState.value.lineaActual?.texto).isEqualTo("Dos")
    }

    @Test
    fun guardaPorDondeVa_yLoBorraAlTerminar() {
        val vm = crear()

        vm.siguiente()
        assertThat(preferencias.lectura.value).isEqualTo(Lectura("c1", Idioma.ESPANOL, 1))

        repeat(5) { vm.siguiente() }
        assertThat(preferencias.lectura.value).isNull()
    }

    @Test
    fun escuchar_empiezaANarrarSolo() {
        crear(escuchar = true)

        assertThat(narrador.textosLeidos).containsExactly("Uno")
    }

    @Test
    fun narracion_leeTodasLasLineasYSeQuedaEnElFinal() {
        val vm = crear()

        vm.alternarNarracion()
        repeat(4) { narrador.terminarLinea() }

        assertThat(narrador.textosLeidos).containsExactly("Uno", "Dos", "Tres", "FIN").inOrder()
        assertThat(vm.uiState.value.narrando).isFalse()
        assertThat(vm.uiState.value.terminado).isTrue()

        // ▶ desde el final vuelve a contarlo entero
        vm.alternarNarracion()
        assertThat(vm.uiState.value.indice).isEqualTo(0)
    }

    @Test
    fun empezarDeNuevo_vuelveAlPrincipioYCallaLaVoz() {
        val vm = crear()
        repeat(5) { vm.siguiente() }
        vm.alternarNarracion()

        vm.empezarDeNuevo()

        assertThat(vm.uiState.value.indice).isEqualTo(0)
        assertThat(vm.uiState.value.narrando).isFalse()
    }

    @Test
    fun narracion_pausaDetieneLaVoz() {
        val vm = crear()

        vm.alternarNarracion()
        vm.alternarNarracion()

        assertThat(narrador.detenido).isTrue()
        assertThat(vm.uiState.value.narrando).isFalse()
    }

    @Test
    fun narracion_ofreceInstalarLaVozSiFalta() = runTest {
        val vm = crear()
        narrador.falloSiguiente = FalloNarrador.IDIOMA_NO_DISPONIBLE

        vm.alternarNarracion()

        assertThat(vm.uiState.value.narrando).isFalse()
        assertThat(vm.faltaVoz.first()).isEqualTo(Idioma.ESPANOL)
    }

    @Test
    fun narracion_otrosFallosMuestranMensaje() = runTest {
        val vm = crear()
        narrador.falloSiguiente = FalloNarrador.MOTOR_NO_DISPONIBLE

        vm.alternarNarracion()

        assertThat(vm.mensajes.first()).isEqualTo(R.string.voz_no_disponible)
    }

    @Test
    fun narracion_usaLaVelocidadGuardada() {
        preferencias = FakePreferenciasRepository(Preferencias(velocidadVoz = VelocidadVoz.LENTA))
        val vm = crear()

        vm.alternarNarracion()

        assertThat(narrador.locuciones.single().velocidad).isEqualTo(VelocidadVoz.LENTA)
    }

    @Test
    fun narracion_pausasEntreFrases_masLargasAlCambiarDeIlustracion() {
        val vm = crear()

        vm.alternarNarracion()
        repeat(3) { narrador.terminarLinea() }

        // Uno (empieza al momento), Dos (misma ilustración), Tres (ilustración nueva), FIN
        val pausas = narrador.locuciones.map { it.pausaPreviaMs }
        assertThat(pausas[0]).isEqualTo(0)
        assertThat(pausas[2]).isGreaterThan(pausas[1])
        assertThat(pausas[1]).isGreaterThan(0)
    }

    @Test
    fun narracion_resaltaLaPalabraYLaBorraAlCambiarDeLinea() {
        val vm = crear()
        vm.alternarNarracion()

        narrador.decirPalabra(0, 3)
        assertThat(vm.uiState.value.palabra).isEqualTo(0 until 3)

        narrador.terminarLinea()
        assertThat(vm.uiState.value.palabra).isNull()
    }

    @Test
    fun precargaTodasLasIlustracionesDelCuento() {
        crear()

        assertThat(precarga.precargadas).containsExactly("https://img/1", "https://img/2")
    }

    @Test
    fun botonAa_pasaAlSiguienteTamanoYVuelveAlPrimero() {
        val vm = crear()

        vm.cambiarTamanoTexto(TamanoTexto.NORMAL)
        assertThat(preferencias.preferencias.value.tamanoTexto).isEqualTo(TamanoTexto.GRANDE)

        vm.cambiarTamanoTexto(TamanoTexto.MUY_GRANDE)
        assertThat(preferencias.preferencias.value.tamanoTexto).isEqualTo(TamanoTexto.NORMAL)
    }

    @Test
    fun errorDeCarga_yReintento() {
        repo.fallar = true
        val vm = crear()
        assertThat(vm.uiState.value.error).isTrue()

        repo.fallar = false
        vm.reintentar()
        assertThat(vm.uiState.value.error).isFalse()
        assertThat(vm.uiState.value.lineas).hasSize(4)
    }
}

class AjustesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun cadaCambioSeGuardaAlMomento() {
        val preferencias = FakePreferenciasRepository()
        val vm = AjustesViewModel(preferencias)

        vm.cambiarIdioma(Idioma.CATALAN)
        vm.cambiarVelocidad(VelocidadVoz.RAPIDA)
        vm.cambiarLetra(LetraCuento.ATKINSON)
        vm.cambiarTamanoTexto(TamanoTexto.MUY_GRANDE)

        assertThat(preferencias.preferencias.value).isEqualTo(
            Preferencias(
                idioma = Idioma.CATALAN,
                velocidadVoz = VelocidadVoz.RAPIDA,
                letra = LetraCuento.ATKINSON,
                tamanoTexto = TamanoTexto.MUY_GRANDE
            )
        )
    }
}
