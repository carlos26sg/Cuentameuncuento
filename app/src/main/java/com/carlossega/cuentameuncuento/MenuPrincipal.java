package com.carlossega.cuentameuncuento;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
    MediaPlayer mp;
    String email, nombre, idioma, favorito;
    TextView info;
    Spinner sp_idioma;

    int[] banderas = {R.drawable.espanol, R.drawable.catalan, R.drawable.ingles};

    //Instanciamos la Base de datos con la que trabajamos
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Usuario user;

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


        //Iniciamos adaptador para el spinner
        IdiomaAdapter adaptador = new IdiomaAdapter();
        sp_idioma.setAdapter(adaptador);
        user = new Usuario();

        //Recogemos los parametros que se pasan por activities
        Bundle extra = this.getIntent().getExtras();
        email = extra.getString("mail");

        //Dependiendo de si llega o no un mail mostraremos mensajes diferentes
        if (!email.equals("noUser")){
            checkBD(email);
        } else {
            info.setText(getString(R.string.no_inicio));
            act_perfil.setVisibility(View.GONE);
        }

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
                finishAffinity();
                System.exit(0);
            }
        });

        leer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putString("mail", email);
                extras.putString("modo", "leer");
                extras.putString("idioma", selectedIdioma());
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
                extras.putString("modo", "reproducir");
                extras.putString("idioma", selectedIdioma());
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
                //Pasamos mail para poder acceder al perfil
                extras.putString("mail", email);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
    }

    //Función que recoge la posición del spinner y retorna el idioma seleccionado
    public String selectedIdioma(){
        String idioma = "";
        int position = sp_idioma.getSelectedItemPosition();
        if (position == 0){idioma = "esp";}
        if (position == 1){idioma = "cat";}
        if (position == 2){idioma = "eng";}
        return idioma;
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
            LayoutInflater inflater = LayoutInflater.from(MenuPrincipal.this);
            view = inflater.inflate(R.layout.itemspinner, null);
            ImageView iv1 = view.findViewById(R.id.iv_bandera);
            iv1.setImageResource(banderas[i]);
            return view;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkPreferences()){
            info.setText(getString(R.string.no_inicio));
            act_perfil.setVisibility(View.GONE);
        }
    }

    public boolean checkPreferences(){
        SharedPreferences preferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        boolean iniciado = preferences.getBoolean("iniciada", true);
        return iniciado;
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
                        favorito = document.get("favorito").toString();
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
                    //Con el dato de idioma establecemos selección del favorito del Usuario
                    if (idioma.equals("esp")){sp_idioma.setSelection(0);}
                    if (idioma.equals("cat")){sp_idioma.setSelection(1);}
                    if (idioma.equals("eng")){sp_idioma.setSelection(2);}
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}