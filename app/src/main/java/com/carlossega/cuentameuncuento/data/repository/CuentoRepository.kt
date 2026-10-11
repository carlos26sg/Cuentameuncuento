package com.carlossega.cuentameuncuento.data.repository

import com.carlossega.cuentameuncuento.Cuento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para gestionar cuentos desde Firestore
 * Abstrae el acceso a datos y facilita testing
 */
@Singleton
class CuentoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /**
     * Obtiene la lista de cuentos filtrados por idioma
     */
    suspend fun getCuentos(idioma: String): Result<List<Cuento>> {
        return try {
            val snapshot = firestore.collection("cuentos")
                .get()
                .await()

            val cuentos = snapshot.documents.mapNotNull { document ->
                try {
                    val titulo = document.getString("titulo_$idioma") ?: ""
                    val descripcion = document.getString("desc_$idioma") ?: ""
                    val imagen = document.getString("imagen") ?: ""
                    val id = document.getString("id") ?: ""

                    if (titulo.isNotEmpty() && id.isNotEmpty()) {
                        Cuento(titulo, descripcion, imagen, id)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(cuentos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene la URL del texto de un cuento específico
     */
    suspend fun getCuentoTextUrl(cuentoId: String, idioma: String): Result<String> {
        return try {
            val document = firestore.collection("cuentos")
                .document(cuentoId)
                .get()
                .await()

            val url = document.getString("txt_$idioma") ?: ""
            if (url.isNotEmpty()) {
                Result.success(url)
            } else {
                Result.failure(Exception("URL no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene la URL de una imagen específica de un cuento
     */
    suspend fun getImagenUrl(cuentoId: String, numeroImagen: String): Result<String> {
        return try {
            val document = firestore.collection("imagenes")
                .document(cuentoId)
                .get()
                .await()

            val url = document.getString("img_$numeroImagen") ?: ""
            if (url.isNotEmpty()) {
                Result.success(url)
            } else {
                Result.failure(Exception("Imagen no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
