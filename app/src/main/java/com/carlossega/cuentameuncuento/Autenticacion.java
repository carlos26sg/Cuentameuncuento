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
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
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
    private TextView mail, password, repassword;
    private Button volver;
    private CheckBox mantener;
    private String message, pass, email, secretKey, nombre, idioma;
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
        mail = findViewById(R.id.et_mail);
        password = findViewById(R.id.et_password);
        repassword = findViewById(R.id.et_repassword);
        TextView repite = findViewById(R.id.tv_repite);
        volver = findViewById(R.id.btn_volver);
        Button confirmar = findViewById(R.id.btn_confirmar);
        mantener = findViewById(R.id.cb_mantener_inicio);

        //Damos valor a idioma y establecemos texto de los TextView y botones
        mantener.setText(R.string.mantener_sesion);
        repite.setText(R.string.repass);
        volver.setText(getString(R.string.atras));
        confirmar.setText(R.string.registrarse);
        idioma = selectedIdioma();

        //Generamos la clave con la que encriptaremos y desencriptaremos la contraseña
        secretKey = "D€sCiFR@rP@S$WoRd.";

        //Depende del mensaje pasado la pantalla hará función de login o registro
        Intent intent = getIntent();
        message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        if (message.equals("login")){
            repassword.setVisibility(View.GONE);
            repite.setVisibility(View.GONE);
            confirmar.setText(R.string.iniciar_sesion);
        } else if (message.equals("register")){
            mantener.setVisibility(View.GONE);
            confirmar.setText(R.string.registrarse);
        }

        //Listener de boton volver
        volver.setOnClickListener(view -> {
            //Cerramos conexión con BD para evitar errores
            db.clearPersistence();
            db.terminate();
            //Cerramos esta activity y abrimos MainActivity
            Intent intent1 = new Intent(Autenticacion.this, MainActivity.class);
            startActivity(intent1);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        //Cerramos conexión con BD para evitar errores
        db.clearPersistence();
        db.terminate();
        super.onDestroy();
    }

    //Función asignada al botón confirmar
    public void confirmar (View view){
        //Recogemos en String el contenido de los TextView
        pass = password.getText().toString();
        email = mail.getText().toString();
        String repass = repassword.getText().toString();
        //Dependiendo de si es login o register haremos una cosa u otra
        if (message.equals("login")){
            //Leemos documento de la base de datos
            //Buscamos un documento que se llame como el email introducido
            DocumentReference docRef = db.collection("usuario").document(email);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    //Si el documento existe
                    if (document.exists()) {
                        String pass_descifrar;
                        pass_descifrar = document.get("pass").toString();
                        if (descifrar(pass_descifrar, secretKey).equals(pass)){
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.acceso_correcto), Toast.LENGTH_LONG);
                            toast.show();
                            nombre = document.get("nombre").toString();
                            if (mantener.isChecked()){guardarPreferencias();}
                            //Info que se guarda para pasar a MenuPrincipal
                            Bundle extras = new Bundle();
                            extras.putString("mail",document.get("mail").toString());
                            //Guardamos en SharedPreferences que iniciamos sesión
                            SharedPreferences pref = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putBoolean("iniciada", true);
                            edit.commit();
                            Log.d(TAG, "mail y contraseña correcta, se accede a MenuPrincipal");
                            //Cerramos conexión con BD para evitar errores
                            db.clearPersistence();
                            db.terminate();
                            //Agrega el objeto bundle al Intent e inicia Activity MenuPrincipal y cierra Autenticacion
                            Intent intent = new Intent(Autenticacion.this, MenuPrincipal.class);
                            intent.putExtras(extras);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    getString(R.string.pass_incorrecto), Toast.LENGTH_LONG);
                            toast.show();
                            Log.d(TAG, "contraseña incorrecta. No se ha podido acceder");
                        }
                    //Si el documento no existe
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                getString(R.string.mail_no_encontrado), Toast.LENGTH_LONG);
                        toast.show();
                        Log.d(TAG, "mail no encontrado. No se ha podido acceder");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });
        } else if (message.equals("register")){
            //Nos aseguramos que la contraseña se ha introducido dos veces igual
            if (pass.equals(repass)){
                //Comprobamos las funciones para ver que todo sea correcto
                if (esPasswordValido(pass) && !email.isEmpty() && esEmailValido(email)){
                    //Buscamos en la base de datos un documento con ese email
                    DocumentReference docRef = db.collection("usuario").document(email);
                    docRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            //Si ya se ha creado en la bd ese mail se muestra Toast
                            if (document.exists()) {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        getString(R.string.ya_existe), Toast.LENGTH_LONG);
                                toast.show();
                                Log.d(TAG, "no se puede crear cuenta, ya existe mail en database");
                            //Si no hay registros de ese mail se creará uno nuevo
                            } else {
                                String cifrado = cifradopass(pass, secretKey);
                                Map<String, Object> user = new HashMap<>();
                                user.put("mail", email);
                                user.put("pass", cifrado);
                                user.put("nombre", "");
                                user.put("favorito", "");
                                user.put("idioma", idioma);
                                user.put("modo_fav", "");
                                //Mostramos en Toast que que ha ido bien
                                db.collection("usuario").document(email)
                                        .set(user)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast toast = Toast.makeText(getApplicationContext(),
                                                    getString(R.string.cuenta_exito), Toast.LENGTH_LONG);
                                            toast.show();
                                            Log.d(TAG, "se crea cuenta con exito");
                                        })
                                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                                //Cerramos conexión con BD para evitar errores
                                db.clearPersistence();
                                db.terminate();
                                //Inicia MainActivity y cierra Autenticacion
                                Intent intent = new Intent(Autenticacion.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    });
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getString(R.string.condiciones), Toast.LENGTH_LONG);
                    toast.show();
                    Log.d(TAG, "la contraseña no respeta el patron");
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        getString(R.string.pass_no_coincide), Toast.LENGTH_LONG);
                toast.show();
                Log.d(TAG, "las contraseñas no coinciden");
            }
        }
    }

    //Si se marca la casilla de mantener sesión iniciada, guardamos datos en SharedPreferences
    private void guardarPreferencias(){
        SharedPreferences preferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("mail", email);
        editor.putString("nombre", nombre);
        editor.putString("idioma", idioma);
        editor.commit();
    }

    //Función que cifra la contraseña para guardarla en la base de datos
    private String cifradopass(String userPass, String secretKey){
        String pass_cifrado ="";
        try {
            //Cifrado MD5
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] llavePass = md5.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            byte[] bytesKey = Arrays.copyOf(llavePass, 24);
            SecretKey key = new SecretKeySpec(bytesKey, "DESede");
            //Inicializamos objeto Cipher con la clave
            Cipher c = Cipher.getInstance("DESede");
            //Iniciamos Cipher en modo Encriptacion
            c.init(Cipher.ENCRYPT_MODE, key);

            //Pasamos la contraseña del usuario a un array de bytes
            byte[] plainTextBytes = userPass.getBytes(StandardCharsets.UTF_8);
            //Encriptamos la contraseña en bytes
            byte[] cifrado = c.doFinal(plainTextBytes);
            // Convertimos el byte cifrado [] a base64 para guardarlo en la base de datos
            byte [] base64Bytes = Base64.getEncoder().encode(cifrado);
            pass_cifrado = new String(base64Bytes);
        } catch (Exception e){
            e.printStackTrace();
        }
        return pass_cifrado;
    }

    //Función que descifra la contraseña que se encuentra cifrada en la base de datos
    private String descifrar(String passDescifrar, String secretKey) {
        String descifrada= "";
        try {
            //Recogemos la contraseña guardada en la base de datos y la decodificamos en Base64
            byte[] mensaje = Base64.getDecoder().decode(passDescifrar.getBytes(StandardCharsets.UTF_8));
            //Cifrado MD5
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            //Hacemos el descifrado con la clave
            byte[] digestOfPassword = md5.digest(secretKey.getBytes(StandardCharsets.UTF_8));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            SecretKey key = new SecretKeySpec(keyBytes, "DESede");

            //Creamos objeto Cipher con algoritmo DES
            Cipher desc = Cipher.getInstance("DESede");
            //Iniciamos Cipher en Desencriptación
            desc.init(Cipher.DECRYPT_MODE, key);
            byte[] plainText = desc.doFinal(mensaje);
            //Pasamos de bytes a String
            descifrada = new String(plainText, StandardCharsets.UTF_8);

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
        // Si el password está vacío se devuelve false
        if (password == null) {
            return false;
        }
        //Buscamos si es igual el password a la expresión regular
        Matcher m = p.matcher(password);
        // Retorna si el password es igual a Regex
        return m.matches();
    }

    //Método que nos valida un email
    public boolean esEmailValido(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\." +
                "[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    //Miramos idioma del usuario
    private String selectedIdioma() {
        String result = "";
        if(volver.getText().equals("Atras")){result = "esp";}
        if(volver.getText().equals("Enrere")){result = "cat";}
        if(volver.getText().equals("Back")){result = "eng";}
        return result;
    }

}