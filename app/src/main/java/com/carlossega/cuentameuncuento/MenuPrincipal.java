package com.carlossega.cuentameuncuento;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MenuPrincipal extends AppCompatActivity {

    //Indicamos las variables necesarias
    Button leer, reproducir, salir, act_perfil, btn_musica, btn_sin_sonido;
    String email, nombre= "", idioma = "";
    TextView info;
    Spinner sp_idioma;

    //Hacemos static el mediaplayer para poder acceder desde otras clases
    public static MediaPlayer mp;

    //Instanciamos la Base de datos con la que trabajamos
    FirebaseFirestore db;
    static Usuario user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        //Hacemos llamada a clase para mostrar la pantalla completa
        PantallaCompleta pantallaCompleta = new PantallaCompleta();
        View decorView = getWindow().getDecorView();
        pantallaCompleta.pantallaCompleta(decorView);

        //Asociamos todos los componentes al ID que le corresponde
        leer = findViewById(R.id.btn_leer);
        reproducir = findViewById(R.id.btn_reproducir);
        salir = findViewById(R.id.btn_salir);
        act_perfil = findViewById(R.id.btn_act_perfil);
        btn_musica = findViewById(R.id.btn_musica);
        btn_sin_sonido = findViewById(R.id.btn_sin_musica);
        info = findViewById(R.id.txt_menu_info);
        sp_idioma = findViewById(R.id.sp_idioma_menu);

        //Establecemos valores de texto
        act_perfil.setText(R.string.perfil);
        salir.setText(getString(R.string.salir));
        leer.setText(getString(R.string.leer));
        reproducir.setText(getString(R.string.reproducir));

        user = new Usuario();

        //Recogemos los parametros que se pasan por activities
        Bundle extra = this.getIntent().getExtras();
        email = extra.getString("mail");
        Log.d(TAG, "llega como usuario: " + email);

        //Arrancamos el hilo musical
        mp = MediaPlayer.create(MenuPrincipal.this, R.raw.hilo_musical);
        mp.start();
        //Contol del icono de musica en función de la reproducción
        if (mp.isPlaying()){
            btn_musica.setVisibility(View.VISIBLE);
            btn_sin_sonido.setVisibility(View.GONE);
        } else {
            btn_musica.setVisibility(View.GONE);
            btn_sin_sonido.setVisibility(View.VISIBLE);
        }

        //Listeners de botones
        btn_musica.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mp.pause();
                btn_musica.setVisibility(View.GONE);
                btn_sin_sonido.setVisibility(View.VISIBLE);
            }
        });

        btn_sin_sonido.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mp.start();
                btn_musica.setVisibility(View.VISIBLE);
                btn_sin_sonido.setVisibility(View.GONE);
            }
        });

        salir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.clearPersistence();
                db.terminate();
                finishAffinity();
                System.exit(0);
            }
        });

        leer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.clearPersistence();
                db.terminate();
                //Adjuntamos variables que pasaremos a siguiente activity y modo
                Bundle extras = new Bundle();
                idioma = selectedIdioma();
                extras.putString("nombre", user.getNombre());
                extras.putString("modo", "leer");
                extras.putString("idioma", idioma);
                //Agrega el objeto bundle al Intent y se inicia SeleccionCuento
                Intent intent = new Intent(MenuPrincipal.this, SeleccionCuento.class);
                intent.putExtras(extras);
                startActivity(intent);
                Log.d(TAG, "se inicia SeleccionCuento en modo: leer y nombre: " +
                        user.getNombre() + ", idioma: " + idioma);
            }
        });

        reproducir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.clearPersistence();
                db.terminate();
                //Adjuntamos variables que pasaremos a siguiente activity y modo
                Bundle extras = new Bundle();
                extras.putString("nombre", user.getNombre());
                extras.putString("modo", "reproducir");
                extras.putString("idioma", user.getIdioma());
                //Agrega el objeto bundle al Intent y se inicia SeleccionCuento
                Intent intent = new Intent(MenuPrincipal.this, SeleccionCuento.class);
                intent.putExtras(extras);
                startActivity(intent);
                Log.d(TAG, "se inicia SeleccionCuento en modo: reproducir y nombre: " +
                        user.getNombre() + ", idioma: " + idioma);
            }
        });

        act_perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.clearPersistence();
                db.terminate();
                Intent intent = new Intent(MenuPrincipal.this, Perfil.class);
                startActivity(intent);
            }
        });
    }

    private String selectedIdioma() {
        String result = "";
        if (user.getNombre() == null){
            if(leer.getText().equals("Leer")){result = "esp";}
            if(leer.getText().equals("Llegir")){result = "cat";}
            if(leer.getText().equals("Read")){result = "eng";}
            Log.d(TAG, "user es null");
        } else if (user.getNombre().equals("NombreDefecto")){
            if(leer.getText().equals("Leer")){result = "esp";}
            if(leer.getText().equals("Llegir")){result = "cat";}
            if(leer.getText().equals("Read")){result = "eng";}
        } else {
            result = user.getIdioma();
        }
        Log.d(TAG, "se establece idioma: " + result);
        return result;
    }

    @Override
    protected void onStart() {
        super.onStart();
        db = FirebaseFirestore.getInstance();
        //Dependiendo de si llega o no un mail mostraremos mensajes diferentes
        if (!email.equals("noUser")){
            try {
                if (user.getNombre().equals("NombreDefecto")){
                    info.setText(getString(R.string.no_inicio));
                    act_perfil.setVisibility(View.GONE);
                    user.setIdioma(selectedIdioma());
                } else {
                    checkBD(email);
                }
            //Controlamos NullPointerException si es la primera vez que se ejecuta
            } catch (NullPointerException e){
                checkBD(email);
            }
        } else {
            //Se establece nombre por defecto y se detecta idioma del usuario
            info.setText(getString(R.string.no_inicio));
            act_perfil.setVisibility(View.GONE);
            user.setNombre("NombreDefecto");
            user.setIdioma(selectedIdioma());
        }
        if(mp.isPlaying()){
            btn_musica.setVisibility(View.VISIBLE);
            btn_sin_sonido.setVisibility(View.GONE);
        } else {
            btn_musica.setVisibility(View.GONE);
            btn_sin_sonido.setVisibility(View.VISIBLE);
        }
    }

    //Hacemos busqueda en base de datos para rellenar los campos de información
    public void checkBD(String emailAComprobar){
        DocumentReference docRef = db.collection("usuario").document(emailAComprobar);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        nombre = document.get("nombre").toString();;
                        idioma = document.get("idioma").toString();;
                        user.setNombre(nombre);
                        user.setIdioma(idioma);
                        user.setMail(emailAComprobar);
                        if (user.getNombre().equals("")){
                            info.setText(getString(R.string.bienvenido) + " " + user.getMail());
                        } else {
                            info.setText(getString(R.string.bienvenido) + " " + user.getNombre());
                        }
                        Log.d(TAG, "carga correcta desde la base de datos");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

}