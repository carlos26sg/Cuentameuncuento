package com.carlossega.cuentameuncuento;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SeleccionCuento extends AppCompatActivity {

    //Indicamos las variables necesarias
    Button atras;
    TextView selecciona;
    ArrayList<Cuento> listaCuentos;
    RecyclerView recyclerCuentos;
    String idioma, modo, id_cuento, nombre;
    AdaptadorCuentos adapter;
    Spinner sp_idioma;
    FirebaseFirestore db;
    //Creamos array de int para almacenar las imagenes de las banderas
    int[] banderas = {R.drawable.espanol, R.drawable.catalan, R.drawable.ingles};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_cuento);

        //Hacemos llamada a clase para mostrar la pantalla completa
        PantallaCompleta pantallaCompleta = new PantallaCompleta();
        View decorView = getWindow().getDecorView();
        pantallaCompleta.pantallaCompleta(decorView);

        //Iniciamos componentes
        atras = findViewById(R.id.btn_atras);
        sp_idioma = findViewById(R.id.sp_seleccion_idioma);
        selecciona = findViewById(R.id.tv_selecciona);
        selecciona.setText(R.string.selecciona);

        //Recogemos los parametros que se pasan por activities
        Bundle extra = this.getIntent().getExtras();
        idioma = extra.getString("idioma");
        modo = extra.getString("modo");
        nombre = extra.getString("nombre");

        //Click del boton atrás
        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Cerramos conexión con BD para evitar errores
                db.clearPersistence();
                db.terminate();
                finish();
            }
        });

        sp_idioma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                /**
                 * Recogemos idioma y eliminamos la lista previa para cargar una nueva con el idioma seleccionado
                 */
                idioma = selectedIdioma();
                listaCuentos.clear();
                llenarCuentos();
                Log.d(TAG, "setOnSelectedListener del spinner ");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    //Función que recoge la posición del spinner y retorna el idioma seleccionado
    public String selectedIdioma(){
        String idioma = "";
        int position = sp_idioma.getSelectedItemPosition();
        if (position == 0){idioma = "esp";}
        if (position == 1){idioma = "cat";}
        if (position == 2){idioma = "eng";}
        return idioma;
    }

    private void llenarCuentos(){
        //Realizamos la consulta de todos los documentos de la colección cuentos
        db.collection("cuentos")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Buscamos en la BD y añadimos cada campo de los cuentos a la lista
                                listaCuentos.add(new Cuento(document.get("titulo_" + idioma).toString(),
                                        document.get("desc_" + idioma).toString(), document.get("imagen").toString(),
                                        document.get("id").toString()));
                            }
                            Log.d(TAG, "datos recogidos ");
                            //Cuando acabe de rellenar el Arraylist pasamos esa lista al adaptador
                            adapter = new AdaptadorCuentos(listaCuentos);
                            recyclerCuentos.setAdapter(adapter);
                            Log.d(TAG, "lista pasada al adaptador ");
                            //onClick del adaptador del recyclerView
                            adapter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    db.clearPersistence();
                                    db.terminate();
                                    id_cuento = listaCuentos.get(recyclerCuentos.getChildAdapterPosition(view)).getId();
                                    Bundle extras = new Bundle();
                                    extras.putString("modo", modo);
                                    extras.putString("idioma", idioma);
                                    extras.putString("cuento", id_cuento);
                                    Intent intent = new Intent(SeleccionCuento.this, ReproductorCuento.class);
                                    //Agrega el objeto bundle al Intent
                                    intent.putExtras(extras);
                                    startActivity(intent);

                                    Log.d(TAG, "se abre nueva activity, cuento: " +
                                            id_cuento + ", idioma: " + idioma + ", modo: " + modo);
                                }
                            });
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    protected void onStart(){
        super.onStart();
        db = FirebaseFirestore.getInstance();
        //Iniciamos adaptador para el spinner
        IdiomaAdapter adaptador = new IdiomaAdapter();
        sp_idioma.setAdapter(adaptador);
        //Con el dato de idioma establecemos bandera en el imageView
        if (idioma.equals("esp")){sp_idioma.setSelection(0);}
        if (idioma.equals("cat")){sp_idioma.setSelection(1);}
        if (idioma.equals("eng")){sp_idioma.setSelection(2);}

        //Iniciamos ArrayList donde guardaremos los cuentos con la consulta a la BD
        listaCuentos = new ArrayList<>();
        //Asociamos RecyclerView con el id del componente
        recyclerCuentos = findViewById(R.id.rv_lista_cuentos);
        recyclerCuentos.setLayoutManager(new LinearLayoutManager(this));
    }

    //Clase que nos adapta el spinner para que sea un cuadrado que muestre la bandera
    class IdiomaAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return banderas.length;
        }

        @Override
        public Object getItem(int i) {
            return banderas[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = LayoutInflater.from(SeleccionCuento.this);
            view = inflater.inflate(R.layout.itemspinner, null);
            ImageView iv1 = view.findViewById(R.id.iv_bandera);
            iv1.setImageResource(banderas[i]);
            return view;
        }
    }

}