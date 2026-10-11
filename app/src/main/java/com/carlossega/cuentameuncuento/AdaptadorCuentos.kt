package com.carlossega.cuentameuncuento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

/**
 * Adapter para mostrar lista de cuentos en RecyclerView
 */
class AdaptadorCuentos(
    private val listaCuentos: ArrayList<Cuento>
) : RecyclerView.Adapter<AdaptadorCuentos.ViewHolderCuentos>() {

    private var listener: View.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCuentos {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_cuentos, parent, false)
        view.setOnClickListener(this::onClick)
        return ViewHolderCuentos(view)
    }

    override fun onBindViewHolder(holder: ViewHolderCuentos, position: Int) {
        val cuento = listaCuentos[position]

        holder.tvTitulo.text = cuento.titulo
        holder.tvDescripcion.text = cuento.descripcion

        // Cargar imagen con Coil
        holder.portada.load(cuento.imagen) {
            crossfade(true)
            placeholder(R.drawable.back)
            error(R.drawable.back)
        }
    }

    override fun getItemCount(): Int = listaCuentos.size

    fun setOnClickListener(listener: View.OnClickListener) {
        this.listener = listener
    }

    private fun onClick(view: View) {
        listener?.onClick(view)
    }

    class ViewHolderCuentos(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.idTitulo)
        val tvDescripcion: TextView = itemView.findViewById(R.id.idDescripcion)
        val portada: ImageView = itemView.findViewById(R.id.idImagen)
    }
}
