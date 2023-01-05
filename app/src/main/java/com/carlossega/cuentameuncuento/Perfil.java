package com.carlossega.cuentameuncuento;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Perfil extends AppCompatActivity {

    TextView txt_perfil_nombre, txt_perfil_opciones, txt_perfil_favorito, txt_perfil_idioma,
        txt_perfil_mail, txt_perfil_email;
    EditText et_nombre;
    Button btn_eliminar, btn_cerrar, btn_confirmar, btn_atras;
    Spinner sp_favorito, sp_idioma;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String nombre, favorito, idioma, email;
    String mensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        //Asociamos todos los componentes al ID que le corresponde
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
        btn_eliminar = (Button) findViewById(R.id.btn_perfil_eliminar);
        btn_eliminar.setText(getString(R.string.eliminar_cuenta));
        btn_cerrar = (Button) findViewById(R.id.btn_perfil_cerrar);
        btn_cerrar.setText(getString(R.string.cerrar_sesion));
        btn_confirmar = (Button) findViewById(R.id.btn_perfil_confirmar);
        btn_confirmar.setText(getString(R.string.confirmar));
        btn_atras = (Button) findViewById(R.id.btn_perfil_atras);
        et_nombre = (EditText) findViewById(R.id.et_perfil_nombre);

        //Comprobamos el mail que llega para editar
        Bundle extra = this.getIntent().getExtras();
        mensaje = extra.getString("mail");
        //Buscamos en la BD la info asociada
        checkBD(mensaje);

        //Listeners de los botones
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
                SharedPreferences pref = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = pref.edit();
                editor.putBoolean("iniciada", false);
                editor.commit();
                finish();
            }
        });
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
                        et_nombre.setText(nombre);
                        idioma = document.get("idioma").toString();;
                        favorito = document.get("favorito").toString();
                        email = document.get("mail").toString();
                        txt_perfil_mail.setText(email);
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