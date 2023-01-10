package com.carlossega.cuentameuncuento;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ReproductorCuento extends AppCompatActivity {

    String idioma, modo, nombre_cuento, url_cuento, url_traducido;
    int contador_lineas, lineas_cuento;
    Button musica, sin_musica, play, pause, salir, next, back;
    Spinner sp_banderas;
    TextView seleccionado, traducido;
    ImageView fondo, bandera;
    String[] cuento;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproductor_cuento);

        //Hacemos llamada a clase para mostrar la pantalla completa
        PantallaCompleta pantallaCompleta = new PantallaCompleta();
        View decorView = getWindow().getDecorView();
        pantallaCompleta.pantallaCompleta(decorView);

        //Iniciamos componentes
        musica = findViewById(R.id.btn_repro_musica);
        sin_musica = findViewById(R.id.btn_repro_sin_sonido);
        play =  findViewById(R.id.btn_repro_play);
        salir = findViewById(R.id.btn_repro_salir);
        sp_banderas = findViewById(R.id.sp_repro_traduccion);
        bandera = findViewById(R.id.img_bandera_seleccion);
        seleccionado = findViewById(R.id.txt_repro_cuento);
        next = findViewById(R.id.btn_repro_next);
        back = findViewById(R.id.btn_repro_back);


        //Recogemos los parametros que se pasan por activities
        Bundle extra = this.getIntent().getExtras();
        idioma = extra.getString("idioma");
        modo = extra.getString("modo");
        nombre_cuento = extra.getString("cuento");
        Log.d(TAG, "se recibe modo: " + modo);
        Log.d(TAG, "cuento elegido: " + cuento + " idioma : " + idioma);


        if (modo.equals("leer")){
            next.setVisibility(View.VISIBLE);
            bandera.setVisibility(View.GONE);
            play.setVisibility(View.GONE);
            back.setVisibility(View.VISIBLE);
        } else if (modo.equals("reproducir")){
            next.setVisibility(View.GONE);
            bandera.setVisibility(View.VISIBLE);
            play.setVisibility(View.VISIBLE);
            back.setVisibility(View.GONE);
        }

        leerDatos(new FirestoreCallBack() {
            @Override
            public void onCallBack(String url) {
                new GetData().execute();
            }
        }, nombre_cuento, idioma);

        if(MenuPrincipal.mp.isPlaying()){
            musica.setVisibility(View.VISIBLE);
            sin_musica.setVisibility(View.GONE);
        } else {
            musica.setVisibility(View.GONE);
            sin_musica.setVisibility(View.VISIBLE);
        }

        //Listeners de botones
        salir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Se añade alerta para borrar
                AlertDialog.Builder builder = new AlertDialog.Builder(ReproductorCuento.this);
                builder.setMessage(R.string.seguro)
                        .setTitle(R.string.volver_seleccion);
                //Se añaden los botones
                builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        musica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuPrincipal.mp.pause();
                musica.setVisibility(View.GONE);
                sin_musica.setVisibility(View.VISIBLE);
            }
        });

        sin_musica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuPrincipal.mp.start();
                sin_musica.setVisibility(View.GONE);
                musica.setVisibility(View.VISIBLE);
            }
        });

        //Con el dato de idioma establecemos bandera en el imageView
        if (idioma.equals("esp")){bandera.setImageResource(R.drawable.espanol);}
        if (idioma.equals("cat")){bandera.setImageResource(R.drawable.catalan);}
        if (idioma.equals("eng")){bandera.setImageResource(R.drawable.ingles);}

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cuento[contador_lineas].equals("FIN") || cuento[contador_lineas].equals("FI")
                || cuento[contador_lineas].equals("THE END")){
                    Log.d(TAG, "se econtró un final de cuento ");
                }else {
                    contador_lineas++;
                    seleccionado.setText(cuento[contador_lineas]);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contador_lineas <= 0){

                }else {
                    contador_lineas--;
                    seleccionado.setText(cuento[contador_lineas]);
                }
            }
        });

    }

    class GetData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            String result = "";
            int posicion=0;
            cuento = new String[lineas_cuento];
            try {
                URL url = new URL(url_cuento);
                urlConnection = (HttpURLConnection) url.openConnection();
                int code = urlConnection.getResponseCode();
                if(code==200){
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    if (in != null) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                        String line = "";
                        while ((line = bufferedReader.readLine()) != null){
                            cuento[posicion] = line;
                            Log.d(TAG, "se obtienen los datos " + cuento[posicion] + ", posicion = " + posicion);
                            posicion++;
                        }
                    }
                    in.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }
            for (int i = 0; i < cuento.length; i++){
                System.out.println(cuento[i]);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            seleccionado.setText(cuento[contador_lineas]);
        }
    }

    //Consulta a la base de datos del cuento y el idioma para obtener la url del cuento seleccionado
    private void leerDatos(FirestoreCallBack firestoreCallBack, String cuento, String idioma){
        DocumentReference docRef = db.collection("cuentos").document(cuento);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        url_cuento = document.get("txt_" + idioma).toString();
                        lineas_cuento = Integer.parseInt(document.get("lines").toString());
                        Log.d(TAG, "se obtienen los datos " + url_cuento +
                                " lineas del cuento " + lineas_cuento);
                    }
                    firestoreCallBack.onCallBack(url_cuento);
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //Se crea interfaz para poder realizar una consulta sincrona
    private interface FirestoreCallBack{
        void onCallBack(String url);
    }

}