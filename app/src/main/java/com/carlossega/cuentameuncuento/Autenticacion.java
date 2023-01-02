package com.carlossega.cuentameuncuento;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.Provider;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Autenticacion extends AppCompatActivity {

    private TextView mail, password, repassword;
    private TextView repite;
    private Button volver, confirmar, google;
    private String message;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autenticacion);
        getSupportActionBar().hide();
        mail = (EditText) findViewById(R.id.et_mail);
        password = (EditText) findViewById(R.id.et_password);
        repassword = (EditText) findViewById(R.id.et_repassword);
        repite = (TextView) findViewById(R.id.tv_repite);
        volver = (Button) findViewById(R.id.btn_volver);
        confirmar = (Button) findViewById(R.id.btn_confirmar);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        volver.setText("Volver");
        if (message.equals("login")){
            repassword.setVisibility(View.GONE);
            repite.setVisibility(View.GONE);
            confirmar.setText("Iniciar");
        } else if (message.equals("register")){
            confirmar.setText("Registrarse");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //reload();
        }
    }

    public void volver (View view){
        this.finish();
    }

    public void confirmar (View view){
        String pass = password.getText().toString();
        String repass = repassword.getText().toString();
        String email = mail.getText().toString();
        if (message.equals("login")){
            mAuth.getInstance().signInWithEmailAndPassword(email, pass).
                    addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Se ha accedido correctamente", Toast.LENGTH_LONG);
                                toast.show();
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Autenticacion.this, "Error." + task.getException(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else if (message.equals("register")){
            if (pass.equals(repass)){
                if (esPasswordValido(pass) && !email.isEmpty() && esEmailValido(email)){
                    mAuth.getInstance().createUserWithEmailAndPassword(email, pass).
                            addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Cuenta creada con exito", Toast.LENGTH_LONG);
                                toast.show();
                                SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("email", email);
                                editor.putString("provider", "BASIC");
                                editor.commit();
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Autenticacion.this, "Error." + task.getException(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "La contraseña debe tener entre 8 y 20 caracteres y contener al" +
                                    " menos mayúsculas, minúsculas y números", Toast.LENGTH_LONG);
                    toast.show();
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Las contraseñas no coinciden", Toast.LENGTH_LONG);
                toast.show();
            }
        }

        //Para borrar con logout
        //editor.clear();
        //editor.commit();

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

    //Metodo para validar el password
    public static boolean esPasswordValido(String password)
    {
        // Regex to check valid password.
        String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=\\S+$).{8,20}$";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the password is empty
        // return false
        if (password == null) {
            return false;
        }

        // Pattern class contains matcher() method
        // to find matching between given password
        // and regular expression.
        Matcher m = p.matcher(password);

        // Return if the password
        // matched the ReGex
        return m.matches();
    }

    //Método que nos valida un email
    public boolean esEmailValido(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

}