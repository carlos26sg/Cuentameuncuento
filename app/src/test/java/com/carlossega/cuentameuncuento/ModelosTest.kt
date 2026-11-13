package com.carlossega.cuentameuncuento

import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LineaCuento
import com.carlossega.cuentameuncuento.data.model.Modo
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests unitarios para modelos de datos
 */
class ModelosTest {

    @Test
    fun lineaCuento_detectaFinalEnTodosLosIdiomas() {
        assertThat(LineaCuento("FIN", null).esFinal).isTrue()
        assertThat(LineaCuento("FI", null).esFinal).isTrue()
        assertThat(LineaCuento(" The End ", null).esFinal).isTrue()
        assertThat(LineaCuento("Fin de la primera parte", null).esFinal).isFalse()
    }

    @Test
    fun idioma_desdeCodigo_usaEspanolSiNoLoConoce() {
        assertThat(Idioma.desdeCodigo("cat")).isEqualTo(Idioma.CATALAN)
        assertThat(Idioma.desdeCodigo("eng")).isEqualTo(Idioma.INGLES)
        assertThat(Idioma.desdeCodigo("xx")).isEqualTo(Idioma.ESPANOL)
        assertThat(Idioma.desdeCodigo(null)).isEqualTo(Idioma.ESPANOL)
    }

    @Test
    fun modo_desdeCodigo_mantieneLosValoresGuardados() {
        assertThat(Modo.desdeCodigo("leer")).isEqualTo(Modo.LEER)
        assertThat(Modo.desdeCodigo("reproducir")).isEqualTo(Modo.ESCUCHAR)
        assertThat(Modo.desdeCodigo("")).isNull()
    }

    @Test
    fun preferencias_tieneFavorito_necesitaCuentoYModo() {
        assertThat(Preferencias().tieneFavorito).isFalse()
        assertThat(Preferencias(favorito = "c1").tieneFavorito).isFalse()
        assertThat(Preferencias(favorito = "c1", modoFavorito = Modo.LEER).tieneFavorito).isTrue()
    }
}
