package com.carlossega.cuentameuncuento

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class ReproductorCuento : AppCompatActivity() {

    // UI Components
    private lateinit var btnMusica: Button
    private lateinit var btnSinMusica: Button
    private lateinit var btnPlay: Button
    private lateinit var btnPause: Button
    private lateinit var btnSalir: Button
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button
    private lateinit var spBanderas: Spinner
    private lateinit var tvSeleccionado: TextView
    private lateinit var tvTraducido: TextView
    private lateinit var ivBandera: ImageView
    private lateinit var llFondo: LinearLayout

    // Variables de estado
    private var idioma: String = ""
    private var modo: String = ""
    private var nombreCuento: String = ""
    private var urlCuento: String = ""
    private var urlTraducido: String = ""
    private var urlImg: String = ""
    private var idiomaTraduccion: String = ""
    private var contadorLineas: Int = 0
    private var lineasCuento: Int = 0
    private var traducidoCreado: Boolean = false

    private val cuento = ArrayList<String>()
    private val cuentoTraducido = ArrayList<String>()

    private val db = FirebaseFirestore.getInstance()
    private var tts: TextToSpeech? = null

    companion object {
        private const val TAG = "ReproductorCuento"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reproductor_cuento)

        // Pantalla completa y mantener encendida
        PantallaCompleta().pantallaCompleta(window.decorView)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Inicializar vistas
        initViews()

        // Configurar listeners
        setupListeners()
    }

    private fun initViews() {
        btnMusica = findViewById(R.id.btn_repro_musica)
        btnSinMusica = findViewById(R.id.btn_repro_sin_sonido)
        btnPlay = findViewById(R.id.btn_repro_play)
        btnPause = findViewById(R.id.btn_repro_pause)
        btnSalir = findViewById(R.id.btn_repro_salir)
        spBanderas = findViewById(R.id.sp_repro_traduccion)
        ivBandera = findViewById(R.id.img_bandera_seleccion)
        tvSeleccionado = findViewById(R.id.txt_repro_cuento)
        tvTraducido = findViewById(R.id.txt_repro_traducido)
        btnNext = findViewById(R.id.btn_repro_next)
        btnBack = findViewById(R.id.btn_repro_back)
        llFondo = findViewById(R.id.llRepro)
    }

    private fun setupListeners() {
        btnSalir.setOnClickListener {
            mostrarDialogoSalir()
        }

        spBanderas.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    idiomaTraduccion = getIdiomaFromPosition(position)
                    cargarCuentoTraducido()
                } else {
                    tvTraducido.text = ""
                    traducidoCreado = false
                }
                Log.d(TAG, "Idioma de traducción seleccionado: $idiomaTraduccion")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnNext.setOnClickListener {
            avanzarLinea()
        }

        btnBack.setOnClickListener {
            retrocederLinea()
        }

        btnPlay.setOnClickListener {
            btnPlay.visibility = View.GONE
            btnPause.visibility = View.VISIBLE
            iniciarTextToSpeech()
            Log.d(TAG, "Play presionado")
        }

        btnPause.setOnClickListener {
            btnPlay.visibility = View.VISIBLE
            btnPause.visibility = View.GONE
            tts?.stop()
            tts?.shutdown()
            Log.d(TAG, "Pause presionado")
        }

        btnMusica.setOnClickListener {
            MenuPrincipal.mp?.pause()
            btnMusica.visibility = View.GONE
            btnSinMusica.visibility = View.VISIBLE
            Log.d(TAG, "Música pausada")
        }

        btnSinMusica.setOnClickListener {
            MenuPrincipal.mp?.start()
            btnSinMusica.visibility = View.GONE
            btnMusica.visibility = View.VISIBLE
            Log.d(TAG, "Música reanudada")
        }
    }

    override fun onStart() {
        super.onStart()

        // Recibir parámetros
        intent.extras?.let { extras ->
            idioma = extras.getString("idioma") ?: "esp"
            modo = extras.getString("modo") ?: "leer"
            nombreCuento = extras.getString("cuento") ?: ""
        }

        Log.d(TAG, "Modo: $modo, Idioma: $idioma, Cuento: $nombreCuento")

        // Configurar UI según el modo
        configurarModo()

        // Configurar spinner de idiomas
        val adaptadorBanderas = BanderaAdapter("opcion_sin", this)
        spBanderas.adapter = adaptadorBanderas
        spBanderas.setSelection(0)

        // Cargar cuento
        cargarCuento()

        // Configurar estado de música
        configurarEstadoMusica()

        // Establecer bandera según idioma
        establecerBandera()

        // Reanudar música si está activa
        MenuPrincipal.btn_musica?.let {
            if (it.visibility == View.VISIBLE) {
                MenuPrincipal.mp?.start()
            }
        }
    }

    private fun configurarModo() {
        when (modo) {
            "leer" -> {
                btnNext.visibility = View.VISIBLE
                ivBandera.visibility = View.GONE
                btnPlay.visibility = View.GONE
                btnBack.visibility = View.VISIBLE
            }
            "reproducir" -> {
                btnNext.visibility = View.GONE
                ivBandera.visibility = View.VISIBLE
                btnPlay.visibility = View.VISIBLE
                btnBack.visibility = View.GONE
            }
        }
    }

    private fun configurarEstadoMusica() {
        MenuPrincipal.mp?.let { mp ->
            if (mp.isPlaying) {
                btnMusica.visibility = View.VISIBLE
                btnSinMusica.visibility = View.GONE
                MenuPrincipal.btn_musica?.visibility = View.VISIBLE
                MenuPrincipal.btn_sin_sonido?.visibility = View.GONE
            } else {
                btnMusica.visibility = View.GONE
                btnSinMusica.visibility = View.VISIBLE
                MenuPrincipal.btn_musica?.visibility = View.GONE
                MenuPrincipal.btn_sin_sonido?.visibility = View.VISIBLE
            }
        }
    }

    private fun establecerBandera() {
        when (idioma) {
            "esp" -> ivBandera.setImageResource(R.drawable.espanol)
            "cat" -> ivBandera.setImageResource(R.drawable.catalan)
            "eng" -> ivBandera.setImageResource(R.drawable.ingles)
        }
    }

    private fun cargarCuento() {
        lifecycleScope.launch {
            try {
                // Obtener URL del cuento desde Firestore
                val document = db.collection("cuentos")
                    .document(nombreCuento)
                    .get()
                    .await()

                if (document.exists()) {
                    urlCuento = document.getString("txt_$idioma") ?: ""
                    lineasCuento = document.getLong("lines")?.toInt() ?: 0

                    if (urlCuento.isNotEmpty()) {
                        // Descargar contenido del cuento
                        descargarCuento(urlCuento, cuento)
                        mostrarLinea(0)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando cuento: ${e.message}")
                Toast.makeText(this@ReproductorCuento, "Error cargando cuento", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarCuentoTraducido() {
        lifecycleScope.launch {
            try {
                val document = db.collection("cuentos")
                    .document(nombreCuento)
                    .get()
                    .await()

                if (document.exists()) {
                    urlTraducido = document.getString("txt_$idiomaTraduccion") ?: ""

                    if (urlTraducido.isNotEmpty()) {
                        cuentoTraducido.clear()
                        descargarCuento(urlTraducido, cuentoTraducido)
                        traducidoCreado = true
                        mostrarLineaTraducida(contadorLineas)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando traducción: ${e.message}")
            }
        }
    }

    private suspend fun descargarCuento(url: String, lista: ArrayList<String>) = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val urlObj = URL(url)
            connection = urlObj.openConnection() as HttpURLConnection
            val code = connection.responseCode

            if (code == 200) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))

                reader.useLines { lines ->
                    lines.forEach { line ->
                        lista.add(line)
                    }
                }

                Log.d(TAG, "Cuento descargado: ${lista.size} líneas")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error descargando cuento: ${e.message}")
        } finally {
            connection?.disconnect()
        }
    }

    private fun mostrarLinea(lineaIndex: Int) {
        if (lineaIndex >= 0 && lineaIndex < cuento.size) {
            val linea = cuento[lineaIndex]
            val partes = linea.split("/")

            if (partes.isNotEmpty()) {
                tvSeleccionado.text = partes[0]
            }

            if (partes.size > 1 && partes[1].isNotEmpty()) {
                cargarImagen(partes[1])
            }

            Log.d(TAG, "Mostrando línea $lineaIndex: ${partes[0]}")
        }
    }

    private fun mostrarLineaTraducida(lineaIndex: Int) {
        if (traducidoCreado && lineaIndex >= 0 && lineaIndex < cuentoTraducido.size) {
            val linea = cuentoTraducido[lineaIndex]
            val partes = linea.split("/")

            if (partes.isNotEmpty()) {
                tvTraducido.text = partes[0]
            }
        }
    }

    private fun cargarImagen(numeroImagen: String) {
        lifecycleScope.launch {
            try {
                val document = db.collection("imagenes")
                    .document(nombreCuento)
                    .get()
                    .await()

                if (document.exists()) {
                    urlImg = document.getString("img_$numeroImagen") ?: ""

                    if (urlImg.isNotEmpty()) {
                        descargarImagen(urlImg)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando imagen: ${e.message}")
            }
        }
    }

    private suspend fun descargarImagen(url: String) = withContext(Dispatchers.IO) {
        try {
            val urlObj = URL(url)
            val bitmap = BitmapFactory.decodeStream(urlObj.openConnection().getInputStream())
            val drawable = BitmapDrawable(resources, bitmap)

            withContext(Dispatchers.Main) {
                llFondo.background = drawable
            }

            Log.d(TAG, "Imagen cargada: $url")
        } catch (e: Exception) {
            Log.e(TAG, "Error descargando imagen: ${e.message}")
        }
    }

    private fun avanzarLinea() {
        val lineaActual = if (contadorLineas < cuento.size) cuento[contadorLineas] else ""

        if (lineaActual.contains("FIN/") || lineaActual.contains("FI/") || lineaActual.contains("THE END/")) {
            Log.d(TAG, "Final del cuento alcanzado")
            return
        }

        contadorLineas++
        mostrarLinea(contadorLineas)
        mostrarLineaTraducida(contadorLineas)
    }

    private fun retrocederLinea() {
        if (contadorLineas <= 0) {
            Log.d(TAG, "Ya estás en la primera línea")
            return
        }

        contadorLineas--
        mostrarLinea(contadorLineas)
        mostrarLineaTraducida(contadorLineas)
    }

    private fun iniciarTextToSpeech() {
        tts = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = when (idioma) {
                    "cat" -> Locale("cat", "CAT")
                    "esp" -> Locale("spa", "ESP")
                    "eng" -> Locale("en", "GB")
                    else -> Locale.getDefault()
                }

                val result = tts?.setLanguage(locale)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Idioma no soportado", Toast.LENGTH_SHORT).show()
                } else {
                    leerTexto(tvSeleccionado.text.toString())
                    configurarListenerTTS()
                }

                Log.d(TAG, "TextToSpeech iniciado en idioma: $idioma")
            } else {
                Toast.makeText(this, "Error al inicializar TTS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun leerTexto(texto: String) {
        val bundle = Bundle()
        bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
        tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, bundle, texto)
    }

    private fun configurarListenerTTS() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "TTS iniciado")
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId == "FIN" || utteranceId == "FI" || utteranceId == "THE END") {
                    runOnUiThread {
                        btnPlay.visibility = View.VISIBLE
                        btnPause.visibility = View.GONE
                        contadorLineas = 0
                        mostrarLinea(contadorLineas)
                        tts?.stop()
                        tts?.shutdown()
                    }
                } else {
                    avanzarLinea()
                    tts?.stop()
                    tts?.shutdown()
                    iniciarTextToSpeech()
                }
            }

            override fun onError(utteranceId: String?) {
                Log.e(TAG, "Error en TTS")
            }
        })
    }

    private fun mostrarDialogoSalir() {
        AlertDialog.Builder(this)
            .setMessage(R.string.seguro)
            .setTitle(R.string.volver_seleccion)
            .setPositiveButton(R.string.si) { _, _ ->
                tts?.stop()
                tts?.shutdown()
                finish()
            }
            .setNegativeButton(R.string.no, null)
            .create()
            .show()
    }

    private fun getIdiomaFromPosition(position: Int): String {
        return when (position) {
            0 -> "none"
            1 -> "esp"
            2 -> "cat"
            3 -> "eng"
            else -> "esp"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
    }
}
