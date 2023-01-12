package com.carlossega.cuentameuncuento;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //Indicamos las variables necesarias
    public static final String EXTRA_MESSAGE = "com.carlossega.cuentameuncuento.MESSAGE";
    TextView txt_info, txt_perfil;
    Button btn_login, btn_register, btn_comenzar;
    String email, nombre, idioma;

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
        txt_info.setText(R.string.info_developer);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        btn_comenzar = findViewById(R.id.img_btn_comenzar);
        btn_comenzar.setText(getString(R.string.comenzar));
        btn_register.setText(R.string.registrarse);
        btn_login.setText(R.string.iniciar_sesion);

        //Cargamos las Preferences en caso de que se encuentre alguna guardada
        getPreferences();

        //Listeners de botones
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Autenticacion.class);
                intent.putExtra(EXTRA_MESSAGE, "login");
                finish();
                startActivity(intent);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Autenticacion.class);
                intent.putExtra(EXTRA_MESSAGE, "register");
                finish();
                startActivity(intent);
            }
        });
    }

    //Función que nos devuelve las SharedPreferences si se han guardado
    private void getPreferences(){
        //Consultamos si existen y recogemos los valores en caso afirmativo
        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        email = prefs.getString("mail", null);
        nombre = prefs.getString("nombre", null);
        idioma = prefs.getString("idioma", "");
        //Si contiene un mail, creamos un Usuario
        //Mostraremos o no los botones de inicio de sesión y Registro
        if (email != null){
            if (nombre.equals("")){
                txt_perfil.setText(getString(R.string.bienvenido) + ", " + email);
            } else {
                txt_perfil.setText(getString(R.string.bienvenido) + ", " + nombre);
            }
            btn_login.setVisibility(View.GONE);
            btn_register.setVisibility(View.GONE);
        } else {
            txt_perfil.setText(R.string.no_inicio);
            btn_login.setVisibility(View.VISIBLE);
            btn_register.setVisibility(View.VISIBLE);
        }
    }

    /** Se llama cuando el usuario pulsa el boton comenzar */
    public void comenzar(View view) {
        /**
         * Si no tenemos email (email=null) es que no se ha iniciado sesion anteriormente
         * Si tenemos algo guardado (en SharedPreferences) cargamos sus datos
         * y los pasamos al MenuPrincipal
         */
        Bundle extras = new Bundle();
        Intent intent = new Intent(MainActivity.this, MenuPrincipal.class);
        if (email != null){
            extras.putString("mail", email);
            intent.putExtras(extras);
            Log.d(TAG, "se inicia app con usuario, se envian datos y abrimos Menu principal");
        } else {
            extras.putString("mail", "noUser");
            intent.putExtras(extras);
            Log.d(TAG, "se inicia aplicación sin ningún usuario");
        }
        finish();
        startActivity(intent);
    }
}