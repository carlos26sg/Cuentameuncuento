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
    fun lineaCuento_separaTextoEImagen() {
        val linea = LineaCuento.desdeTexto("Había una vez un dragón/3")

        assertThat(linea.texto).isEqualTo("Había una vez un dragón")
        assertThat(linea.imagen).isEqualTo("3")
    }

    @Test
    fun lineaCuento_sinImagen() {
        assertThat(LineaCuento.desdeTexto("Texto sin imagen").imagen).isNull()
        assertThat(LineaCuento.desdeTexto("Texto con barra vacía/").imagen).isNull()
    }

    @Test
    fun lineaCuento_detectaFinalEnTodosLosIdiomas() {
        assertThat(LineaCuento.desdeTexto("FIN/").esFinal).isTrue()
        assertThat(LineaCuento.desdeTexto("FI/").esFinal).isTrue()
        assertThat(LineaCuento.desdeTexto("THE END/").esFinal).isTrue()
        assertThat(LineaCuento.desdeTexto("Fin de la primera parte/").esFinal).isFalse()
    }

    @Test
    fun lineaCuento_desdeFichero_quitaBomYLineasVacias() {
        val contenido = Char(0xFEFF) + "Primera/1\r\n\r\nSegunda\nFIN/\n\n"

        val lineas = LineaCuento.desdeFichero(contenido)

        assertThat(lineas.map { it.texto }).containsExactly("Primera", "Segunda", "FIN").inOrder()
        assertThat(lineas[0].imagen).isEqualTo("1")
    }

    @Test
    fun idioma_desdeCodigo_usaEspanolSiNoLoConoce() {
        assertThat(Idioma.desdeCodigo("cat")).isEqualTo(Idioma.CATALAN)
        assertThat(Idioma.desdeCodigo("eng")).isEqualTo(Idioma.INGLES)
        assertThat(Idioma.desdeCodigo("xx")).isEqualTo(Idioma.ESPANOL)
        assertThat(Idioma.desdeCodigo(null)).isEqualTo(Idioma.ESPANOL)
    }

    @Test
    fun modo_desdeCodigo_mantieneLosValoresDeFirestore() {
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
