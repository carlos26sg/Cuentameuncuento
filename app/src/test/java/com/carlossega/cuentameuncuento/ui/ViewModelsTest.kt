package com.carlossega.cuentameuncuento.ui

import androidx.lifecycle.SavedStateHandle
import com.carlossega.cuentameuncuento.FakeCuentoRepository
import com.carlossega.cuentameuncuento.FakeNarrador
import com.carlossega.cuentameuncuento.FakePreferenciasRepository
import com.carlossega.cuentameuncuento.MainDispatcherRule
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.audio.FalloNarrador
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LineaCuento
import com.carlossega.cuentameuncuento.data.model.Modo
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.ui.ajustes.AjustesViewModel
import com.carlossega.cuentameuncuento.ui.reproductor.ReproductorCuento
import com.carlossega.cuentameuncuento.ui.reproductor.ReproductorViewModel
import com.carlossega.cuentameuncuento.ui.seleccion.CuentosUiState
import com.carlossega.cuentameuncuento.ui.seleccion.SeleccionCuento
import com.carlossega.cuentameuncuento.ui.seleccion.SeleccionViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SeleccionViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repo = FakeCuentoRepository().apply {
        cuentos[Idioma.ESPANOL] = listOf(Cuento("c1", "El dragón", "", ""))
        cuentos[Idioma.INGLES] = listOf(Cuento("c1", "The dragon", "", ""), Cuento("c2", "The fox", "", ""))
    }

    private fun crear(idioma: Idioma = Idioma.ESPANOL) =
        SeleccionViewModel(repo, SavedStateHandle(mapOf(SeleccionCuento.EXTRA_IDIOMA to idioma.codigo)))

    @Test
    fun cargaLosCuentosDelIdiomaRecibido() {
        val vm = crear(Idioma.INGLES)

        val estado = vm.uiState.value as CuentosUiState.Listo
        assertThat(estado.cuentos.map { it.titulo }).containsExactly("The dragon", "The fox")
    }

    @Test
    fun cambiarIdioma_recargaSinDuplicar() {
        val vm = crear()

        vm.cambiarIdioma(Idioma.INGLES)
        vm.cambiarIdioma(Idioma.INGLES)
        vm.cambiarIdioma(Idioma.ESPANOL)

        assertThat((vm.uiState.value as CuentosUiState.Listo).cuentos).hasSize(1)
        assertThat(repo.llamadasGetCuentos).isEqualTo(3)
    }

    @Test
    fun sinCuentos_estadoVacio_yErrorConReintento() {
        val vm = crear(Idioma.CATALAN)
        assertThat(vm.uiState.value).isEqualTo(CuentosUiState.Vacio)

        repo.fallar = true
        vm.reintentar()
        assertThat(vm.uiState.value).isEqualTo(CuentosUiState.Error)

        repo.fallar = false
        vm.cambiarIdioma(Idioma.ESPANOL)
        assertThat(vm.uiState.value).isInstanceOf(CuentosUiState.Listo::class.java)
    }
}

class ReproductorViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val narrador = FakeNarrador()
    private val repo = FakeCuentoRepository().apply {
        lineas["c1" to Idioma.ESPANOL] = LineaCuento.desdeFichero("Uno/1\nDos\nTres/2\nFIN/")
        lineas["c1" to Idioma.INGLES] = LineaCuento.desdeFichero("One/1\nTwo\nThree/2\nTHE END/")
        imagenes = mapOf("1" to "https://img/1", "2" to "https://img/2")
    }

    private fun crear(modo: Modo = Modo.LEER, estado: Map<String, Any> = emptyMap()) = ReproductorViewModel(
        repo,
        narrador,
        SavedStateHandle(
            mapOf(
                ReproductorCuento.EXTRA_CUENTO to "c1",
                ReproductorCuento.EXTRA_IDIOMA to Idioma.ESPANOL.codigo,
                ReproductorCuento.EXTRA_MODO to modo.codigo
            ) + estado
        )
    )

    @Test
    fun avanzaYRetrocede_sinPasarDelFinal() {
        val vm = crear()
        assertThat(vm.uiState.value.lineaActual?.texto).isEqualTo("Uno")
        assertThat(vm.uiState.value.hayAnterior).isFalse()

        repeat(10) { vm.siguiente() }
        assertThat(vm.uiState.value.lineaActual?.texto).isEqualTo("FIN")
        assertThat(vm.uiState.value.haySiguiente).isFalse()

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
    fun recuperaLaLineaTrasMatarElProceso() {
        val vm = crear(estado = mapOf("indice" to 2))
        assertThat(vm.uiState.value.lineaActual?.texto).isEqualTo("Tres")
    }

    @Test
    fun narracion_leeTodasLasLineasYVuelveAlPrincipio() {
        val vm = crear(Modo.ESCUCHAR)

        vm.alternarNarracion()
        repeat(4) { narrador.terminarLinea() }

        assertThat(narrador.textosLeidos).containsExactly("Uno", "Dos", "Tres", "FIN").inOrder()
        assertThat(vm.uiState.value.narrando).isFalse()
        assertThat(vm.uiState.value.indice).isEqualTo(0)
    }

    @Test
    fun narracion_pausaDetieneLaVoz() {
        val vm = crear(Modo.ESCUCHAR)

        vm.alternarNarracion()
        vm.alternarNarracion()

        assertThat(narrador.detenido).isTrue()
        assertThat(vm.uiState.value.narrando).isFalse()
    }

    @Test
    fun narracion_avisaSiNoHayVozParaElIdioma() = runTest {
        val vm = crear(Modo.ESCUCHAR)
        narrador.falloSiguiente = FalloNarrador.IDIOMA_NO_DISPONIBLE

        vm.alternarNarracion()

        assertThat(vm.uiState.value.narrando).isFalse()
        assertThat(vm.mensajes.first()).isEqualTo(R.string.voz_idioma_no_disponible)
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

    private val cuentos = FakeCuentoRepository().apply {
        cuentos[Idioma.ESPANOL] = listOf(Cuento("c1", "El dragón", "", ""), Cuento("c2", "El zorro", "", ""))
    }

    @Test
    fun cargaLasPreferenciasGuardadas() {
        val preferencias = FakePreferenciasRepository(
            Preferencias(idioma = Idioma.ESPANOL, favorito = "c1", modoFavorito = Modo.LEER)
        )
        val vm = AjustesViewModel(preferencias, cuentos)

        assertThat(vm.uiState.value.favorito).isEqualTo("c1")
        assertThat(vm.uiState.value.modoFavorito).isEqualTo(Modo.LEER)
        assertThat(vm.uiState.value.cuentos).hasSize(2)
    }

    @Test
    fun guardaElFavoritoPorId() = runTest {
        val preferencias = FakePreferenciasRepository()
        val vm = AjustesViewModel(preferencias, cuentos)

        vm.cambiarFavorito("c2")
        vm.cambiarModo(Modo.ESCUCHAR)
        vm.guardar()

        val guardadas = preferencias.preferencias.value
        assertThat(guardadas.favorito).isEqualTo("c2")
        assertThat(guardadas.modoFavorito).isEqualTo(Modo.ESCUCHAR)
        assertThat(guardadas.tieneFavorito).isTrue()
        assertThat(vm.mensajes.first()).isEqualTo(R.string.ajustes_guardados)
    }

    @Test
    fun errorAlGuardar_muestraMensaje() = runTest {
        val preferencias = FakePreferenciasRepository().apply { fallar = true }
        val vm = AjustesViewModel(preferencias, cuentos)

        vm.guardar()

        assertThat(vm.mensajes.first()).isEqualTo(R.string.error_guardar)
    }
}
