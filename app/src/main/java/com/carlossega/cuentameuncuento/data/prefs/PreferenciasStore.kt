package com.carlossega.cuentameuncuento.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.Modo
import com.carlossega.cuentameuncuento.data.model.Preferencias
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
) {

    private val datos: Flow<Preferences> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    val preferencias: Flow<Preferencias> = datos.map { p ->
        Preferencias(
            nombre = p[NOMBRE].orEmpty(),
            idioma = Idioma.desdeCodigo(p[IDIOMA]),
            favorito = p[FAVORITO].orEmpty(),
            modoFavorito = Modo.desdeCodigo(p[MODO_FAV])
        )
    }

    val musicaActiva: Flow<Boolean> = datos.map { it[MUSICA] ?: true }

    suspend fun guardar(preferencias: Preferencias) {
        context.dataStore.edit { p ->
            p[NOMBRE] = preferencias.nombre
            p[IDIOMA] = preferencias.idioma.codigo
            p[FAVORITO] = preferencias.favorito
            p[MODO_FAV] = preferencias.modoFavorito?.codigo.orEmpty()
        }
    }

    suspend fun guardarMusicaActiva(activa: Boolean) {
        context.dataStore.edit { it[MUSICA] = activa }
    }

    suspend fun borrar() {
        context.dataStore.edit { it.clear() }
    }

    /**
     * Borra el fichero de EncryptedSharedPreferences de versiones anteriores (≤ 2.0).
     * Sus datos se recuperan de Firestore al arrancar.
     */
    fun borrarPreferenciasAntiguas() {
        context.deleteSharedPreferences(PREFS_ANTIGUAS)
    }

    private companion object {
        const val PREFS_ANTIGUAS = "secure_user_prefs"
        val NOMBRE = stringPreferencesKey("nombre")
        val IDIOMA = stringPreferencesKey("idioma")
        val FAVORITO = stringPreferencesKey("favorito")
        val MODO_FAV = stringPreferencesKey("modo_fav")
        val MUSICA = booleanPreferencesKey("musica")
    }
}
