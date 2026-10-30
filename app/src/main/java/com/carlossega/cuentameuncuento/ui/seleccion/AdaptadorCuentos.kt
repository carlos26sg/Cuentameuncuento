package com.carlossega.cuentameuncuento.ui.seleccion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.error
import coil3.request.placeholder
import com.carlossega.cuentameuncuento.R
import com.carlossega.cuentameuncuento.data.model.Cuento
import com.carlossega.cuentameuncuento.databinding.ItemCuentoBinding

/**
 * Adapter para mostrar lista de cuentos en RecyclerView
 */
class AdaptadorCuentos(
    private val alPulsar: (Cuento) -> Unit
) : ListAdapter<Cuento, AdaptadorCuentos.CuentoViewHolder>(Diferencias) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CuentoViewHolder {
        val binding = ItemCuentoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CuentoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CuentoViewHolder, position: Int) {
        holder.mostrar(getItem(position))
    }

    inner class CuentoViewHolder(
        private val binding: ItemCuentoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun mostrar(cuento: Cuento) {
            binding.txtTitulo.text = cuento.titulo
            binding.txtDescripcion.text = cuento.descripcion
            binding.imgPortada.load(cuento.imagen) {
                placeholder(R.mipmap.ic_launcher)
                error(R.mipmap.ic_launcher)
            }
            binding.root.setOnClickListener { alPulsar(cuento) }
        }
    }

    private object Diferencias : DiffUtil.ItemCallback<Cuento>() {
        override fun areItemsTheSame(oldItem: Cuento, newItem: Cuento) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Cuento, newItem: Cuento) = oldItem == newItem
    }
}
