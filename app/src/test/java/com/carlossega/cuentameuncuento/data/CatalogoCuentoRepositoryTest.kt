package com.carlossega.cuentameuncuento.data

import com.carlossega.cuentameuncuento.data.catalogo.Catalogo
import com.carlossega.cuentameuncuento.data.catalogo.CatalogoCuentoRepository
import com.carlossega.cuentameuncuento.data.catalogo.FuentesCatalogo
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class CatalogoCuentoRepositoryTest {

    private class FuentesFalsas(
        var incluido: String,
        var guardado: String? = null,
        var publicado: String? = null
    ) : FuentesCatalogo {
        var guardadoNuevo: String? = null

        override suspend fun incluido() = incluido
        override suspend fun guardado() = guardado
        override suspend fun guardar(texto: String) {
            guardadoNuevo = texto
        }

        override suspend fun descargar() = publicado ?: throw IOException("sin conexión")
        override fun url(ruta: String, incluida: Boolean) = if (incluida) "app:$ruta" else "web:$ruta"
    }

    private fun catalogo(generado: Long, vararg cuentos: String, formato: Int = 1) = """
        {"formato":$formato,"generado":$generado,"revision":"r$generado","cuentos":[${cuentos.joinToString(",")}]}
    """.trimIndent()

    private fun cuento(id: String, titulo: String, imagen: String = "cuentos/$id/img_1-a.webp") = """
        {"id":"$id","titulo":{"esp":"$titulo","eng":"$titulo EN"},"descripcion":{"esp":"Desc"},
         "portada":"cuentos/$id/portada-a.webp","ilustraciones":{"1":"$imagen"},
         "lineas":[{"imagen":"1","texto":{"esp":"Uno","eng":"One"}},
                   {"texto":{"esp":"Dos","eng":"Two"}},
                   {"texto":{"esp":"FIN","eng":"THE END"}}]}
    """.trimIndent()

    private fun TestScope.repositorio(fuentes: FuentesCatalogo) = CatalogoCuentoRepository(fuentes, backgroundScope)

    @Test
    fun listaElCatalogoIncluido_ordenadaYConImagenesDeLaApp() = runTest {
        val repo = repositorio(FuentesFalsas(catalogo(1, cuento("z", "Zorro"), cuento("a", "Árbol"))))

        val cuentos = repo.cuentos(Idioma.ESPANOL).first()

        assertThat(cuentos.map { it.titulo }).containsExactly("Árbol", "Zorro").inOrder()
        assertThat(cuentos.first().imagen).isEqualTo("app:cuentos/a/portada-a.webp")
    }

    @Test
    fun soloListaLosCuentosConTituloEnElIdioma() = runTest {
        val repo = repositorio(FuentesFalsas(catalogo(1, cuento("a", "Árbol"))))

        assertThat(repo.cuentos(Idioma.CATALAN).first()).isEmpty()
        assertThat(repo.cuentos(Idioma.INGLES).first().single().titulo).isEqualTo("Árbol EN")
    }

    @Test
    fun lineasEnElIdiomaPedido_alineadasEntreIdiomas() = runTest {
        val repo = repositorio(FuentesFalsas(catalogo(1, cuento("a", "Árbol"))))

        val esp = repo.getLineas("a", Idioma.ESPANOL).getOrThrow()
        val eng = repo.getLineas("a", Idioma.INGLES).getOrThrow()

        assertThat(esp.map { it.texto }).containsExactly("Uno", "Dos", "FIN").inOrder()
        assertThat(eng.map { it.texto }).containsExactly("One", "Two", "THE END").inOrder()
        assertThat(esp.map { it.imagen }).containsExactly("1", null, null).inOrder()
        assertThat(eng.last().esFinal).isTrue()
    }

    @Test
    fun cuentoQueNoExiste_esUnError() = runTest {
        val repo = repositorio(FuentesFalsas(catalogo(1, cuento("a", "Árbol"))))

        assertThat(repo.getLineas("x", Idioma.ESPANOL).isFailure).isTrue()
        assertThat(repo.getImagenes("x").isFailure).isTrue()
    }

    @Test
    fun usaElGuardadoSiEsMasNuevoQueElIncluido() = runTest {
        val fuentes = FuentesFalsas(
            incluido = catalogo(1, cuento("a", "Viejo")),
            guardado = catalogo(2, cuento("a", "Nuevo"))
        )

        assertThat(repositorio(fuentes).cuentos(Idioma.ESPANOL).first().single().titulo).isEqualTo("Nuevo")
    }

    @Test
    fun trasActualizarLaApp_elIncluidoMasNuevoGanaAlGuardado() = runTest {
        val fuentes = FuentesFalsas(
            incluido = catalogo(3, cuento("a", "De la app nueva")),
            guardado = catalogo(2, cuento("a", "Descargado antes"))
        )

        assertThat(repositorio(fuentes).cuentos(Idioma.ESPANOL).first().single().titulo)
            .isEqualTo("De la app nueva")
    }

    @Test
    fun descargaUnCatalogoMasNuevo_loGuardaYActualizaLaLista() = runTest {
        val publicado = catalogo(2, cuento("a", "Árbol"), cuento("b", "Barco", imagen = "cuentos/b/img_1-b.webp"))
        val fuentes = FuentesFalsas(incluido = catalogo(1, cuento("a", "Árbol")), publicado = publicado)
        val repo = repositorio(fuentes)

        val listas = repo.cuentos(Idioma.ESPANOL).take(2).toList()

        assertThat(listas[0].map { it.id }).containsExactly("a")
        assertThat(listas[1].map { it.id }).containsExactly("a", "b")
        assertThat(fuentes.guardadoNuevo).isEqualTo(publicado)
        // Las imágenes que ya vienen en la app se cargan de ella; las nuevas, de internet
        assertThat(repo.getImagenes("a").getOrThrow()["1"]).isEqualTo("app:cuentos/a/img_1-a.webp")
        assertThat(repo.getImagenes("b").getOrThrow()["1"]).isEqualTo("web:cuentos/b/img_1-b.webp")
    }

    @Test
    fun sinConexion_oCatalogoPublicadoNoValido_seQuedaConElQueHay() = runTest {
        val sinConexion = FuentesFalsas(incluido = catalogo(1, cuento("a", "Árbol")))
        val formatoNuevo = FuentesFalsas(
            incluido = catalogo(1, cuento("a", "Árbol")),
            publicado = catalogo(5, cuento("a", "Otro"), formato = 2)
        )

        for (fuentes in listOf(sinConexion, formatoNuevo)) {
            val repo = repositorio(fuentes)
            assertThat(repo.cuentos(Idioma.ESPANOL).first().single().titulo).isEqualTo("Árbol")
            advanceUntilIdle()
            assertThat(repo.cuentos(Idioma.ESPANOL).first().single().titulo).isEqualTo("Árbol")
            assertThat(fuentes.guardadoNuevo).isNull()
        }
    }

    @Test
    fun catalogo_rechazaTextoQueNoEsUnCatalogo() {
        assertThat(Catalogo.leer("no es json")).isNull()
        assertThat(Catalogo.leer(catalogo(1))).isNull()
        assertThat(Catalogo.leer(catalogo(1, cuento("a", "Árbol"), formato = 99))).isNull()
        assertThat(Catalogo.leer(catalogo(1, cuento("a", "Árbol")))).isNotNull()
    }
}
