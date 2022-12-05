package com.carlossega.cuentameuncuento;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MenuPrincipal extends AppCompatActivity {

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

        ImageButton btn_salir = (ImageButton) findViewById(R.id.imgbtn_salir);
        btn_salir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finishAffinity();
                System.exit(0);
            }
        });

    }
}