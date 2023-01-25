package com.carlossega.cuentameuncuento;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Perfil extends AppCompatActivity {

    //Indicamos las variables necesarias
    TextView txt_perfil_nombre, txt_perfil_opciones, txt_perfil_idioma,
        txt_perfil_mail, txt_perfil_email, txt_modo_fav, txt_cuento_fav;
    EditText et_nombre;
    Button btn_eliminar, btn_cerrar, btn_confirmar, btn_atras;
    Spinner sp_idioma, sp_modo, sp_fav;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String nombre, idioma, email, idioma_sp, modo, cuento_fav, modo_sp, cuento_sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        //Hacemos llamada a clase para mostrar la pantalla completa
        PantallaCompleta pantallaCompleta = new PantallaCompleta();
        View decorView = getWindow().getDecorView();
        pantallaCompleta.pantallaCompleta(decorView);

        //Asociamos todos los componentes al ID que le corresponde
        txt_perfil_nombre = findViewById(R.id.txt_perfil_nombre);
        txt_perfil_nombre.setText(getString(R.string.nombre));
        txt_perfil_opciones = findViewById(R.id.txt_perfil_opciones);
        txt_perfil_opciones.setText(getString(R.string.opciones_perfil));
        txt_perfil_idioma = findViewById(R.id.txt_perfil_idioma);
        txt_perfil_idioma.setText(getString(R.string.idioma));
        txt_perfil_email = findViewById(R.id.txt_perfil_email);
        txt_perfil_email.setText(getString(R.string.email));
        txt_perfil_mail = findViewById(R.id.txt_perfil_mail);
        btn_eliminar = findViewById(R.id.btn_perfil_eliminar);
        btn_eliminar.setText(getString(R.string.eliminar_cuenta));
        btn_cerrar = findViewById(R.id.btn_perfil_cerrar);
        btn_cerrar.setText(getString(R.string.cerrar_sesion));
        btn_confirmar = findViewById(R.id.btn_perfil_confirmar);
        btn_confirmar.setText(getString(R.string.confirmar));
        txt_modo_fav = findViewById(R.id.txt_modo_fav);
        txt_modo_fav.setText(R.string.modo);
        txt_cuento_fav = findViewById(R.id.txt_cuento_fav);
        txt_cuento_fav.setText(R.string.cuento_fav);
        btn_atras = findViewById(R.id.btn_perfil_atras);
        et_nombre = findViewById(R.id.et_perfil_nombre);
        sp_idioma = findViewById(R.id.sp_perfil_idioma);
        sp_fav = findViewById(R.id.sp_cuento_fav);
        sp_modo = findViewById(R.id.sp_modo_fav);

        //Iniciamos adaptador para el spinner
        BanderaAdapter adaptador = new BanderaAdapter("idiomas", this);
        sp_idioma.setAdapter(adaptador);
        // Creamos ArrayAdapter usando array spinner por defecto
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.modo, R.layout.spinner_item_text);
        adapter.setDropDownViewResource(R.layout.spinner_item_dropdown);
        sp_modo.setAdapter(adapter);
        //Cargamos lista de cuentos
        spinnerCuentos();

        //Buscamos la info del usuario
        nombre = MenuPrincipal.usuario.getNombre();
        idioma = MenuPrincipal.usuario.getIdioma();
        email = MenuPrincipal.usuario.getMail();
        modo = MenuPrincipal.usuario.getModo_fav();
        cuento_fav = MenuPrincipal.usuario.getFavorito();
        et_nombre.setText(nombre);
        txt_perfil_mail.setText(email);
        if (idioma.equals("esp")){sp_idioma.setSelection(0);}
        if (idioma.equals("cat")){sp_idioma.setSelection(1);}
        if (idioma.equals("eng")){sp_idioma.setSelection(2);}
        //Seleccionamos modo favorito
        if (modo.equals("")){
           sp_modo.setSelection(0);
        } else {
            switch (modo){
                case "leer":
                    sp_modo.setSelection(1);
                    break;
                case "repro":
                    sp_modo.setSelection(2);
                    break;
            }
        }

        //Listeners de los botones
        btn_atras.setOnClickListener(view -> finish());

        btn_confirmar.setOnClickListener(view -> {
            modo_sp = checkModofavorito();
            idioma_sp = selectedIdioma();
            guardarBD(et_nombre.getText().toString(), idioma_sp, modo_sp, cuento_fav);
            SharedPreferences preferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("nombre", et_nombre.getText().toString());
            editor.putString("idioma", idioma_sp);
            editor.commit();
            finish();
        });

        //Borramos los datos de SharedPreferences para cerrar sesión del usuario
        btn_cerrar.setOnClickListener(view -> {
            MenuPrincipal.usuario.setNombre("NombreDefecto");
            SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.commit();
            finish();
        });

        btn_eliminar.setOnClickListener(view -> {
            //Se añade alerta para borrar
            AlertDialog.Builder builder = new AlertDialog.Builder(Perfil.this);
            builder.setMessage(R.string.seguro)
                    .setTitle(R.string.borrar);
            //Se añaden los botones
            builder.setPositiveButton(R.string.si, (dialog, id) -> {
                SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.commit();
                MenuPrincipal.usuario.setNombre("NombreDefecto");
                eliminarBD();
                finish();
            });
            builder.setNegativeButton(R.string.no, (dialog, id) -> {
            });
            // Creamos el alertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
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
    public void guardarBD(String nombre, String idioma_spinner, String m_fav, String c_fav){
        db.collection("usuario").document(email)
                .update(
                        "nombre", nombre,
                        "idioma", idioma_spinner
                        //"modo_fav", m_fav,
                        //"favorito", c_fav
                );
        MenuPrincipal.usuario.setNombre(nombre);
        MenuPrincipal.usuario.setIdioma(idioma_spinner);
        Toast toast = Toast.makeText(getApplicationContext(),
                "Cuenta actualizada correctamente", Toast.LENGTH_LONG);
        toast.show();
        Log.d(TAG, "datos actualizados correctamente");
    }

    //Función para actualizar los datos de un registro
    public void eliminarBD(){
        db.collection("usuario").document(email)
                .delete()
                .addOnSuccessListener(aVoid -> {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Cuenta eliminada correctamente", Toast.LENGTH_LONG);
                            toast.show();
                            Log.d(TAG, "Documento borrado correctamente!");
                        }
                )
                .addOnFailureListener(e -> Log.w(TAG, "Error borrando documento", e));
    }

    private String checkModofavorito(){
        String result = "";
        if (sp_modo.getSelectedItemPosition()==0){result = "";}
        if (sp_modo.getSelectedItemPosition()==1){result = "leer";}
        if (sp_modo.getSelectedItemPosition()==2){result = "repro";}
        return result;
    }

    //Cargamos en spinner la lista de cuentos disponibles
    public void spinnerCuentos(){
        List<SpinnerId> lista = new ArrayList<SpinnerId>();
        lista.add(new SpinnerId("Sin selección", ""));
        //Realizamos la consulta de todos los documentos de la colección cuentos
        db.collection("cuentos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            //Buscamos en la BD y añadimos cada campo de los cuentos a la lista
                            try {
                                lista.add(new SpinnerId(document.get("titulo_" + idioma).toString(),
                                        document.get("id").toString()));
                            } catch (NullPointerException e){
                                Log.d(TAG, "error : " + e);
                            }
                        }
                        Log.d(TAG, "datos recogidos ");
                        // Creamos ArrayAdapter usando array spinner por defecto
                        ArrayAdapter<SpinnerId> adap = new ArrayAdapter<>(this,
                                R.layout.spinner_item_text, lista);
                        adap.setDropDownViewResource(R.layout.spinner_item_dropdown);
                        sp_fav.setAdapter(adap);
                        sp_fav.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                SpinnerId s = (SpinnerId) parent.getItemAtPosition(position);
                                cuento_fav = s.nombre;
                                Log.d(TAG, "seleccionado: " + cuento_fav);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }
}