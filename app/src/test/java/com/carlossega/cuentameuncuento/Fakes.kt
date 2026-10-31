package com.carlossega.cuentameuncuento

import com.carlossega.cuentameuncuento.audio.FalloNarrador
import com.carlossega.cuentameuncuento.audio.Locucion
import com.carlossega.cuentameuncuento.audio.Narrador
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LineaCuento
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.data.precarga.PrecargaImagenes
import com.carlossega.cuentameuncuento.data.repository.CuentoRepository
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Sustituye Dispatchers.Main en los tests de ViewModel */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeCuentoRepository : CuentoRepository {
    val cuentos = mutableMapOf<Idioma, List<Cuento>>()
    val lineas = mutableMapOf<Pair<String, Idioma>, List<LineaCuento>>()
    var imagenes: Map<String, String> = emptyMap()
    var fallar = false
    var llamadasCuentos = 0

    /** Si no es null, se emite antes que la lista "del servidor", como la copia guardada */
    var guardados: List<Cuento>? = null

    override fun cuentos(idioma: Idioma): Flow<List<Cuento>> = flow {
        llamadasCuentos++
        guardados?.let { emit(it) }
        if (fallar) throw Exception("sin red")
        emit(cuentos[idioma].orEmpty())
    }

    override suspend fun getLineas(cuentoId: String, idioma: Idioma): Result<List<LineaCuento>> {
        val resultado = lineas[cuentoId to idioma]
        return if (fallar || resultado == null) Result.failure(Exception("no hay texto")) else Result.success(resultado)
    }

    override suspend fun getImagenes(cuentoId: String): Result<Map<String, String>> =
        if (fallar) Result.failure(Exception("sin red")) else Result.success(imagenes)
}

class FakePreferenciasRepository(inicial: Preferencias = Preferencias()) : PreferenciasRepository {
    private val _preferencias = MutableStateFlow(inicial)
    override val preferencias: StateFlow<Preferencias> = _preferencias
    var fallar = false

    override suspend fun guardar(preferencias: Preferencias) {
        if (fallar) throw java.io.IOException("disco lleno")
        _preferencias.value = preferencias
    }
}

class FakePrecargaImagenes : PrecargaImagenes {
    val precargadas = mutableListOf<String>()

    override fun precargar(urls: Collection<String>) {
        precargadas += urls
    }
}

/** Narrador que termina cada línea cuando el test llama a [terminarLinea] */
class FakeNarrador : Narrador {
    val locuciones = mutableListOf<Locucion>()
    val textosLeidos: List<String> get() = locuciones.map { it.texto }
    var falloSiguiente: FalloNarrador? = null
    var liberado = false
    var detenido = false
    private var alPalabra: ((Int, Int) -> Unit)? = null
    private var alTerminar: (() -> Unit)? = null

    override fun hablar(
        locucion: Locucion,
        alPalabra: (inicio: Int, fin: Int) -> Unit,
        alTerminar: () -> Unit,
        alFallar: (FalloNarrador) -> Unit
    ) {
        falloSiguiente?.let {
            falloSiguiente = null
            alFallar(it)
            return
        }
        locuciones += locucion
        this.alPalabra = alPalabra
        this.alTerminar = alTerminar
    }

    fun decirPalabra(inicio: Int, fin: Int) {
        alPalabra?.invoke(inicio, fin)
    }

    fun terminarLinea() {
        val callback = alTerminar
        alTerminar = null
        callback?.invoke()
    }

    override fun detener() {
        detenido = true
        alTerminar = null
    }

    override fun liberar() {
        liberado = true
    }
}
