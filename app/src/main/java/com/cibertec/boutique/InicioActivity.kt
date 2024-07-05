package com.cibertec.boutique

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cibertec.boutique.db.DBHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class InicioActivity : AppCompatActivity() {
    //Variables
    private lateinit var textViewNoCuenta : TextView

    //Autenticacion con bd
    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnIniciarSesion: Button

    //Firebase
    companion object {
        private const val RC_SIGN_IN = 9001
    }
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textViewNoCuenta = findViewById(R.id.textViewNoCuenta)
        etCorreo = findViewById(R.id.etCorreo)
        etContrasena = findViewById(R.id.etContrasena)
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion)


        //SPAN
        val text: String = "Todavía no tienes una cuenta? Regístrate"

        val spannableString = SpannableString(text)
        val boldSpan = StyleSpan(Typeface.BOLD)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@InicioActivity, RegistroActivity::class.java)
                startActivity(intent)
            }
        }

        val start = text.indexOf("Regístrate")
        val end = start + "Regístrate".length

        spannableString.setSpan(boldSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textViewNoCuenta.text = spannableString
        textViewNoCuenta.movementMethod = LinkMovementMethod.getInstance()

        //FIREBASE
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val intent = Intent(this, UsuarioActivity::class.java)
            startActivity(intent)
            finish()
        }

        val icGoogle: ImageView = findViewById(R.id.icGoogle)
        icGoogle.setOnClickListener {
            signIn()
        }

        //BOTON INCIO BD
        btnIniciarSesion.setOnClickListener {
            iniciarSesion()
        }

    }


    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //AUTENTACION CON BD
    private fun iniciarSesion() {
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = DBHelper(this)
        val usuario = dbHelper.verificarUsuario(correo, contrasena)

        if (usuario != null) {
            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

            // Guardar datos del usuario en SharedPreferences
            val preferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putInt("id", usuario.id)
            editor.putString("nombre", usuario.nombre)
            editor.putString("apellido", usuario.apellido)
            editor.putString("correo", usuario.correo)
            editor.putString("contrasena", usuario.contrasena)
            if (usuario.imagen != null) {
                val imagenBase64 = Base64.encodeToString(usuario.imagen, Base64.DEFAULT)
                editor.putString("imagen", imagenBase64)
            } else {
                editor.putString("imagen", null)
            }
            editor.apply()

            // Preparar datos del usuario para PerfilActivity
            val intent = Intent(this, PerfilActivity::class.java).apply {
                putExtra("id", usuario.id)
                putExtra("nombre", usuario.nombre)
                putExtra("apellido", usuario.apellido)
                putExtra("correo", usuario.correo)
                putExtra("contrasena", usuario.contrasena)
                // Si necesitas pasar la imagen como ByteArray en el intent:
                putExtra("imagen", usuario.imagen)
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
        }
    }


}