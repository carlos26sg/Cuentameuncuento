package com.carlossega.cuentameuncuento.data

import com.carlossega.cuentameuncuento.FakeCuentoRepository
import com.carlossega.cuentameuncuento.FakePrecargaImagenes
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.lineasDePrueba
import com.carlossega.cuentameuncuento.data.precarga.PrecargaCuentos
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PrecargaCuentosTest {

    private val precarga = FakePrecargaImagenes()
    private val repo = FakeCuentoRepository().apply {
        cuentos[Idioma.ESPANOL] = listOf(Cuento("c1", "El dragón", "", "https://portada/c1"))
        lineas["c1" to Idioma.ESPANOL] = lineasDePrueba("Uno/2\nDos/3\nFIN/")
        imagenes = mapOf("2" to "https://img/2", "3" to "https://img/3")
    }

    @Test
    fun precargaPortadasYPrimeraIlustracion() = runTest {
        PrecargaCuentos(repo, precarga).precargar(Idioma.ESPANOL)

        assertThat(precarga.precargadas).containsExactly("https://portada/c1", "https://img/2")
    }

    @Test
    fun sinConexion_noFallaNiPrecarga() = runTest {
        repo.fallar = true

        PrecargaCuentos(repo, precarga).precargar(Idioma.ESPANOL)

        assertThat(precarga.precargadas).isEmpty()
    }
}
