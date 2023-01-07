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
    String idioma;
    AdaptadorCuentos adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_cuento);
        //Ocultamos barra
        getSupportActionBar().hide();
        //Iniciamos componentes
        atras = (Button) findViewById(R.id.btn_atras);
        selecciona = (TextView) findViewById(R.id.tv_selecciona);
        selecciona.setText(R.string.selecciona);

        idioma = "_esp";

        listaCuentos = new ArrayList<>();
        recyclerCuentos = (RecyclerView) findViewById(R.id.rv_lista_cuentos);
        recyclerCuentos.setLayoutManager(new LinearLayoutManager(this));
        llenarCuentos();
        adapter = new AdaptadorCuentos(listaCuentos);
        recyclerCuentos.setAdapter(adapter);

        //Click del boton atrás
        atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void llenarCuentos(){
        db.collection("cuentos")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //BUscamos en la BD y añadimos cada campo de los cuentos a la lista
                                listaCuentos.add(new Cuento(document.get("titulo" + idioma).toString(),
                                        document.get("descripcion").toString(), document.get("imagen").toString()));
                            }
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

    /**
     * Código para mostrar la aplicación a pantalla completa
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}