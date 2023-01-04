package com.carlossega.cuentameuncuento;

import static com.carlossega.cuentameuncuento.R.id.btn_login;
import static com.carlossega.cuentameuncuento.R.id.txt_info;
import static com.carlossega.cuentameuncuento.R.id.txt_perfil;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.units.qual.C;

public class MainActivity extends AppCompatActivity {


    public static final String EXTRA_MESSAGE = "com.carlossega.cuentameuncuento.MESSAGE";
    TextView txt_info, txt_perfil;
    Button btn_login, btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setTheme(R.style.SplashTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt_info = findViewById(R.id.txt_info);
        txt_perfil = findViewById(R.id.txt_perfil);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        txt_info.setText("Cuentame un cuento v1.0");

        /*//Para borrar con logout
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        getPreferences();*/
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferences();
    }

    //Función que nos devuelve las SharedPreferences si se han guardado
    private void getPreferences(){
        //Consultamos si existen y recogemos los valores en caso afirmativo
        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        String email = prefs.getString("user", null);
        String nombre = prefs.getString("nombre", null);
        String idioma = prefs.getString("idioma", "");
        String fav = prefs.getString("favorito", "");
        //Si contiene un mail, creamos un Usuario
        //Mostraremos o no los botones de inicio de sesión y Registro
        if (email != null){
            Usuario usuario = new Usuario(email, nombre, idioma, fav);
            if (usuario.getNombre().equals("")){ txt_perfil.setText(getString(R.string.bienvenido));} else {
                txt_perfil.setText(getString(R.string.bienvenido) + ", " + nombre);
            }
            btn_login.setVisibility(View.INVISIBLE);
            btn_register.setVisibility(View.INVISIBLE);
        } else {
            txt_perfil.setText("No se ha iniciado sesión");
            btn_login.setVisibility(View.VISIBLE);
            btn_register.setVisibility(View.VISIBLE);
        }
    }

    /** Called when the user taps the Send button */
    public void comenzar(View view) {
        Intent intent = new Intent(this, MenuPrincipal.class);
        intent.putExtra(EXTRA_MESSAGE, "mensaje");
        startActivity(intent);
    }

    public void login(View view){
        Intent intent = new Intent(this, Autenticacion.class);
        intent.putExtra(EXTRA_MESSAGE, "login");
        startActivity(intent);
    }

    public void register(View view){
        Intent intent = new Intent(this, Autenticacion.class);
        intent.putExtra(EXTRA_MESSAGE, "register");
        startActivity(intent);
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