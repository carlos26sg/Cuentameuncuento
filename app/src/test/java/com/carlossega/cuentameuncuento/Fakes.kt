package com.carlossega.cuentameuncuento

import com.carlossega.cuentameuncuento.audio.FalloNarrador
import com.carlossega.cuentameuncuento.audio.Narrador
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LineaCuento
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.data.repository.CuentoRepository
import com.carlossega.cuentameuncuento.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    var llamadasGetCuentos = 0

    override suspend fun getCuentos(idioma: Idioma): Result<List<Cuento>> {
        llamadasGetCuentos++
        return if (fallar) Result.failure(Exception("sin red")) else Result.success(cuentos[idioma].orEmpty())
    }

    override suspend fun getLineas(cuentoId: String, idioma: Idioma): Result<List<LineaCuento>> {
        val resultado = lineas[cuentoId to idioma]
        return if (fallar || resultado == null) Result.failure(Exception("no hay texto")) else Result.success(resultado)
    }

    override suspend fun getImagenes(cuentoId: String): Result<Map<String, String>> =
        if (fallar) Result.failure(Exception("sin red")) else Result.success(imagenes)
}

class FakeUserRepository(inicial: Preferencias = Preferencias()) : UserRepository {
    private val _preferencias = MutableStateFlow(inicial)
    override val preferencias: StateFlow<Preferencias> = _preferencias
    var fallar = false
    var datosBorrados = false

    override suspend fun asegurarSesion() = !fallar

    override suspend fun sincronizar(): Result<Unit> = Result.success(Unit)

    override suspend fun guardar(preferencias: Preferencias): Result<Unit> {
        if (fallar) return Result.failure(Exception("error"))
        _preferencias.value = preferencias
        return Result.success(Unit)
    }

    override suspend fun borrarDatos(): Result<Unit> {
        if (fallar) return Result.failure(Exception("error"))
        datosBorrados = true
        _preferencias.value = Preferencias()
        return Result.success(Unit)
    }
}

/** Narrador que termina cada línea cuando el test llama a [terminarLinea] */
class FakeNarrador : Narrador {
    val textosLeidos = mutableListOf<String>()
    var falloSiguiente: FalloNarrador? = null
    var liberado = false
    var detenido = false
    private var alTerminar: (() -> Unit)? = null

    override fun hablar(
        texto: String,
        idioma: Idioma,
        alTerminar: () -> Unit,
        alFallar: (FalloNarrador) -> Unit
    ) {
        falloSiguiente?.let {
            falloSiguiente = null
            alFallar(it)
            return
        }
        textosLeidos += texto
        this.alTerminar = alTerminar
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
