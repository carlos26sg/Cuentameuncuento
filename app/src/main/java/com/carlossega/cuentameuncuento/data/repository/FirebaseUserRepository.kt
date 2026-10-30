package com.carlossega.cuentameuncuento.data.repository

import android.util.Log
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.data.model.Modo
import com.carlossega.cuentameuncuento.data.model.Preferencias
import com.carlossega.cuentameuncuento.data.prefs.PreferenciasStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Usuario anónimo de Firebase Auth con sus preferencias en `usuarios/{uid}`.
 * No se guarda ningún dato personal: solo idioma, cuento favorito y modo favorito.
 */
@Singleton
class FirebaseUserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val store: PreferenciasStore
) : UserRepository {

    override val preferencias: Flow<Preferencias> = store.preferencias

    override suspend fun asegurarSesion(): Boolean {
        if (auth.currentUser != null) return true
        return try {
            auth.signInAnonymously().await().user != null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo iniciar la sesión anónima", e)
            false
        }
    }

    override suspend fun sincronizar(): Result<Unit> = resultado {
        val uid = uidActual()
        val documento = firestore.collection(COLECCION).document(uid).get().await()
        if (documento.exists()) {
            store.guardar(
                Preferencias(
                    nombre = documento.getString("nombre").orEmpty(),
                    idioma = Idioma.desdeCodigo(documento.getString("idioma")),
                    favorito = documento.getString("favorito").orEmpty(),
                    modoFavorito = Modo.desdeCodigo(documento.getString("modo_fav"))
                )
            )
        }
    }

    override suspend fun guardar(preferencias: Preferencias): Result<Unit> = resultado {
        store.guardar(preferencias)
        // Lo importante es el guardado local; la copia en Firestore es para sincronizar
        if (!asegurarSesion()) return@resultado
        val uid = checkNotNull(auth.currentUser).uid
        val datos = mapOf(
            "nombre" to preferencias.nombre,
            "idioma" to preferencias.idioma.codigo,
            "favorito" to preferencias.favorito,
            "modo_fav" to preferencias.modoFavorito?.codigo.orEmpty(),
            "last_updated" to System.currentTimeMillis()
        )
        // Sin await: sin conexión Firestore guarda la escritura en cola y la envía más tarde
        firestore.collection(COLECCION).document(uid).set(datos, SetOptions.merge())
            .addOnFailureListener { Log.w(TAG, "No se pudieron subir las preferencias", it) }
    }

    override suspend fun borrarDatos(): Result<Unit> = resultado {
        // El borrado sí necesita confirmación del servidor; sin conexión falla en vez de quedarse esperando
        withTimeoutOrNull(TIEMPO_MAXIMO_BORRADO_MS) { borrarEnServidor() }
            ?: throw IOException("No se pudo contactar con el servidor")
        auth.signOut()
        store.borrar()
    }

    private suspend fun borrarEnServidor() {
        auth.currentUser?.let { usuario ->
            firestore.collection(COLECCION).document(usuario.uid).delete().await()
            try {
                usuario.delete().await()
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                // Un usuario anónimo no puede volver a autenticarse. Sus datos ya están
                // borrados, así que basta con cerrar la sesión
                Log.w(TAG, "No se pudo borrar el usuario anónimo", e)
            }
        }
    }

    private suspend fun uidActual(): String {
        if (!asegurarSesion()) throw IllegalStateException("No hay sesión de usuario")
        return checkNotNull(auth.currentUser).uid
    }

    private inline fun resultado(bloque: () -> Unit): Result<Unit> = try {
        bloque()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Error con los datos del usuario", e)
        Result.failure(e)
    }

    private companion object {
        const val TAG = "UserRepository"
        const val COLECCION = "usuarios"
        const val TIEMPO_MAXIMO_BORRADO_MS = 15_000L
    }
}
