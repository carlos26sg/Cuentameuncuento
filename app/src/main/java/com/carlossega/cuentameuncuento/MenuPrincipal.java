package com.carlossega.cuentameuncuento;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MenuPrincipal extends AppCompatActivity {

    Button leer, reproducir, salir, act_perfil, btn_musica, btn_sin_sonido;
    public static final String EXTRA_MESSAGE = "com.carlossega.cuentameuncuento.MESSAGE";
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        getSupportActionBar().hide();
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

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
                Intent intent = new Intent(MenuPrincipal.this, SeleccionCuento.class);
                intent.putExtra(EXTRA_MESSAGE, "leer");
                startActivity(intent);
            }
        });

        reproducir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MenuPrincipal.this, SeleccionCuento.class);
                intent.putExtra(EXTRA_MESSAGE, "reproducir");
                startActivity(intent);
            }
        });

        act_perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuPrincipal.this, Perfil.class);
                startActivity(intent);
            }
        });

    }

    public void cerrarSesion(View view){

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