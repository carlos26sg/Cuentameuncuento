package com.carlossega.cuentameuncuento

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

/**
 * Adapter para el Spinner de selección de idiomas con banderas
 */
class BanderaAdapter(
    modo: String,
    private val context: Context
) : BaseAdapter() {

    private val banderas: IntArray = when (modo) {
        "idiomas" -> intArrayOf(
            R.drawable.espanol,
            R.drawable.catalan,
            R.drawable.ingles
        )
        "opcion_sin" -> intArrayOf(
            R.drawable.sin_bandera,
            R.drawable.espanol,
            R.drawable.catalan,
            R.drawable.ingles
        )
        else -> intArrayOf()
    }

    override fun getCount(): Int = banderas.size

    override fun getItem(position: Int): Any = banderas[position]

    override fun getItemId(position: Int): Long = 0

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.itemspinner, null)
        val ivBandera: ImageView = view.findViewById(R.id.iv_bandera)
        ivBandera.setImageResource(banderas[position])
        return view
    }
}
