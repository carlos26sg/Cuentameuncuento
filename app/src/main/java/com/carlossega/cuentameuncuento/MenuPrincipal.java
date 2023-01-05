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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.StandardCharsets;

public class MenuPrincipal extends AppCompatActivity {

    Button leer, reproducir, salir, act_perfil, btn_musica, btn_sin_sonido;
    MediaPlayer mp;
    String email, nombre, idioma, favorito;
    TextView info;
    //Instanciamos la Base de datos con la que trabajamos
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Usuario user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        getSupportActionBar().hide();
        info = findViewById(R.id.txt_menu_info);
        user = new Usuario();
        // Get the Intent that started this activity and extract the string
        Bundle extra = this.getIntent().getExtras();
        email = extra.getString("mail");
        System.out.println(email);
        if (!email.equals("noUser")){
            checkBD(email);
        } else {
            info.setText(getString(R.string.no_inicio));
        }

        /* Captura el mensaje que se le pasa de la primera activity
        Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.txt1);
        textView.setText(message);*/

        leer = (Button) findViewById(R.id.btn_leer);
        reproducir = (Button) findViewById(R.id.btn_reproducir);
        salir = (Button) findViewById(R.id.btn_salir);
        act_perfil = (Button) findViewById(R.id.btn_act_perfil);
        act_perfil.setText(R.string.perfil);
        salir.setText(getString(R.string.salir));
        leer.setText(getString(R.string.leer));
        btn_musica = (Button) findViewById(R.id.btn_musica);
        btn_sin_sonido = (Button) findViewById(R.id.btn_sin_musica);
        reproducir.setText(getString(R.string.reproducir));
        mp = MediaPlayer.create(MenuPrincipal.this, R.raw.hilo_musical);
        mp.start();

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
                finishAffinity();
                System.exit(0);
            }
        });

        leer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putString("mail", email);
                extras.putString("funcion", "leer");
                Intent intent = new Intent(MenuPrincipal.this, SeleccionCuento.class);
                //Agrega el objeto bundle al Intent
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        reproducir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putString("mail", email);
                extras.putString("funcion", "reproducir");
                Intent intent = new Intent(MenuPrincipal.this, SeleccionCuento.class);
                //Agrega el objeto bundle al Intent
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        act_perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuPrincipal.this, Perfil.class);
                Bundle extras = new Bundle();
                extras.putString("mail", email);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkPreferences()){
            info.setText(getString(R.string.no_inicio));
        }
    }

    public boolean checkPreferences(){
        SharedPreferences preferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        boolean iniciado = preferences.getBoolean("iniciada", false);
        return iniciado;
    }

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
                        favorito = document.get("favorito").toString();
                        System.out.println(nombre + idioma + favorito + document.get("mail").toString());
                        user.setNombre(nombre);
                        user.setIdioma(idioma);
                        user.setFavorito(favorito);
                        user.setMail(email);
                        if (user.getNombre().equals("")){
                            info.setText("Bienvenido " + user.getMail());
                        } else {
                            info.setText("Bienvenido " + user.getNombre());
                        }
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
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