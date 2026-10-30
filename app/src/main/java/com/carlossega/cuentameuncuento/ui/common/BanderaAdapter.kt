package com.carlossega.cuentameuncuento.ui.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Idioma
import com.carlossega.cuentameuncuento.databinding.ItemBanderaBinding

/**
 * Adapter para los Spinner de idioma con banderas.
 * Con [conOpcionNinguno] la primera opción es "sin idioma" (traducción desactivada).
 */
class BanderaAdapter(conOpcionNinguno: Boolean = false) : BaseAdapter() {

    private val opciones: List<Idioma?> =
        if (conOpcionNinguno) listOf(null) + Idioma.entries else Idioma.entries

    override fun getCount(): Int = opciones.size

    override fun getItem(position: Int): Idioma? = opciones[position]

    override fun getItemId(position: Int): Long = position.toLong()

    fun posicionDe(idioma: Idioma?): Int = opciones.indexOf(idioma).coerceAtLeast(0)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = convertView?.let { ItemBanderaBinding.bind(it) }
            ?: ItemBanderaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val idioma = opciones[position]
        binding.ivBandera.setImageResource(idioma?.bandera ?: R.drawable.sin_bandera)
        binding.ivBandera.contentDescription = parent.context.getString(
            when (idioma) {
                Idioma.ESPANOL -> R.string.idioma_espanol
                Idioma.CATALAN -> R.string.idioma_catalan
                Idioma.INGLES -> R.string.idioma_ingles
                null -> R.string.sin_traduccion
            }
        )
        return binding.root
    }
}
