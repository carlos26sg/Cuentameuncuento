package com.carlossega.cuentameuncuento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class AdaptadorCuentos extends RecyclerView.Adapter<AdaptadorCuentos.ViewHolderCuentos> {

    ArrayList<Cuento> listaCuentos;

    public AdaptadorCuentos(ArrayList<Cuento> listaCuentos) {
        this.listaCuentos = listaCuentos;
    }

    @NonNull
    @Override
    public AdaptadorCuentos.ViewHolderCuentos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_cuentos, null,false);
        return new ViewHolderCuentos(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorCuentos.ViewHolderCuentos holder, int position) {
        //Asignamos los datos recogidos  a los componentes donde se visualizarán
        holder.tv_titulo.setText(listaCuentos.get(position).getTitulo());
        holder.tv_descripcion.setText(listaCuentos.get(position).getDescripcion());
        //Iniciamos la visualización con la librería Picasso
        Picasso.get().load(listaCuentos.get(position).getImagen()).into(holder.portada);
    }

    //Contador de Items
    @Override
    public int getItemCount() {
        return listaCuentos.size();
    }

    public class ViewHolderCuentos extends RecyclerView.ViewHolder {

        //Iniciamos los componentes que forman parte del RecyclerViewer
        TextView tv_titulo, tv_descripcion;
        ImageView portada;

        public ViewHolderCuentos(@NonNull View itemView) {
            super(itemView);
            //Asignamos los componentes a su ID
            tv_titulo = (TextView) itemView.findViewById(R.id.idTitulo);
            tv_descripcion = (TextView) itemView.findViewById(R.id.idDescripcion);
            portada = (ImageView) itemView.findViewById(R.id.idImagen);
        }
    }

}
