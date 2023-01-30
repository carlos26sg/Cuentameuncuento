package com.carlossega.cuentameuncuento;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Objects;

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
    FirebaseFirestore db, db_img;
    boolean traducido_creado = false;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reproductor_cuento);

        //Hacemos llamada a clase para mostrar la pantalla completa
        PantallaCompleta pantallaCompleta = new PantallaCompleta();
        View decorView = getWindow().getDecorView();
        pantallaCompleta.pantallaCompleta(decorView);
        //Forzamos para mantener pantalla encendida y no se apague para leer el cuento
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Iniciamos componentes
        musica = findViewById(R.id.btn_repro_musica);
        sin_musica = findViewById(R.id.btn_repro_sin_sonido);
        play =  findViewById(R.id.btn_repro_play);
        pause = findViewById(R.id.btn_repro_pause);
        salir = findViewById(R.id.btn_repro_salir);
        sp_banderas = findViewById(R.id.sp_repro_traduccion);
        bandera = findViewById(R.id.img_bandera_seleccion);
        seleccionado = findViewById(R.id.txt_repro_cuento);
        traducido = findViewById(R.id.txt_repro_traducido);
        next = findViewById(R.id.btn_repro_next);
        back = findViewById(R.id.btn_repro_back);
        fondo = findViewById(R.id.llRepro);
        db = FirebaseFirestore.getInstance();
        db_img = FirebaseFirestore.getInstance();

        //Listeners de botones
        salir.setOnClickListener(view -> {
            //Se añade alerta para borrar
            AlertDialog.Builder builder = new AlertDialog.Builder(ReproductorCuento.this);
            builder.setMessage(R.string.seguro)
                    .setTitle(R.string.volver_seleccion);
            //Se añaden los botones
            builder.setPositiveButton(R.string.si, (dialog, id) -> {
                db.clearPersistence();
                db_img.clearPersistence();
                db.terminate();
                db_img.terminate();
                if (tts != null){
                    tts.stop();
                    tts.shutdown();
                }
                finish();
            });
            builder.setNegativeButton(R.string.no, (dialog, id) -> {
                // User cancelled the dialog
            });
            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
            Log.d(TAG, "se abre alertdialog para cerrar ventana ");
        });



        //Spinner al que le cargamos las banderas
        sp_banderas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                /*
                  Recogemos idioma y eliminamos la lista previa para cargar una nueva con
                  el idioma seleccionado.
                  Vemos con getSelectedItemPosition si hay algun idioma seleccionado
                 */
                if (sp_banderas.getSelectedItemPosition()!=0){
                        idioma_traduccion = selectedIdioma();
                        leerDatos(url -> new GetData().execute(), nombre_cuento, idioma_traduccion);
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

        next.setOnClickListener(view -> next());

        back.setOnClickListener(view -> {
            if (contador_lineas <= 0){
                Log.d(TAG, "lineas <= 0 no se hace nada ");
            }else {
                contador_lineas--;
                String[] divisor = cuento[contador_lineas].split("/");
                seleccionado.setText(divisor[0]);
                setImagen(url -> new GetImg().execute(), divisor[1]);
                Log.d(TAG, "linea cuento: " + divisor[0] + "img: " + divisor[1]);
                try {
                    if (sp_banderas.getSelectedItemPosition() != 0){
                        String[] divisor_traducido = cuento_traducido[contador_lineas].split("/");
                        traducido.setText(divisor_traducido[0]);
                    }
                }catch (NullPointerException e){
                    Log.d(TAG, "error: " + e);
                }
            }
        });

        //Con play iniciamos el Texto a voz de android
        play.setOnClickListener(view -> {
            play.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            ttsFunction();
            Log.d(TAG, "se pulsa play");
        });

        pause.setOnClickListener(view -> {
            play.setVisibility(View.VISIBLE);
            pause.setVisibility(View.GONE);
            tts.stop();
            tts.shutdown();
            Log.d(TAG, "se pulsa pause");
        });

        musica.setOnClickListener(view -> {
            MenuPrincipal.mp.pause();
            musica.setVisibility(View.GONE);
            sin_musica.setVisibility(View.VISIBLE);
            Log.d(TAG, "se pulsa boton musica ");
        });

        sin_musica.setOnClickListener(view -> {
            MenuPrincipal.mp.start();
            sin_musica.setVisibility(View.GONE);
            musica.setVisibility(View.VISIBLE);
            Log.d(TAG, "se pulsa boton mute ");
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Recogemos los parametros que se pasan por activities
        Bundle extra = this.getIntent().getExtras();
        idioma = extra.getString("idioma");
        modo = extra.getString("modo");
        nombre_cuento = extra.getString("cuento");
        Log.d(TAG, "se recibe modo: " + modo);
        Log.d(TAG, "cuento elegido: " + nombre_cuento + " idioma : " + idioma);

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
        BanderaAdapter adaptador = new BanderaAdapter("opcion_sin", this);
        sp_banderas.setAdapter(adaptador);
        sp_banderas.setSelection(0);

        //Leemos datos del cuento
        leerDatos(url -> new GetData().execute(), nombre_cuento, idioma);

        /*
          Miramos estado de MediaPlayer y establecemos visibilidad de botones.
          En bloque Try/Catch para evitar NullPointerException
         */
        try {
            if(MenuPrincipal.mp.isPlaying()){
                musica.setVisibility(View.VISIBLE);
                sin_musica.setVisibility(View.GONE);
                MenuPrincipal.btn_musica.setVisibility(View.VISIBLE);
                MenuPrincipal.btn_sin_sonido.setVisibility(View.GONE);
            } else {
                musica.setVisibility(View.GONE);
                sin_musica.setVisibility(View.VISIBLE);
                MenuPrincipal.btn_musica.setVisibility(View.GONE);
                MenuPrincipal.btn_sin_sonido.setVisibility(View.VISIBLE);
            }
        } catch (NullPointerException e){
            Log.d(TAG, "error: " + e);
        }

        //Establecemos la bandera del ImageView
        checkIdiomaBandera();

        //Comprobamos si está activo el botón
        if (MenuPrincipal.btn_musica.getVisibility() == View.VISIBLE){
            MenuPrincipal.mp.start();
        }

    }

    //Función para pasar a la siguiente linea
    private void next(){
        if (cuento[contador_lineas].contains("FIN/") || cuento[contador_lineas].contains("FI/")
                || cuento[contador_lineas].contains("THE END/")){
            Log.d(TAG, "se econtró un final de cuento ");
        }else {
            //Creamos hilo para evitar error android.view.ViewRootImpl$CalledFromWrongThreadException:
            // Only the original thread that created a view hierarchy can touch its views.
            runOnUiThread(() -> {
                try {
                    contador_lineas++;
                    String[] divisor = cuento[contador_lineas].split("/");
                    seleccionado.setText(divisor[0]);
                    setImagen(url -> new GetImg().execute(), divisor[1]);
                    Log.d(TAG, "linea cuento: " + divisor[0] + "img: " + divisor[1]);
                    if (sp_banderas.getSelectedItemPosition() != 0){
                        String[] divisor_traducido = cuento_traducido[contador_lineas].split("/");
                        traducido.setText(divisor_traducido[0]);
                    }
                }catch (NullPointerException e){
                    Log.d(TAG, "error: " + e);
                }
            });
        }
    }

    //Función que establece la bandera según el idioma recibido
    private void checkIdiomaBandera() {
        //Con el dato de idioma establecemos bandera en el imageView
        if (idioma.equals("esp")){bandera.setImageResource(R.drawable.espanol);}
        if (idioma.equals("cat")){bandera.setImageResource(R.drawable.catalan);}
        if (idioma.equals("eng")){bandera.setImageResource(R.drawable.ingles);}
    }

    //Función que lee el texto con la función de texto a voz de Android
    void leerTexto(String strTexto, String ub){
            Bundle bundle = new Bundle();
            bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
            tts.speak(strTexto, TextToSpeech.QUEUE_FLUSH, bundle, ub);
    }

    //Función de texto a voz de Android
    public void ttsFunction() {
        tts = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                Locale loc = null;
                if (idioma.equals("cat")){loc = new Locale("cat", "CAT");}
                if (idioma.equals("esp")){loc = new Locale("spa", "ESP");}
                if (idioma.equals("eng")){loc = new Locale("en", "GB");}
                int result = tts.setLanguage(loc);
                Log.d(TAG, "se establece idioma locale: " + idioma);
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(getApplicationContext(), "Lenguaje no soportado", Toast.LENGTH_SHORT).show();
                } else {
                    leerTexto(seleccionado.getText().toString(), seleccionado.getText().toString());
                    Log.d(TAG, "se inicia el OnUtterancee listener y se leen datos");
                    //OnUtteranceProgressListener
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                        @Override
                        public void onStart(String s) {
                            Log.v(TAG, "onInit exitoso");
                        }

                        //Cuando acaba de leer, miramos si es fin del cuento o hay mas lineas
                        @Override
                        public void onDone(String s) {
                            if (s.equals("FIN") || s.equals("FI") || s.equals("THE END")){
                                //Creamos hilo para evitar error android.view.ViewRootImpl$CalledFromWrongThreadException:
                                // Only the original thread that created a view hierarchy can touch its views.
                                runOnUiThread(() -> {
                                    //Reseteamos contador lineas y volvemos al principio
                                    play.setVisibility(View.VISIBLE);
                                    pause.setVisibility(View.GONE);
                                    contador_lineas = 0;
                                    String[] divisor = cuento[contador_lineas].split("/");
                                    Log.d(TAG, "linea cuento: " + divisor[0] + "img: " + divisor[1]);
                                    seleccionado.setText(divisor[0]);
                                    setImagen(url -> new GetImg().execute(), divisor[1]);
                                    tts.stop();
                                    tts.shutdown();
                                });
                            } else {
                                next();
                                tts.stop();
                                tts.shutdown();
                                ttsFunction();
                            }
                        }

                        @Override
                        public void onError(String s) {
                            Log.d(TAG, "error en la reproducción de voz");
                        }
                    });
                }
            } else {
                Toast.makeText(getApplicationContext(), "falló la inicialización", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //AsyncTask que con la url, coge el txt y lo guarda en un array linea a linea
    class GetData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            HttpURLConnection urlConnection = null;
            String url_main;
            int posicion=0;
            //Creamos array con las lineas del cuento. Dato que debe estar en la BD
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
            //Con la url, leemos el cuento y lo guardamos
            try {
                URL url = new URL(url_main);
                urlConnection = (HttpURLConnection) url.openConnection();
                int code = urlConnection.getResponseCode();
                if(code==200){
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    //Guardamos el cuento en un array, leyendolo linea a linea
                    while ((line = bufferedReader.readLine()) != null){
                        if (traducido_creado){
                            cuento_traducido[posicion] = line;
                        } else {
                            cuento[posicion] = line;
                        }
                        posicion++;
                    }
                    Log.d(TAG, "se obtienen las lineas del cuento");
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        //Establecemes el texto
        @Override
        protected void onPostExecute(Void unused) {
            if (traducido_creado){
                String[] divisor = cuento_traducido[contador_lineas].split("/");
                traducido.setText(divisor[0]);
            } else {
                String[] divisor = cuento[contador_lineas].split("/");
                Log.d(TAG, "linea cuento: " + divisor[0] + "img: " + divisor[1]);
                seleccionado.setText(divisor[0]);
                setImagen(url -> new GetImg().execute(), divisor[1]);
            }
        }
    }

    //Función que busca en la url y consigue la imagen correspondiente
    class GetImg extends AsyncTask<String, Void, Drawable> {
        @Override
        protected Drawable doInBackground(String... params) {
            Drawable dr = null;
            try  {
                URL url = new URL(url_img);
                Log.d(TAG, "url" + url);
                Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                dr = new BitmapDrawable(getResources(), image);
            } catch (Exception e) {
                Log.d(TAG, "GetImg exception " + e);
            }
            return dr;
        }

        //Se establece la imagen de fondo
        @Override
        protected void onPostExecute(Drawable result) {
            fondo.setBackground(result);
        }
    }

    //Consulta a la base de datos del cuento y el idioma para obtener la url del cuento seleccionado
    private void leerDatos(FirestoreCallBack firestoreCallBack, String cuento, String idioma){
        DocumentReference docRef = db.collection("cuentos").document(cuento);
        docRef.get().addOnCompleteListener(task -> {
            try {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (url_cuento.equals("")){
                            try {
                                url_cuento = Objects.requireNonNull(document.get("txt_" + idioma)).toString();
                            } catch (NullPointerException e){
                                Log.d(TAG, "error: " + e);
                            }
                        } else {
                            url_traducido = Objects.requireNonNull(document.get("txt_" + idioma_traduccion)).toString();
                            Log.d(TAG, "guarda la url del cuento traducido");
                        }
                        lineas_cuento = Integer.parseInt(Objects.requireNonNull(document.get("lines")).toString());
                    }
                    if (url_traducido == null){
                        firestoreCallBack.onCallBack(url_cuento);
                    } else {
                        firestoreCallBack.onCallBack(url_traducido);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            } catch (NullPointerException e){
                onStart();
                Log.d(TAG, "error: " + e);
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
        try {
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        url_img = Objects.requireNonNull(document.get("img_" + num_img)).toString();
                    }
                    firestoreCallBack.onCallBack(url_img);
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });
        } catch (IllegalStateException e){
            onStart();
            Log.d(TAG, "error: " + e);
        }

    }

    //En caso de onDestroy, cerramos instancia db
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        db.clearPersistence();
        db.terminate();
        db_img.clearPersistence();
        db_img.terminate();
        super.onDestroy();
    }
}