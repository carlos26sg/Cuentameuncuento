package com.carlossega.cuentameuncuento;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Perfil extends AppCompatActivity {

    //Indicamos las variables necesarias
    TextView txt_perfil_nombre, txt_perfil_opciones, txt_perfil_favorito, txt_perfil_idioma,
        txt_perfil_mail, txt_perfil_email;
    EditText et_nombre;
    Button btn_eliminar, btn_cerrar, btn_confirmar, btn_atras;
    Spinner sp_favorito, sp_idioma;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String nombre, favorito, idioma, email, mensaje, idioma_sp;
    int[] banderas = {R.drawable.espanol, R.drawable.catalan, R.drawable.ingles};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        //Hacemos llamada a clase para mostrar la pantalla completa
        PantallaCompleta pantallaCompleta = new PantallaCompleta();
        View decorView = getWindow().getDecorView();
        pantallaCompleta.pantallaCompleta(decorView);

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
        sp_idioma = (Spinner) findViewById(R.id.sp_perfil_idioma);
        //Iniciamos adaptador para el spinner
        IdiomaAdapter adaptador = new IdiomaAdapter();
        sp_idioma.setAdapter(adaptador);

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

        btn_confirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //sp_favorito.getSelectedItem();
                idioma_sp = selectedIdioma();
                guardarBD(et_nombre.getText().toString(), idioma_sp);
                finish();
            }
        });

        //Borramos los datos de SharedPreferences para cerrar sesión del usuario
        btn_cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.commit();
                editor.putBoolean("iniciada", false);
                editor.commit();
                finish();
            }
        });

        btn_eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Se añade alerta para borrar
                AlertDialog.Builder builder = new AlertDialog.Builder(Perfil.this);
                builder.setMessage(R.string.seguro)
                        .setTitle(R.string.borrar);
                //Se añaden los botones
                builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.clear();
                        editor.commit();
                        editor.putBoolean("iniciada", false);
                        editor.commit();
                        finish();
                        eliminarBD();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
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
                    if (idioma.equals("esp")){sp_idioma.setSelection(0);}
                    if (idioma.equals("cat")){sp_idioma.setSelection(1);}
                    if (idioma.equals("eng")){sp_idioma.setSelection(2);}
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
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

    //Función para actualizar los datos de un registro
    public void guardarBD(String nombre, String idioma_spinner){
        Map<String, Object> data = new HashMap<>();
        data.put("nombre", nombre);
        db.collection("usuario").document(email)
                .update(
                        "nombre", nombre,
                        "idioma", idioma_spinner
                        //,"favorito", favorito
                );
    }

    //Función para actualizar los datos de un registro
    public void eliminarBD(){
        db.collection("usuario").document(email)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
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
            LayoutInflater inflater = LayoutInflater.from(Perfil.this);
            view = inflater.inflate(R.layout.itemspinner, null);
            ImageView iv1 = view.findViewById(R.id.iv_bandera);
            iv1.setImageResource(banderas[i]);
            return view;
        }
    }

}