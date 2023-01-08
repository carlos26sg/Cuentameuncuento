package com.carlossega.cuentameuncuento;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //Indicamos las variables necesarias
    public static final String EXTRA_MESSAGE = "com.carlossega.cuentameuncuento.MESSAGE";
    TextView txt_info, txt_perfil;
    Button btn_login, btn_register;
    String email, nombre, idioma, fav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Hacemos llamada a clase para mostrar la pantalla completa
        PantallaCompleta pantallaCompleta = new PantallaCompleta();
        View decorView = getWindow().getDecorView();
        pantallaCompleta.pantallaCompleta(decorView);

        //Asociamos todos los componentes al ID que le corresponde
        txt_info = findViewById(R.id.txt_info);
        txt_perfil = findViewById(R.id.txt_perfil);
        txt_info.setText("Cuentame un cuento v1.0");
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        //Cargamos las Preferences en caso de que se encuentre alguna guardada
        getPreferences();
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
        email = prefs.getString("user", null);
        nombre = prefs.getString("nombre", null);
        idioma = prefs.getString("idioma", "");
        fav = prefs.getString("favorito", "");
        //Si contiene un mail, creamos un Usuario
        //Mostraremos o no los botones de inicio de sesión y Registro
        if (email != null){
            Usuario usuario = new Usuario(email, nombre, idioma, fav);
            if (usuario.getNombre().equals("")){ txt_perfil.setText(getString(R.string.bienvenido));} else {
                txt_perfil.setText(getString(R.string.bienvenido) + ", " + email);
            }
            btn_login.setVisibility(View.INVISIBLE);
            btn_register.setVisibility(View.INVISIBLE);
        } else {
            txt_perfil.setText("No se ha iniciado sesión");
            btn_login.setVisibility(View.VISIBLE);
            btn_register.setVisibility(View.VISIBLE);
        }
    }

    /** Se llama cuando el usuario pulsa el boton comenzar */
    public void comenzar(View view) {
        if (email != null){
            Bundle extras = new Bundle();
            extras.putString("mail", email);
            extras.putString("nombre", nombre);
            extras.putString("idioma", idioma);
            extras.putString("favorito", fav);
            Intent intent = new Intent(MainActivity.this, MenuPrincipal.class);
            //Agrega el objeto bundle al Intent
            intent.putExtras(extras);
            finish();
            startActivity(intent);
            System.out.println("Envio preferences");
        } else {
            Intent intent = new Intent(MainActivity.this, MenuPrincipal.class);
            Bundle extras = new Bundle();
            extras.putString("mail", "noUser");
            intent.putExtras(extras);
            finish();
            startActivity(intent);
            System.out.println("Envio comenzar directamente");
        }
    }

    //Función que llama la actividad de Login, pasandole mensaje login
    public void login(View view){
        Intent intent = new Intent(this, Autenticacion.class);
        intent.putExtra(EXTRA_MESSAGE, "login");
        finish();
        startActivity(intent);
    }

    //Función que llama la actividad de Register, pasandole mensaje register
    public void register(View view){
        Intent intent = new Intent(this, Autenticacion.class);
        intent.putExtra(EXTRA_MESSAGE, "register");
        finish();
        startActivity(intent);
    }
}