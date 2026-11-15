package com.carlossega.cuentameuncuento.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.carlossega.cuentameuncuento.ui.biblioteca.BibliotecaPantalla
import com.carlossega.cuentameuncuento.ui.ficha.FichaPantalla
import com.carlossega.cuentameuncuento.ui.lector.LectorPantalla
import kotlinx.serialization.Serializable

/*
 * Pantallas de la app. Los nombres de las propiedades son las claves que leen los
 * ViewModels en su SavedStateHandle.
 */

/** Inicio: todos los cuentos. Los ajustes se abren en un panel encima */
@Serializable
object Biblioteca

/** Ficha de un cuento: portada, datos, un vistazo y "Leer" / "Escuchar" */
@Serializable
data class Ficha(val cuento: String, val idioma: String)

/** [linea] < 0: empieza por el principio. [escuchar]: empieza a narrar al abrirse */
@Serializable
data class Lector(val cuento: String, val idioma: String, val escuchar: Boolean = false, val linea: Int = -1)

@Composable
fun Navegacion() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = Biblioteca,
        enterTransition = { fadeIn() + scaleIn(initialScale = 0.96f) },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        composable<Biblioteca> {
            BibliotecaPantalla(
                alElegir = { cuento, idioma -> nav.navigate(Ficha(cuento, idioma.codigo)) },
                alSeguirLeyendo = { lectura ->
                    nav.navigate(Lector(lectura.cuentoId, lectura.idioma.codigo, linea = lectura.linea))
                }
            )
        }
        composable<Ficha> {
            FichaPantalla(
                alVolver = { nav.popBackStack() },
                alEmpezar = { cuento, idioma, escuchar, linea ->
                    nav.navigate(Lector(cuento, idioma.codigo, escuchar, linea))
                }
            )
        }
        composable<Lector> {
            LectorPantalla(
                alCerrar = { nav.popBackStack() },
                alElegirOtro = { nav.popBackStack(Biblioteca, inclusive = false) }
            )
        }
    }
}
