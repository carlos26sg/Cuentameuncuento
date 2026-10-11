package com.carlossega.cuentameuncuento

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests unitarios para modelos de datos
 */
class ModelosTest {

    @Test
    fun cuento_creacionYGetters() {
        val cuento = Cuento(
            titulo = "Test Title",
            descripcion = "Test Description",
            imagen = "http://test.com/image.jpg",
            id = "test_id"
        )

        assertThat(cuento.titulo).isEqualTo("Test Title")
        assertThat(cuento.descripcion).isEqualTo("Test Description")
        assertThat(cuento.imagen).isEqualTo("http://test.com/image.jpg")
        assertThat(cuento.id).isEqualTo("test_id")
    }

    @Test
    fun usuario_defaultValues() {
        val usuario = Usuario()

        assertThat(usuario.nombre).isEmpty()
        assertThat(usuario.mail).isEmpty()
        assertThat(usuario.idioma).isEqualTo("esp")
        assertThat(usuario.favorito).isEmpty()
        assertThat(usuario.modo_fav).isEmpty()
    }

    @Test
    fun spinnerId_toString_returnsId() {
        val spinner = SpinnerId(id = "test_id", nombre = "Test Name")

        assertThat(spinner.toString()).isEqualTo("test_id")
        assertThat(spinner.nombre).isEqualTo("Test Name")
    }
}
