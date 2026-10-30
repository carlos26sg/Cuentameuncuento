package com.carlossega.cuentameuncuento.ui.common

import android.widget.ImageButton
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.audio.MusicaFondo
import kotlinx.coroutines.launch

/**
 * Convierte el botón en el interruptor de la música de fondo (mismo estado en todas las pantallas)
 */
fun ImageButton.vincularMusica(owner: LifecycleOwner, musica: MusicaFondo) {
    setOnClickListener { musica.alternar() }
    owner.lifecycleScope.launch {
        owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            musica.activa.collect { activa ->
                setImageResource(if (activa) R.drawable.audio else R.drawable.sin_sonido)
                contentDescription = context.getString(
                    if (activa) R.string.silenciar_musica else R.string.activar_musica
                )
            }
        }
    }
}
