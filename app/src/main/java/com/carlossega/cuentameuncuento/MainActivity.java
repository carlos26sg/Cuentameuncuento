package com.carlossega.cuentameuncuento;

import static com.carlossega.cuentameuncuento.R.id.txt_info;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    String user = "Prueba";
    public static final String EXTRA_MESSAGE = "com.carlossega.cuentameuncuento.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        TextView txt_info = findViewById(R.id.txt_info);
        txt_info.setText("Cuentame un cuento v1.0");
        TextView txt_inicio = findViewById(R.id.txt_inicio);
        txt_inicio.setText("Para iniciar sesión con Google pulse aquí");
    }

    /** Called when the user taps the Send button */
    public void comenzar(View view) {
        Intent intent = new Intent(this, MenuPrincipal.class);
        String message = user;
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

}