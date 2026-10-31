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
import com.carlossega.cuentameuncuento.data.model.VelocidadVoz
import com.carlossega.cuentameuncuento.data.repository.PreferenciasRepository
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
) : PreferenciasRepository {

    private val datos: Flow<Preferences> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    override val preferencias: Flow<Preferencias> = datos.map { p ->
        Preferencias(
            idioma = Idioma.desdeCodigo(p[IDIOMA]),
            favorito = p[FAVORITO].orEmpty(),
            modoFavorito = Modo.desdeCodigo(p[MODO_FAV]),
            velocidadVoz = VelocidadVoz.desdeCodigo(p[VELOCIDAD_VOZ])
        )
    }

    val musicaActiva: Flow<Boolean> = datos.map { it[MUSICA] ?: true }

    override suspend fun guardar(preferencias: Preferencias) {
        context.dataStore.edit { p ->
            p[IDIOMA] = preferencias.idioma.codigo
            p[FAVORITO] = preferencias.favorito
            p[MODO_FAV] = preferencias.modoFavorito?.codigo.orEmpty()
            p[VELOCIDAD_VOZ] = preferencias.velocidadVoz.codigo
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

    private companion object {
        const val PREFS_ANTIGUAS = "secure_user_prefs"
        val IDIOMA = stringPreferencesKey("idioma")
        val FAVORITO = stringPreferencesKey("favorito")
        val MODO_FAV = stringPreferencesKey("modo_fav")
        val MUSICA = booleanPreferencesKey("musica")
        val VELOCIDAD_VOZ = stringPreferencesKey("velocidad_voz")
    }
}
