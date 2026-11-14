package com.carlossega.cuentameuncuento.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.carlossega.cuentameuncuento.ui.ajustes.AjustesPantalla
import com.carlossega.cuentameuncuento.ui.lector.LectorPantalla
import com.carlossega.cuentameuncuento.ui.lista.ListaPantalla
import com.carlossega.cuentameuncuento.ui.menu.MenuPantalla
import com.carlossega.cuentameuncuento.ui.portada.PortadaPantalla
import kotlinx.serialization.Serializable

/*
 * Pantallas de la app. Los nombres de las propiedades son las claves que leen los
 * ViewModels en su SavedStateHandle.
 */
@Serializable
object Menu

@Serializable
object Lista

@Serializable
object Ajustes

/** Portada animada de un cuento, antes de empezar a leerlo */
@Serializable
data class Portada(val cuento: String, val idioma: String)

/** [linea] < 0: empieza por el principio. [escuchar]: empieza a narrar al abrirse */
@Serializable
data class Lector(val cuento: String, val idioma: String, val escuchar: Boolean = false, val linea: Int = -1)

@Composable
fun Navegacion() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = Menu,
        enterTransition = { fadeIn() + scaleIn(initialScale = 0.96f) },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        composable<Menu> {
            MenuPantalla(
                alAbrirCuentos = { nav.navigate(Lista) },
                alSeguirLeyendo = { lectura ->
                    nav.navigate(Lector(lectura.cuentoId, lectura.idioma.codigo, linea = lectura.linea))
                },
                alAbrirAjustes = { nav.navigate(Ajustes) }
            )
        }
        composable<Lista> {
            ListaPantalla(
                alVolver = { nav.popBackStack() },
                alElegir = { cuento, idioma -> nav.navigate(Portada(cuento, idioma.codigo)) }
            )
        }
        composable<Portada> {
            PortadaPantalla(
                alVolver = { nav.popBackStack() },
                alEmpezar = { cuento, idioma, escuchar, linea ->
                    nav.navigate(Lector(cuento, idioma.codigo, escuchar, linea)) {
                        // Al cerrar el cuento se vuelve a la lista, no a la portada
                        popUpTo<Portada> { inclusive = true }
                    }
                }
            )
        }
        composable<Lector> {
            LectorPantalla(
                alCerrar = { nav.popBackStack() },
                alElegirOtro = {
                    if (!nav.popBackStack(Lista, inclusive = false)) {
                        nav.navigate(Lista) { popUpTo(Menu) }
                    }
                }
            )
        }
        composable<Ajustes> {
            AjustesPantalla(alVolver = { nav.popBackStack() })
        }
    }
}
