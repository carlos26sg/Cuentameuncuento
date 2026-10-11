package com.carlossega.cuentameuncuento;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

//Clase que nos adapta el spinner para que sea un cuadrado que muestre la bandera
public class BanderaAdapter extends BaseAdapter {

    //Variables para manejar el adaptador
    Context con;
    int[] banderas;

    public BanderaAdapter(String modo, Context con) {
        this.con = con;
        if (modo.equals("idiomas")){
            //Creamos array de int para almacenar las imagenes de las banderas
            banderas = new int[]{R.drawable.espanol, R.drawable.catalan, R.drawable.ingles};
        } else if (modo.equals("opcion_sin")){
            banderas = new int[]{R.drawable.sin_bandera, R.drawable.espanol, R.drawable.catalan, R.drawable.ingles};
        }
    }

    @Override
    public int getCount() {
            return banderas.length;
        }

    @Override
    public Object getItem(int i) {
            return banderas[i];
        }

    @Override
    public long getItemId(int i) { return 0; }

    @SuppressLint({"InflateParams", "ViewHolder"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(con);
        view = inflater.inflate(R.layout.itemspinner, null);
        ImageView iv1 = view.findViewById(R.id.iv_bandera);
        iv1.setImageResource(banderas[i]);
        return view;
    }
}
