package com.carlossega.cuentameuncuento.auth

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests unitarios para UserPreferences
 */
class UserPreferencesTest {

    @Test
    fun userPreferences_defaultValues() {
        val prefs = UserPreferences()

        assertThat(prefs.nombre).isEmpty()
        assertThat(prefs.idioma).isEqualTo("esp")
        assertThat(prefs.favorito).isEmpty()
        assertThat(prefs.modoFav).isEmpty()
    }

    @Test
    fun userPreferences_withCustomValues() {
        val prefs = UserPreferences(
            nombre = "Carlos",
            idioma = "cat",
            favorito = "cuento1",
            modoFav = "leer"
        )

        assertThat(prefs.nombre).isEqualTo("Carlos")
        assertThat(prefs.idioma).isEqualTo("cat")
        assertThat(prefs.favorito).isEqualTo("cuento1")
        assertThat(prefs.modoFav).isEqualTo("leer")
    }

    @Test
    fun userPreferences_copy_works() {
        val original = UserPreferences(nombre = "Original")
        val copy = original.copy(nombre = "Modified")

        assertThat(original.nombre).isEqualTo("Original")
        assertThat(copy.nombre).isEqualTo("Modified")
    }
}
