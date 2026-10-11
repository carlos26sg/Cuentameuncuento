package com.carlossega.cuentameuncuento

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SeleccionCuento : AppCompatActivity() {

    private lateinit var btnAtras: Button
    private lateinit var tvSelecciona: TextView
    private lateinit var recyclerCuentos: RecyclerView
    private lateinit var spIdioma: Spinner

    private val listaCuentos = ArrayList<Cuento>()
    private lateinit var adapter: AdaptadorCuentos

    private var idioma: String = ""
    private var modo: String = ""
    private var idCuento: String = ""

    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "SeleccionCuento"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_cuento)

        // Pantalla completa
        PantallaCompleta().pantallaCompleta(window.decorView)

        // Recibir parámetros
        intent.extras?.let { extras ->
            idioma = extras.getString("idioma") ?: "esp"
            modo = extras.getString("modo") ?: "leer"
        }

        Log.d(TAG, "Modo: $modo, Idioma: $idioma")

        // Inicializar vistas
        initViews()

        // Configurar RecyclerView
        setupRecyclerView()

        // Configurar listeners
        setupListeners()
    }

    private fun initViews() {
        btnAtras = findViewById(R.id.btn_atras)
        spIdioma = findViewById(R.id.sp_seleccion_idioma)
        tvSelecciona = findViewById(R.id.tv_selecciona)

        tvSelecciona.text = getString(R.string.selecciona)
    }

    private fun setupRecyclerView() {
        recyclerCuentos = findViewById(R.id.rv_lista_cuentos)
        recyclerCuentos.layoutManager = LinearLayoutManager(this)

        // Inicializar adaptador
        adapter = AdaptadorCuentos(listaCuentos)
        recyclerCuentos.adapter = adapter

        // Click del adaptador
        adapter.setOnClickListener { view ->
            try {
                idCuento = listaCuentos[recyclerCuentos.getChildAdapterPosition(view)].id
                abrirReproductorCuento()
            } catch (e: IndexOutOfBoundsException) {
                Log.e(TAG, "Error al obtener cuento: ${e.message}")
            }
        }
    }

    private fun setupListeners() {
        btnAtras.setOnClickListener {
            finish()
        }

        // Configurar spinner de idiomas
        val adaptadorBanderas = BanderaAdapter("idiomas", this)
        spIdioma.adapter = adaptadorBanderas

        // Establecer idioma inicial
        when (idioma) {
            "esp" -> spIdioma.setSelection(0)
            "cat" -> spIdioma.setSelection(1)
            "eng" -> spIdioma.setSelection(2)
        }

        spIdioma.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                idioma = getIdiomaFromPosition(position)
                listaCuentos.clear()
                cargarCuentos()
                Log.d(TAG, "Idioma seleccionado: $idioma")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    private fun getIdiomaFromPosition(position: Int): String {
        return when (position) {
            0 -> "esp"
            1 -> "cat"
            2 -> "eng"
            else -> "esp"
        }
    }

    private fun cargarCuentos() {
        lifecycleScope.launch {
            try {
                val snapshot = db.collection("cuentos")
                    .get()
                    .await()

                for (document in snapshot.documents) {
                    try {
                        val titulo = document.getString("titulo_$idioma") ?: ""
                        val descripcion = document.getString("desc_$idioma") ?: ""
                        val imagen = document.getString("imagen") ?: ""
                        val id = document.getString("id") ?: ""

                        if (titulo.isNotEmpty() && id.isNotEmpty()) {
                            listaCuentos.add(Cuento(titulo, descripcion, imagen, id))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando documento: ${e.message}")
                    }
                }

                adapter.notifyDataSetChanged()
                Log.d(TAG, "Cargados ${listaCuentos.size} cuentos")

            } catch (e: Exception) {
                Log.e(TAG, "Error cargando cuentos: ${e.message}")
            }
        }
    }

    private fun abrirReproductorCuento() {
        val intent = Intent(this, ReproductorCuento::class.java).apply {
            putExtra("modo", modo)
            putExtra("idioma", idioma)
            putExtra("cuento", idCuento)
        }
        startActivity(intent)
        Log.d(TAG, "Abriendo cuento: $idCuento, modo: $modo, idioma: $idioma")
    }

    override fun onStart() {
        super.onStart()
        // Cargar cuentos al iniciar
        cargarCuentos()
    }
}
