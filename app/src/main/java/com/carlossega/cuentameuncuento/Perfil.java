package com.carlossega.cuentameuncuento;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class Perfil extends AppCompatActivity {

    TextView txt_perfil_nombre, txt_perfil_opciones, txt_perfil_favorito, txt_perfil_idioma,
        txt_perfil_mail, txt_perfil_email;
    Button btn_eliminar, btn_cerrar, btn_confirmar, btn_atras;
    Spinner sp_favorito, sp_idioma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        txt_perfil_nombre = (TextView) findViewById(R.id.txt_perfil_nombre);
        txt_perfil_nombre.setText(getString(R.string.nombre));
        txt_perfil_opciones = (TextView) findViewById(R.id.txt_perfil_opciones);
        txt_perfil_opciones.setText(getString(R.string.opciones_perfil));
        txt_perfil_favorito = (TextView) findViewById(R.id.txt_perfil_favorito);
        txt_perfil_favorito.setText(getString(R.string.favorito));
        txt_perfil_idioma = (TextView) findViewById(R.id.txt_perfil_idioma);
        txt_perfil_idioma.setText(getString(R.string.idioma));
        txt_perfil_email = (TextView) findViewById(R.id.txt_perfil_email);
        txt_perfil_email.setText(getString(R.string.email));
        txt_perfil_mail = (TextView) findViewById(R.id.txt_perfil_mail);
        //txt_perfil_mail.setText(getString(email user));
        btn_eliminar = (Button) findViewById(R.id.btn_perfil_eliminar);
        btn_eliminar.setText(getString(R.string.eliminar_cuenta));
        btn_cerrar = (Button) findViewById(R.id.btn_perfil_cerrar);
        btn_cerrar.setText(getString(R.string.cerrar_sesion));
        btn_confirmar = (Button) findViewById(R.id.btn_perfil_confirmar);
        btn_confirmar.setText(getString(R.string.confirmar));
        btn_atras = (Button) findViewById(R.id.btn_perfil_atras);


        btn_atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Borramos los datos de SharedPreferences
        btn_cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.commit();
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