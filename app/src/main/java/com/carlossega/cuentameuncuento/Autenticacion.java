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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Autenticacion extends AppCompatActivity implements Serializable {

    //Iniciamos las variables con las que trabajamos
    private TextView mail, password, repassword, repite;
    private Button volver, confirmar;
    private CheckBox mantener;
    private String message, pass, repass, email, secretKey, nombre;
    //Instanciamos la Base de datos con la que trabajamos
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacion);
        //Hacemos llamada a clase para mostrar la pantalla completa
        PantallaCompleta pantallaCompleta = new PantallaCompleta();
        View decorView = getWindow().getDecorView();
        pantallaCompleta.pantallaCompleta(decorView);

        //Asiganmos controles a las variables
        mail = (EditText) findViewById(R.id.et_mail);
        password = (EditText) findViewById(R.id.et_password);
        repassword = (EditText) findViewById(R.id.et_repassword);
        repite = (TextView) findViewById(R.id.tv_repite);
        volver = (Button) findViewById(R.id.btn_volver);
        confirmar = (Button) findViewById(R.id.btn_confirmar);
        mantener = (CheckBox) findViewById(R.id.cb_mantener_inicio);
        mantener.setText("Mantener la sesi??n iniciada");
        //Generamos la clave con la que encriptaremos y desencriptaremos la contrase??a
        secretKey = "D???sCiFR@rP@S$WoRd.";

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        volver.setText(getString(R.string.atras));
        if (message.equals("login")){
            repassword.setVisibility(View.GONE);
            repite.setVisibility(View.GONE);
            confirmar.setText("Iniciar");
        } else if (message.equals("register")){
            mantener.setVisibility(View.GONE);
            confirmar.setText("Registrarse");
        }
    }

    //Funci??n asignada al bot??n volver
    public void volver (View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    //Funci??n asignada al bot??n confirmar
    public void confirmar (View view){
        //Recogemos en String el contenido de los TextView
        pass = password.getText().toString();
        email = mail.getText().toString();
        repass = repassword.getText().toString();
        //Dependiendo de si es login o register haremos una cosa u otra
        if (message.equals("login")){
            //Leemos documento de la base de datos
            //Buscamos un documento que se llame como el email introducido
            DocumentReference docRef = db.collection("usuario").document(email);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        //Si el documento existe
                        if (document.exists()) {
                            String pass_descifrar;
                            pass_descifrar = document.get("pass").toString();
                            if (descifrar(pass_descifrar, secretKey).equals(pass)){
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Se ha accedido correctamente", Toast.LENGTH_LONG);
                                toast.show();
                                nombre = document.get("nombre").toString();
                                if (mantener.isChecked()){guardarPreferencias();}
                                Bundle extras = new Bundle();
                                extras.putString("mail",document.get("mail").toString());
                                extras.putString("nombre", nombre);
                                extras.putString("idioma", document.get("idioma").toString());
                                extras.putString("favorito", document.get("favorito").toString());

                                Intent intent = new Intent(Autenticacion.this, MenuPrincipal.class);
                                //Agrega el objeto bundle al Intent
                                intent.putExtras(extras);
                                startActivity(intent);
                                //Inicia Activity
                                finish();
                                SharedPreferences pref = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                                SharedPreferences.Editor edit = pref.edit();
                                edit.putBoolean("iniciada", true);
                                edit.commit();
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Contrase??a incorrecta. Comprueba la contrase??a.", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        //Si el documento no existe
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Error al intentar acceder. No se ha encontrado el mail.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        } else if (message.equals("register")){
            //Nos aseguramos que la contrase??a se ha introducido dos veces igual
            if (pass.equals(repass)){
                //Comprobamos las funciones para ver que todo sea correcto
                if (esPasswordValido(pass) && !email.isEmpty() && esEmailValido(email)){
                    //Buscamos en la base de datos un documento con ese email
                    DocumentReference docRef = db.collection("usuario").document(email);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                //Si ya se ha creado en la bd ese mail se muestra Toast
                                if (document.exists()) {
                                    Toast toast = Toast.makeText(getApplicationContext(),
                                            "Ya existe una cuenta con ese email", Toast.LENGTH_LONG);
                                    toast.show();
                                //Si no hay registros de ese mail se crear?? uno nuevo
                                } else {
                                    String cifrado = cifradopass(pass, secretKey);
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("mail", email);
                                    user.put("pass", cifrado);
                                    user.put("nombre", "");
                                    user.put("favorito", "");
                                    user.put("idioma", "esp");
                                    db.collection("usuario").document(email)
                                            .set(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                //Mostramos en Toast que que ha ido bien
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast toast = Toast.makeText(getApplicationContext(),
                                                            "Cuenta creada con exito", Toast.LENGTH_LONG);
                                                    toast.show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error writing document", e);
                                                }
                                            });
                                    Intent intent = new Intent(Autenticacion.this, MainActivity.class);
                                    startActivity(intent);
                                    //Inicia Activity
                                    finish();
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "La contrase??a debe tener entre 8 y 20 caracteres y contener al" +
                                    " menos may??sculas, min??sculas y n??meros", Toast.LENGTH_LONG);
                    toast.show();
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Las contrase??as no coinciden", Toast.LENGTH_LONG);
                toast.show();
            }
        }

    }

    private void guardarPreferencias(){
        SharedPreferences preferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user", email);
        editor.putString("favorito", "");
        editor.putString("nombre", nombre);
        editor.putString("idioma", "esp");
        editor.putBoolean("iniciada", true);
        editor.commit();
    }

    //Funci??n que cifra la contrase??a para guardarla en la base de datos
    private String cifradopass(String userPass, String secretKey){
        String pass_cifrado ="";
        try {
            //Cifrado MD5
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] llavePass = md5.digest(secretKey.getBytes("utf-8"));
            byte[] bytesKey = Arrays.copyOf(llavePass, 24);
            SecretKey key = new SecretKeySpec(bytesKey, "DESede");
            //Inicializamos objeto Cipher con la clave
            Cipher c = Cipher.getInstance("DESede");
            //Iniciamos Cipher en modo Encriptacion
            c.init(Cipher.ENCRYPT_MODE, key);

            //Pasamos la contrase??a del usuario a un array de bytes
            byte[] plainTextBytes = userPass.getBytes("utf-8");
            //Encriptamos la contrase??a en bytes
            byte[] cifrado = c.doFinal(plainTextBytes);
            // Convertimos el byte cifrado [] a base64 para guardarlo en la base de datos
            byte [] base64Bytes = Base64.getEncoder().encode(cifrado);
            pass_cifrado = new String(base64Bytes);
        } catch (Exception e){
            e.printStackTrace();
        }
        return pass_cifrado;
    }

    //Funci??n que descifra la contrase??a que se encuentra cifrada en la base de datos
    private String descifrar(String passDescifrar, String secretKey) {
        String descifrada= "";
        try {
            //Recogemos la contrase??a guardada en la base de datos y la decodificamos en Base64
            byte[] mensaje = Base64.getDecoder().decode(passDescifrar.getBytes("utf-8"));
            //Cifrado MD5
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            //Hacemos el descifrado con la clave
            byte[] digestOfPassword = md5.digest(secretKey.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            SecretKey key = new SecretKeySpec(keyBytes, "DESede");

            //Creamos objeto Cipher con algoritmo DES
            Cipher desc = Cipher.getInstance("DESede");
            //Iniciamos Cipher en Desencriptaci??n
            desc.init(Cipher.DECRYPT_MODE, key);
            byte[] plainText = desc.doFinal(mensaje);
            //Pasamos de bytes a String
            descifrada = new String(plainText, "UTF-8");

        }catch (Exception e){
            e.printStackTrace();
        }
        return descifrada;
    }

    //Metodo para validar el password
    public static boolean esPasswordValido(String password)
    {
        // Regex para chequear el patron.
        String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=\\S+$).{8,20}$";

        // Compila ReGex
        Pattern p = Pattern.compile(regex);
        // Si el password est?? vac??o se devuelve false
        if (password == null) {
            return false;
        }
        //Buscamos si es igual el password a la expresi??n regular
        Matcher m = p.matcher(password);
        // Retorna si el password es igual a Regex
        return m.matches();
    }

    //M??todo que nos valida un email
    public boolean esEmailValido(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\." +
                "[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

}