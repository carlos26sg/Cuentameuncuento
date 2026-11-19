package com.carlossega.cuentameuncuento

import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LetraCuento
import com.carlossega.cuentameuncuento.data.model.LineaCuento
import com.carlossega.cuentameuncuento.data.model.VelocidadVoz
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
        assertThat(Idioma.desdeCodigo("eus")).isEqualTo(Idioma.EUSKERA)
    }

    @Test
    fun idioma_delSistema_eligeElDelMovilSiHayCuentosEnEl() {
        assertThat(Idioma.delSistema("fr")).isEqualTo(Idioma.FRANCES)
        assertThat(Idioma.delSistema("de")).isEqualTo(Idioma.ALEMAN)
        assertThat(Idioma.delSistema("gl")).isEqualTo(Idioma.GALLEGO)
        assertThat(Idioma.delSistema("eu")).isEqualTo(Idioma.EUSKERA)
        assertThat(Idioma.delSistema("ca")).isEqualTo(Idioma.CATALAN)
        assertThat(Idioma.delSistema("ja")).isEqualTo(Idioma.ESPANOL)
    }

    @Test
    fun finalDelCuento_enLosIdiomasNuevos() {
        assertThat(LineaCuento("ENDE", null).esFinal).isTrue()
        assertThat(LineaCuento("AMAIERA", null).esFinal).isTrue()
    }

    @Test
    fun letraYVelocidad_tienenValorPorDefecto() {
        assertThat(LetraCuento.desdeCodigo("atkinson")).isEqualTo(LetraCuento.ATKINSON)
        assertThat(LetraCuento.desdeCodigo(null)).isEqualTo(LetraCuento.ANDIKA)
        assertThat(VelocidadVoz.desdeCodigo("lenta")).isEqualTo(VelocidadVoz.LENTA)
        assertThat(VelocidadVoz.desdeCodigo("xx")).isEqualTo(VelocidadVoz.NORMAL)
    }
}
