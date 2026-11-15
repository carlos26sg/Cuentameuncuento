package com.carlossega.cuentameuncuento.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.LetraCuento
import com.carlossega.cuentameuncuento.data.model.Lectura
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.data.model.TamanoTexto
import com.carlossega.cuentameuncuento.data.model.VelocidadVoz
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias")

/**
 * Preferencias locales con DataStore.
 * Sustituye a EncryptedSharedPreferences: aquí no hay datos secretos y DataStore
 * no da problemas al restaurar copias de seguridad en otro dispositivo.
 */
@Singleton
class PreferenciasStore @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferenciasRepository {

    private val datos: Flow<Preferences> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    override val preferencias: Flow<Preferencias> = datos
        .map { p ->
            Preferencias(
                idioma = Idioma.desdeCodigo(p[IDIOMA]),
                favoritos = p.favoritos(),
                velocidadVoz = VelocidadVoz.desdeCodigo(p[VELOCIDAD_VOZ]),
                letra = LetraCuento.desdeCodigo(p[LETRA]),
                tamanoTexto = TamanoTexto.desdeCodigo(p[TAMANO_TEXTO])
            )
        }
        .distinctUntilChanged()

    override val lectura: Flow<Lectura?> = datos
        .map { p ->
            val cuento = p[LECTURA_CUENTO]?.takeIf { it.isNotEmpty() } ?: return@map null
            Lectura(cuento, Idioma.desdeCodigo(p[LECTURA_IDIOMA]), p[LECTURA_LINEA] ?: 0)
        }
        .distinctUntilChanged()

    val musicaActiva: Flow<Boolean> = datos.map { it[MUSICA] ?: true }

    override suspend fun cambiarIdioma(idioma: Idioma) {
        context.dataStore.edit { it[IDIOMA] = idioma.codigo }
    }

    override suspend fun cambiarVelocidad(velocidad: VelocidadVoz) {
        context.dataStore.edit { it[VELOCIDAD_VOZ] = velocidad.codigo }
    }

    override suspend fun cambiarLetra(letra: LetraCuento) {
        context.dataStore.edit { it[LETRA] = letra.codigo }
    }

    override suspend fun cambiarTamanoTexto(tamano: TamanoTexto) {
        context.dataStore.edit { it[TAMANO_TEXTO] = tamano.codigo }
    }

    override suspend fun alternarFavorito(cuentoId: String) {
        context.dataStore.edit { p ->
            val actuales = p.favoritos()
            p[FAVORITOS] = if (cuentoId in actuales) actuales - cuentoId else actuales + cuentoId
            // El favorito único de la versión 2.1 ya está incluido en la lista
            p.remove(FAVORITO_ANTIGUO)
            p.remove(MODO_FAV_ANTIGUO)
        }
    }

    override suspend fun guardarLectura(lectura: Lectura?) {
        context.dataStore.edit { p ->
            if (lectura == null) {
                p.remove(LECTURA_CUENTO)
                p.remove(LECTURA_IDIOMA)
                p.remove(LECTURA_LINEA)
            } else {
                p[LECTURA_CUENTO] = lectura.cuentoId
                p[LECTURA_IDIOMA] = lectura.idioma.codigo
                p[LECTURA_LINEA] = lectura.linea
            }
        }
    }

    suspend fun guardarMusicaActiva(activa: Boolean) {
        context.dataStore.edit { it[MUSICA] = activa }
    }

    /**
     * Borra el fichero de EncryptedSharedPreferences de versiones anteriores (≤ 2.0).
     * Las preferencias antiguas (idioma y favorito) se pierden: hay que volver a elegirlas.
     */
    fun borrarPreferenciasAntiguas() {
        context.deleteSharedPreferences(PREFS_ANTIGUAS)
    }

    /** Hasta la versión 2.1 solo había un favorito: si aún no hay lista, se parte de él */
    private fun Preferences.favoritos(): Set<String> =
        this[FAVORITOS] ?: setOfNotNull(this[FAVORITO_ANTIGUO]?.takeIf { it.isNotEmpty() })

    private companion object {
        const val PREFS_ANTIGUAS = "secure_user_prefs"
        val IDIOMA = stringPreferencesKey("idioma")
        val FAVORITOS = stringSetPreferencesKey("favoritos")
        val FAVORITO_ANTIGUO = stringPreferencesKey("favorito")
        val MODO_FAV_ANTIGUO = stringPreferencesKey("modo_fav")
        val MUSICA = booleanPreferencesKey("musica")
        val VELOCIDAD_VOZ = stringPreferencesKey("velocidad_voz")
        val LETRA = stringPreferencesKey("letra")
        val TAMANO_TEXTO = stringPreferencesKey("tamano_texto")
        val LECTURA_CUENTO = stringPreferencesKey("lectura_cuento")
        val LECTURA_IDIOMA = stringPreferencesKey("lectura_idioma")
        val LECTURA_LINEA = intPreferencesKey("lectura_linea")
    }
}
