package com.carlossega.cuentameuncuento;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SeleccionCuento extends AppCompatActivity {

    Button atras;
    TextView selecciona;
    ArrayList<Cuento> listaCuentos;
    RecyclerView recyclerCuentos;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String idioma, modo, cuento;
    AdaptadorCuentos adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_cuento);

        //Hacemos llamada a clase para mostrar la pantalla completa
        PantallaCompleta pantallaCompleta = new PantallaCompleta();
        View decorView = getWindow().getDecorView();
        pantallaCompleta.pantallaCompleta(decorView);

        //Iniciamos componentes
        atras = (Button) findViewById(R.id.btn_atras);
        selecciona = (TextView) findViewById(R.id.tv_selecciona);
        selecciona.setText(R.string.selecciona);

        //Recogemos los parametros que se pasan por activities
        Bundle extra = this.getIntent().getExtras();
        idioma = extra.getString("idioma");
        modo = extra.getString("modo");

        //Iniciamos ArrayList donde guardaremos los cuentos con la consulta a la BD
        listaCuentos = new ArrayList<>();
        //Asociamos RecyclerView con el id del componente
        recyclerCuentos = (RecyclerView) findViewById(R.id.rv_lista_cuentos);
        recyclerCuentos.setLayoutManager(new LinearLayoutManager(this));
        //Llamamos función que no hará la consulta y llenará el Recycler
        llenarCuentos();

        //Click del boton atrás
        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
                                        document.get("desc_" + idioma).toString(), document.get("imagen").toString()));
                            }
                            //Cuando acabe de rellenar el Arraylist pasamos esa lista al adaptador
                            adapter = new AdaptadorCuentos(listaCuentos);
                            recyclerCuentos.setAdapter(adapter);
                        } else {
                            //Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    protected void onStart(){
        super.onStart();
    }
}