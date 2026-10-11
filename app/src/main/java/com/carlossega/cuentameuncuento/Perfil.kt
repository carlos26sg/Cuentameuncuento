package com.carlossega.cuentameuncuento

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.carlossega.cuentameuncuento.auth.AuthManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Perfil : AppCompatActivity() {

    private lateinit var txtPerfilNombre: TextView
    private lateinit var txtPerfilMail: TextView
    private lateinit var etNombre: EditText
    private lateinit var btnEliminar: Button
    private lateinit var btnCerrar: Button
    private lateinit var btnConfirmar: Button
    private lateinit var btnAtras: Button
    private lateinit var spIdioma: Spinner
    private lateinit var spModo: Spinner
    private lateinit var spFav: Spinner

    private val db = FirebaseFirestore.getInstance()
    private lateinit var authManager: AuthManager

    private var nombre: String = ""
    private var idioma: String = ""
    private var email: String = ""
    private var modoFav: String = ""
    private var cuentoFav: String = ""

    private val listaCuentos = ArrayList<SpinnerId>()

    companion object {
        private const val TAG = "Perfil"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Pantalla completa
        PantallaCompleta().pantallaCompleta(window.decorView)

        authManager = AuthManager(this)

        // Inicializar vistas
        initViews()

        // Configurar spinners
        setupSpinners()

        // Cargar datos del usuario
        loadUserData()

        // Configurar listeners
        setupListeners()
    }

    private fun initViews() {
        txtPerfilNombre = findViewById(R.id.txt_perfil_nombre)
        txtPerfilNombre.text = "Configuración"

        findViewById<TextView>(R.id.txt_perfil_opciones).text = "Preferencias"
        findViewById<TextView>(R.id.txt_perfil_idioma).text = getString(R.string.idioma)
        findViewById<TextView>(R.id.txt_perfil_email).text = getString(R.string.email)
        findViewById<TextView>(R.id.txt_modo_fav).text = getString(R.string.modo)
        findViewById<TextView>(R.id.txt_cuento_fav).text = getString(R.string.cuento_fav)

        txtPerfilMail = findViewById(R.id.txt_perfil_mail)
        etNombre = findViewById(R.id.et_perfil_nombre)

        // Ocultar campo de nombre (ya no es necesario)
        txtPerfilNombre.visibility = View.GONE
        etNombre.visibility = View.GONE

        btnEliminar = findViewById(R.id.btn_perfil_eliminar)
        btnEliminar.text = "Borrar datos"

        btnCerrar = findViewById(R.id.btn_perfil_cerrar)
        btnCerrar.visibility = View.GONE // Ocultar cerrar sesión (es anónimo)

        btnConfirmar = findViewById(R.id.btn_perfil_confirmar)
        btnConfirmar.text = getString(R.string.confirmar)

        btnAtras = findViewById(R.id.btn_perfil_atras)

        spIdioma = findViewById(R.id.sp_perfil_idioma)
        spFav = findViewById(R.id.sp_cuento_fav)
        spModo = findViewById(R.id.sp_modo_fav)
    }

    private fun setupSpinners() {
        // Spinner de idiomas con banderas
        val adaptadorBanderas = BanderaAdapter("idiomas", this)
        spIdioma.adapter = adaptadorBanderas

        // Spinner de modo (leer/reproducir)
        val adapterModo = ArrayAdapter.createFromResource(
            this,
            R.array.modo,
            R.layout.spinner_item_text
        )
        adapterModo.setDropDownViewResource(R.layout.spinner_item_dropdown)
        spModo.adapter = adapterModo

        // Cargar cuentos para spinner de favoritos
        cargarSpinnerCuentos()
    }

    private fun loadUserData() {
        // Obtener datos del usuario desde MenuPrincipal
        nombre = MenuPrincipal.usuario.nombre
        idioma = MenuPrincipal.usuario.idioma
        email = MenuPrincipal.usuario.mail
        modoFav = MenuPrincipal.usuario.modo_fav
        cuentoFav = MenuPrincipal.usuario.favorito

        etNombre.setText(nombre)
        txtPerfilMail.text = email

        // Establecer idioma en spinner
        when (idioma) {
            "esp" -> spIdioma.setSelection(0)
            "cat" -> spIdioma.setSelection(1)
            "eng" -> spIdioma.setSelection(2)
        }

        // Establecer modo favorito
        when (modoFav) {
            "" -> spModo.setSelection(0)
            "leer" -> spModo.setSelection(1)
            "reproducir" -> spModo.setSelection(2)
        }
    }

    private fun cargarSpinnerCuentos() {
        lifecycleScope.launch {
            try {
                // Agregar opción "sin selección" primero
                listaCuentos.add(SpinnerId("", getString(R.string.sin_seleccion)))

                val snapshot = db.collection("cuentos").get().await()

                for (document in snapshot.documents) {
                    try {
                        val titulo = document.getString("titulo_$idioma") ?: ""
                        val id = document.getString("id") ?: ""

                        if (titulo.isNotEmpty() && id.isNotEmpty()) {
                            listaCuentos.add(SpinnerId(id, titulo))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando cuento: ${e.message}")
                    }
                }

                // Crear adapter personalizado
                val adapter = ArrayAdapter(
                    this@Perfil,
                    R.layout.spinner_item_text,
                    listaCuentos.map { it.nombre }
                )
                adapter.setDropDownViewResource(R.layout.spinner_item_dropdown)
                spFav.adapter = adapter

                // Establecer cuento favorito si existe
                if (cuentoFav.isNotEmpty()) {
                    listaCuentos.forEachIndexed { index, spinnerId ->
                        if (spinnerId.nombre == cuentoFav) {
                            spFav.setSelection(index)
                        }
                    }
                }

                Log.d(TAG, "Cuentos cargados: ${listaCuentos.size}")

            } catch (e: Exception) {
                Log.e(TAG, "Error cargando cuentos: ${e.message}")
                Toast.makeText(this@Perfil, "Error cargando cuentos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        btnAtras.setOnClickListener {
            finish()
        }

        btnEliminar.setOnClickListener {
            mostrarDialogoEliminar()
        }

        btnCerrar.setOnClickListener {
            cerrarSesion()
        }

        btnConfirmar.setOnClickListener {
            guardarCambios()
        }

        spFav.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position < listaCuentos.size) {
                    cuentoFav = listaCuentos[position].nombre
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun guardarCambios() {
        val nuevoNombre = if (etNombre.visibility == View.VISIBLE) {
            etNombre.text.toString()
        } else {
            MenuPrincipal.usuario.nombre.ifEmpty { "Usuario" }
        }

        val idiomaSpinner = getIdiomaFromPosition(spIdioma.selectedItemPosition)
        val modoSpinner = when (spModo.selectedItemPosition) {
            1 -> "leer"
            2 -> "reproducir"
            else -> ""
        }

        lifecycleScope.launch {
            try {
                // Guardar con AuthManager
                val result = authManager.saveUserPreferences(
                    nombre = nuevoNombre,
                    idioma = idiomaSpinner,
                    favorito = cuentoFav,
                    modoFav = modoSpinner
                )

                result.onSuccess {
                    // Actualizar usuario en MenuPrincipal
                    MenuPrincipal.usuario.nombre = nuevoNombre
                    MenuPrincipal.usuario.idioma = idiomaSpinner
                    MenuPrincipal.usuario.modo_fav = modoSpinner
                    MenuPrincipal.usuario.favorito = cuentoFav

                    Toast.makeText(
                        this@Perfil,
                        "Perfil actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.d(TAG, "Perfil actualizado correctamente")
                }

                result.onFailure {
                    Toast.makeText(
                        this@Perfil,
                        "Error al guardar los cambios",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Error guardando perfil: ${it.message}")
                }

            } catch (e: Exception) {
                Toast.makeText(this@Perfil, "Error al guardar", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error: ${e.message}")
            }
        }
    }

    private fun mostrarDialogoEliminar() {
        AlertDialog.Builder(this)
            .setMessage("¿Estás seguro de que quieres eliminar tu cuenta?")
            .setTitle(R.string.eliminar_cuenta)
            .setPositiveButton(R.string.si) { _, _ ->
                eliminarCuenta()
            }
            .setNegativeButton(R.string.no, null)
            .create()
            .show()
    }

    private fun eliminarCuenta() {
        lifecycleScope.launch {
            try {
                val userId = authManager.getCurrentUserId()
                if (userId != null) {
                    db.collection("usuarios").document(userId).delete().await()
                    authManager.signOut()

                    Toast.makeText(
                        this@Perfil,
                        "Cuenta eliminada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Cerrar app
                    finishAffinity()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Perfil, "Error al eliminar cuenta", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error eliminando cuenta: ${e.message}")
            }
        }
    }

    private fun cerrarSesion() {
        authManager.signOut()

        // Resetear usuario
        MenuPrincipal.usuario = Usuario()

        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

        finish()
    }

    private fun getIdiomaFromPosition(position: Int): String {
        return when (position) {
            0 -> "esp"
            1 -> "cat"
            2 -> "eng"
            else -> "esp"
        }
    }
}
