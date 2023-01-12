package com.carlossega.cuentameuncuento;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    //Indicamos las variables necesarias
    String idioma, modo, nombre_cuento, url_cuento = "", url_traducido= "",
            url_img = "", idioma_traduccion;
    int contador_lineas, lineas_cuento;
    Button musica, sin_musica, play, pause, salir, next, back;
    Spinner sp_banderas;
    TextView seleccionado, traducido;
    ImageView bandera;
    LinearLayout fondo;
    String[] cuento, cuento_traducido;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    boolean traducido_creado = false;

    //Creamos array de int para almacenar las imagenes
    int[] banderas = {R.drawable.sin_bandera, R.drawable.espanol, R.drawable.catalan, R.drawable.ingles};

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
        traducido = findViewById(R.id.txt_repro_traducido);
        next = findViewById(R.id.btn_repro_next);
        back = findViewById(R.id.btn_repro_back);
        fondo = findViewById(R.id.llRepro);

        //Recogemos los parametros que se pasan por activities
        Bundle extra = this.getIntent().getExtras();
        idioma = extra.getString("idioma");
        modo = extra.getString("modo");
        nombre_cuento = extra.getString("cuento");
        Log.d(TAG, "se recibe modo: " + modo);
        Log.d(TAG, "cuento elegido: " + cuento + " idioma : " + idioma);

        //Establecemos pantalla segun el modo seleccionado por el usuario
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

        //Iniciamos adaptador para el spinner
        IdiomaAdapter adaptador = new IdiomaAdapter();
        sp_banderas.setAdapter(adaptador);
        sp_banderas.setSelection(0);

        leerDatos(new FirestoreCallBack() {
            @Override
            public void onCallBack(String url) {
                new GetData().execute();
            }
        }, nombre_cuento, idioma);

        //Miramos estado de MediaPlayer y establecemos visibilidad de botones.
        if(MenuPrincipal.mp.isPlaying()){
            musica.setVisibility(View.VISIBLE);
            sin_musica.setVisibility(View.GONE);
        } else {
            musica.setVisibility(View.GONE);
            sin_musica.setVisibility(View.VISIBLE);
        }
        //Establecemos la bandera del ImageView
        checkIdiomaBandera();

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
                Log.d(TAG, "se abre alertdialog para cerrar ventana ");
            }

        });

        musica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuPrincipal.mp.pause();
                musica.setVisibility(View.GONE);
                sin_musica.setVisibility(View.VISIBLE);
                Log.d(TAG, "se pulsa boton musica ");
            }
        });

        sin_musica.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuPrincipal.mp.start();
                sin_musica.setVisibility(View.GONE);
                musica.setVisibility(View.VISIBLE);
                Log.d(TAG, "se pulsa boton mute ");
            }
        });


        sp_banderas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                /**
                 * Recogemos idioma y eliminamos la lista previa para cargar una nueva con
                 * el idioma seleccionado.
                 * Vemos con getSelectedItemPosition si hay algun idioma seleccionado
                 */
                if (sp_banderas.getSelectedItemPosition()!=0){
                        idioma_traduccion = selectedIdioma();
                        leerDatos(new FirestoreCallBack() {
                            @Override
                            public void onCallBack(String url) {
                                new GetData().execute();
                            }
                        }, nombre_cuento, idioma_traduccion);
                } else {
                    traducido.setText("");
                }
                Log.d(TAG, "se selecciona idioma del spinner ");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "no se selecciona nada en el spinner ");
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cuento[contador_lineas].contains("FIN/") || cuento[contador_lineas].contains("FI/")
                || cuento[contador_lineas].contains("THE END/")){
                    Log.d(TAG, "se econtró un final de cuento ");
                }else {
                    contador_lineas++;
                    String[] divisor = cuento[contador_lineas].split("/");
                    seleccionado.setText(divisor[0]);
                    //setImagen(divisor[1]);
                    setImagen(new FirestoreCallBack() {
                        @Override
                        public void onCallBack(String url) {
                            new GetImg().execute();
                        }
                    }, divisor[1]);
                    Log.d(TAG, "linea cuento: " + divisor[0] + "img: " + divisor[1]);
                    if (sp_banderas.getSelectedItemPosition() != 0){
                        String[] divisor_traducido = cuento_traducido[contador_lineas].split("/");
                        traducido.setText(divisor_traducido[0]);
                    }
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contador_lineas <= 0){
                    Log.d(TAG, "lineas <= 0 no se hace nada ");
                }else {
                    contador_lineas--;
                    String[] divisor = cuento[contador_lineas].split("/");
                    seleccionado.setText(divisor[0]);
                    //setImagen(divisor[1]);
                    setImagen(new FirestoreCallBack() {
                        @Override
                        public void onCallBack(String url) {
                            new GetImg().execute();
                        }
                    }, divisor[1]);
                    Log.d(TAG, "linea cuento: " + divisor[0] + "img: " + divisor[1]);
                    if (sp_banderas.getSelectedItemPosition() != 0){
                        String[] divisor_traducido = cuento_traducido[contador_lineas].split("/");
                        traducido.setText(divisor_traducido[0]);
                    }
                }
            }
        });
    }

    private void checkIdiomaBandera() {
        //Con el dato de idioma establecemos bandera en el imageView
        if (idioma.equals("esp")){bandera.setImageResource(R.drawable.espanol);}
        if (idioma.equals("cat")){bandera.setImageResource(R.drawable.catalan);}
        if (idioma.equals("eng")){bandera.setImageResource(R.drawable.ingles);}
    }

    class GetData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            String result = "";
            String url_main= "";
            int posicion=0;
            if(cuento == null){
                url_main = url_cuento;
                cuento = new String[lineas_cuento];
                Log.d(TAG,"array de cuento creado");
            } else{
                url_main = url_traducido;
                cuento_traducido = new String[lineas_cuento];
                traducido_creado = true;
                Log.d(TAG,"array de cuento traducido creado");
            }
            try {
                URL url = new URL(url_main);
                urlConnection = (HttpURLConnection) url.openConnection();
                int code = urlConnection.getResponseCode();
                if(code==200){
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    if (in != null) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                        String line = "";
                        while ((line = bufferedReader.readLine()) != null){
                            if (traducido_creado){
                                cuento_traducido[posicion] = line;
                                posicion++;
                            } else {
                                cuento[posicion] = line;
                                posicion++;
                            }
                        }
                        Log.d(TAG, "se obtienen las lineas del cuento");
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
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (traducido_creado){
                String[] divisor = cuento_traducido[contador_lineas].split("/");
                traducido.setText(divisor[0]);
            } else {
                String[] divisor = cuento[contador_lineas].split("/");
                Log.d(TAG, "linea cuento: " + divisor[0] + "img: " + divisor[1]);
                seleccionado.setText(divisor[0]);
                //setImagen(divisor[1]);
                setImagen(new FirestoreCallBack() {
                    @Override
                    public void onCallBack(String url) {
                        new GetImg().execute();
                    }
                }, divisor[1]);
            }
        }
    }

    class GetImg extends AsyncTask<String, Void, Drawable> {
        @Override
        protected Drawable doInBackground(String... params) {
            Drawable dr = null;
            try  {
                URL url = new URL(url_img);
                Log.d(TAG, "url" + url);
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                dr = new BitmapDrawable(image);
            } catch (Exception e) {
                Log.d(TAG, "GetImg exception " + e);
            }
            return dr;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            fondo.setBackgroundDrawable(result);
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
                        if (url_cuento.equals("")){
                            url_cuento = document.get("txt_" + idioma).toString();
                        } else {
                            url_traducido = document.get("txt_" + idioma_traduccion).toString();
                            Log.d(TAG, "guarda la url del cuento traducido");
                        }
                        lineas_cuento = Integer.parseInt(document.get("lines").toString());
                    }
                    if (url_traducido == null){
                        firestoreCallBack.onCallBack(url_cuento);
                    } else {
                        firestoreCallBack.onCallBack(url_traducido);
                    }
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

    //Función que recoge la posición del spinner y retorna el idioma seleccionado
    public String selectedIdioma(){
        String idioma = "";
        int position = sp_banderas.getSelectedItemPosition();
        if (position == 0){idioma = "none";}
        if (position == 1){idioma = "esp";}
        if (position == 2){idioma = "cat";}
        if (position == 3){idioma = "eng";}
        return idioma;
    }

    //Consulta a la base de datos del cuento y el idioma para obtener la url del cuento seleccionado
    private void setImagen(FirestoreCallBack firestoreCallBack, String num_img){
        DocumentReference docRef = db.collection("imagenes").document(nombre_cuento);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        url_img = document.get("img_" + num_img).toString();
                    }
                    firestoreCallBack.onCallBack(url_img);
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
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
            LayoutInflater inflater = LayoutInflater.from(ReproductorCuento.this);
            view = inflater.inflate(R.layout.itemspinner, null);
            ImageView iv1 = view.findViewById(R.id.iv_bandera);
            iv1.setImageResource(banderas[i]);
            return view;
        }
    }

}