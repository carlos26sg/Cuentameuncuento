package com.carlossega.cuentameuncuento;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MenuPrincipal extends AppCompatActivity {

    //Indicamos las variables necesarias
    Button leer, reproducir, salir, act_perfil, btn_favorito;
    String email, nombre= "", idioma = "", modo_fav, cuento_fav;
    TextView info;
    FirebaseFirestore db;
    static Button btn_musica, btn_sin_sonido;

    //Hacemos static el mediaplayer para poder acceder desde otras clases
    public static MediaPlayer mp;

    //Iniciamos el usuario con el que manejaremos el perfil
    static Usuario usuario;

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
        btn_favorito = findViewById(R.id.btn_favorito);

        //Establecemos valores de texto
        act_perfil.setText(R.string.perfil);
        salir.setText(getString(R.string.salir));
        leer.setText(getString(R.string.leer));
        reproducir.setText(getString(R.string.reproducir));
        btn_favorito.setText(R.string.favorito);

        //Creamos usuario, con el que trabajaremos para manejar la información
        usuario = new Usuario();

        //Recogemos los parametros que se pasan por activities
        Bundle extra = this.getIntent().getExtras();
        email = extra.getString("mail");
        Log.d(TAG, "llega como usuario: " + email);

        //Arrancamos el hilo musical
        mp = MediaPlayer.create(MenuPrincipal.this, R.raw.hilo_musical);
        mp.start();

        //Listeners de botones
        btn_musica.setOnClickListener(v -> {
            mp.pause();
            btn_musica.setVisibility(View.GONE);
            btn_sin_sonido.setVisibility(View.VISIBLE);
        });

        btn_sin_sonido.setOnClickListener(v -> {
            mp.start();
            btn_musica.setVisibility(View.VISIBLE);
            btn_sin_sonido.setVisibility(View.GONE);
        });

        salir.setOnClickListener(v -> {
            finishAffinity();
            System.exit(0);
        });

        leer.setOnClickListener(v -> {
            //Adjuntamos variables que pasaremos a siguiente activity y modo
            Bundle extras = new Bundle();
            idioma = selectedIdioma();
            extras.putString("modo", "leer");
            extras.putString("idioma", idioma);
            if (idioma.equals("")){ return;}
            //Agrega el objeto bundle al Intent y se inicia SeleccionCuento
            Intent intent = new Intent(MenuPrincipal.this, SeleccionCuento.class);
            intent.putExtras(extras);
            startActivity(intent);
            Log.d(TAG, "se inicia SeleccionCuento en modo: leer y nombre: " +
                    usuario.getNombre() + ", idioma: " + idioma);
        });

        reproducir.setOnClickListener(v -> {
            //Adjuntamos variables que pasaremos a siguiente activity y modo
            Bundle extras = new Bundle();
            extras.putString("modo", "reproducir");
            extras.putString("idioma", usuario.getIdioma());
            if (idioma.equals("")){ return;}
            //Agrega el objeto bundle al Intent y se inicia SeleccionCuento
            Intent intent = new Intent(MenuPrincipal.this, SeleccionCuento.class);
            intent.putExtras(extras);
            startActivity(intent);
            Log.d(TAG, "se inicia SeleccionCuento en modo: reproducir y nombre: " +
                    usuario.getNombre() + ", idioma: " + idioma);
        });

        act_perfil.setOnClickListener(view -> {
            if (idioma.equals("")){ return;}
            Intent intent = new Intent(MenuPrincipal.this, Perfil.class);
            startActivity(intent);
        });

        btn_favorito.setOnClickListener(view -> {
            try {
                if (usuario.getNombre().equals("NombreDefecto")){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getString(R.string.inicia_sesion_acceso), Toast.LENGTH_LONG);
                    toast.show();
                } else if (usuario.getModo_fav().equals("") || usuario.getFavorito().equals("")){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getString(R.string.config_perfil), Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    //Adjuntamos variables que pasaremos a siguiente activity y modo
                    Bundle extras = new Bundle();
                    extras.putString("modo", modo_fav);
                    extras.putString("idioma", idioma);
                    extras.putString("cuento", cuento_fav);
                    Intent intent = new Intent(this, ReproductorCuento.class);
                    //Agrega el objeto bundle al Intent
                    intent.putExtras(extras);
                    startActivity(intent);
                    Log.d(TAG, "se inicia favorito en modo: " + usuario.getModo_fav() + ", cuento: "
                            + usuario.getFavorito() + "nombre: " + usuario.getNombre()
                            + ", idioma: " + idioma);
                }
            } catch (NullPointerException e){
                Toast toast = Toast.makeText(getApplicationContext(),
                        getString(R.string.espera), Toast.LENGTH_LONG);
                toast.show();
                Log.d(TAG, "error: " + e);
            }
        });
    }

    //Función para establecer el idioma según las entradas que tengamos
    private String selectedIdioma() {
        String result = "";
        if (usuario.getNombre() == null){
            if(leer.getText().equals("Leer")){result = "esp";}
            if(leer.getText().equals("Llegir")){result = "cat";}
            if(leer.getText().equals("Read")){result = "eng";}
            Log.d(TAG, "user es null");
        } else if (usuario.getNombre().equals("NombreDefecto")){
            if(leer.getText().equals("Leer")){result = "esp";}
            if(leer.getText().equals("Llegir")){result = "cat";}
            if(leer.getText().equals("Read")){result = "eng";}
        } else {
            result = usuario.getIdioma();
        }
        Log.d(TAG, "se establece idioma: " + result);
        return result;
    }

    @Override
    protected void onStart() {
        try {
            db.clearPersistence();
        } catch (NullPointerException e){
            Log.d(TAG, "error: " + e);
        }
        super.onStart();
        db = FirebaseFirestore.getInstance();
        //Dependiendo de si llega o no un mail mostraremos mensajes diferentes
        if (!email.equals("noUser")){
            try {
                if (usuario.getNombre().equals("NombreDefecto")){
                    info.setText(getString(R.string.no_inicio));
                    act_perfil.setVisibility(View.GONE);
                    usuario.setIdioma(selectedIdioma());
                } else {
                    checkBD(email);
                }
            //Controlamos NullPointerException si es la primera vez que se ejecuta
            } catch (NullPointerException e){
                Log.d(TAG, "mail pasado: " + email);
                checkBD(email);
            }
        } else {
            //Se establece nombre por defecto y se detecta idioma del usuario
            info.setText(getString(R.string.no_inicio));
            act_perfil.setVisibility(View.GONE);
            usuario.setNombre("NombreDefecto");
            usuario.setIdioma(selectedIdioma());
        }
        //Contol del icono de musica en función de la reproducción
        if (mp.isPlaying()){
            btn_musica.setVisibility(View.VISIBLE);
            btn_sin_sonido.setVisibility(View.GONE);
        } else {
            btn_musica.setVisibility(View.GONE);
            btn_sin_sonido.setVisibility(View.VISIBLE);
        }

        //Codigo setOnCompletionListener para volver a reproducir la canción
        mp.setOnCompletionListener(MediaPlayer::start);
    }

    //Hacemos busqueda en base de datos para rellenar los campos de información
    public void checkBD(String emailAComprobar){
        DocumentReference docRef = db.collection("usuario").document(emailAComprobar);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    try {
                        nombre = document.get("nombre").toString();
                        idioma = document.get("idioma").toString();
                        modo_fav = document.get("modo_fav").toString();
                        cuento_fav = document.get("favorito").toString();
                    } catch (NullPointerException e){
                        Log.d(TAG, "error: " + e);
                    }
                    usuario.setNombre(nombre);
                    usuario.setIdioma(idioma);
                    usuario.setMail(emailAComprobar);
                    usuario.setModo_fav(modo_fav);
                    usuario.setFavorito(cuento_fav);
                    if (usuario.getNombre().equals("")){
                        info.setText(getString(R.string.bienvenido) + " " + usuario.getMail());
                    } else {
                        info.setText(getString(R.string.bienvenido) + " " + usuario.getNombre());
                    }
                    Log.d(TAG, "carga correcta desde la base de datos");
                    db.clearPersistence();
                    db.terminate();
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }
}