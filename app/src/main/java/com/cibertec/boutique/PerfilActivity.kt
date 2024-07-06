package com.cibertec.boutique

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.cibertec.boutique.clase.CircleTransformation
import com.cibertec.boutique.clase.Usuario
import com.cibertec.boutique.db.DBHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import com.google.firebase.storage.FirebaseStorage
import org.w3c.dom.Text
import java.util.Date
import java.util.UUID

class PerfilActivity : AppCompatActivity() {
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    private lateinit var imageView: ImageView
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextNombres: EditText
    private lateinit var editTextApellidos:EditText
    private lateinit var editTextContrasena: EditText
    private lateinit var textViewUser : TextView

    private lateinit var btnChangeImage: Button
    private lateinit var btnSignOut: Button
    private lateinit var btnEditar: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnMapa: Button

    private lateinit var dbHelper: DBHelper
    private var usuarioId: Int =0
    private var isFirstTimeProfile: Boolean = true

    companion object {
        private const val IMAGE_REQUEST = 1
        private const val REQUEST_CODE_STORAGE_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)
        checkAndRequestPermissions()

        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        //INICIALIZACION DE VARIABLES
        imageView = findViewById(R.id.imageView)
        editTextNombres = findViewById(R.id.editTextNombres)
        editTextApellidos = findViewById(R.id.editTextApellidos)
        editTextContrasena = findViewById(R.id.editTextContrasena)
        editTextCorreo = findViewById(R.id.editTextCorreo)
        textViewUser = findViewById(R.id.tvBienvenidaUsuario)

        btnChangeImage = findViewById(R.id.btnCambiarImagen)
        btnSignOut = findViewById(R.id.btnCerrarSesion)
        btnEditar = findViewById(R.id.btnEditar)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnMapa = findViewById(R.id.btnMapa)

        //Obtener los datos de la otra vista
        val id = intent.getIntExtra("id", 0)
        val nombre = intent.getStringExtra("nombre") ?: ""
        val apellido = intent.getStringExtra("apellido") ?: ""
        val correo = intent.getStringExtra("correo") ?: ""
        val contrasena = intent.getStringExtra("contrasena") ?: ""
        val imagenBase64 = intent.getStringExtra("imagen") ?: ""
        val edad = intent.getIntExtra("edad", 0)
        val fechaNac = intent.getSerializableExtra("fechaNac") as? Date ?: Date()
        val sexo = intent.getStringExtra("sexo") ?: ""
        // Convertir la imagen de Base64 a ByteArray
        val imagen = if (imagenBase64.isNotEmpty()) {
            Base64.decode(imagenBase64, Base64.DEFAULT)
        } else {
            null
        }

        //Firebase
        val auth = Firebase.auth
        val user = auth.currentUser

        if (user != null) {
            val userName = user.displayName
            val userPhoto = user.photoUrl
            val userEmail = user.email

            // Separar nombre y apellidos
            val names = userName?.split(" ")
            val firstName = names?.get(0) ?: ""
            val lastName = if (names?.size ?: 0 > 1) names?.subList(1, names.size)?.joinToString(" ") else ""


            textViewUser.text = "Hola, " + userName
            editTextCorreo.setText(userEmail)
            editTextNombres.setText(firstName)
            editTextApellidos.setText(lastName)
            Glide.with(this)
                .load(userPhoto)
                .transform(CircleTransformation())
                .into(imageView)
        } else {
            val usuario = Usuario(
                id = id,
                nombre = nombre,
                apellido = apellido,
                correo = correo,
                contrasena = contrasena,
                edad = edad,
                sexo = sexo,
                fechaNac = fechaNac,
                imagen = imagen
            )
            // Guardar datos del usuario
            guardarDatosUsuarioEnSharedPreferences(usuario)
            // Obtener el ID del usuario desde SharedPreferences
            usuarioId = obtenerIdDeUsuario()
            Log.d("PerfilActivity", "ID de usuario inicial: $usuarioId")
            textViewUser.text = "Hola, $nombre"
            editTextNombres.setText(nombre)
            editTextApellidos.setText(apellido)
            editTextCorreo.setText(correo)
            editTextContrasena.setText(contrasena)

            cargarImagenPerfil()
            cargarDatosUsuario()

            dbHelper = DBHelper(this)
            guardarCambios()
            usuarioId = obtenerIdDeUsuario()
            Log.d("PerfilActivity", "ID de usuario inicial: $usuarioId")
        }

        //Lanza el popup de las opciones
        btnChangeImage.setOnClickListener {
            showPopupMenu(it)
        }

        btnSignOut.setOnClickListener {
            if(user != null){
                signOutAndStartSignInActivity()
            }else{
                cerrarSesionYRedirigirAMainActivity()
            }
        }


        btnEditar.setOnClickListener {
            habilitarEdicion(true)
        }

        btnGuardar.setOnClickListener {
            guardarCambios()
        }

        btnMapa.setOnClickListener {
            val intent = Intent(this, MapaActivity::class.java)
            startActivity(intent)
        }

        //NAVEGACION
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_inicio -> {
                    val intent = Intent(this, UsuarioActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_contacto -> {
                    val intent = Intent(this, ContactoActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }


    } //FIN ONCREATE

    //POP UP DE OPCIONES DE FOTO
    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.options_profile_picture)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_choose_from_gallery -> {
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), IMAGE_REQUEST)
                    true
                }
                R.id.action_take_photo -> {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                        takePictureIntent.resolveActivity(packageManager)?.also {
                            startActivityForResult(takePictureIntent, IMAGE_REQUEST)
                        }
                    }
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    //EDITAR DATOS DE USUARIO ---------------------------------------------------------

    //IMAGEN  ********************
    private fun cargarImagenPerfil() {
        val imagenByteArray = intent.getByteArrayExtra("imagen")

        if (imagenByteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(imagenByteArray, 0, imagenByteArray.size)
            imageView.setImageBitmap(bitmap)
            isFirstTimeProfile = false
        } else {
            val defaultImage = BitmapFactory.decodeResource(resources, R.drawable.default_profile_image)
            imageView.setImageBitmap(defaultImage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // uri = utilizado para archivos de imagem, manejo de rutas de imagen de galería
            val uri = data?.data

            //valida si la imagen fue tomada desde la galería o por camara
            if (uri != null && Firebase.auth.currentUser != null) {  //galería y firebase
                uploadImageToFirebase(uri)
            }else if(uri != null && obtenerIdDeUsuario() != -1) {
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    Glide.with(this)
                        .load(bitmap)
                        .transform(CircleTransformation())
                        .into(imageView)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    intent.putExtra("imagen", byteArray)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (uri == null && Firebase.auth.currentUser != null){
                val bitmap = data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    val tempUri = bitmapToUri(this,bitmap)
                    uploadImageToFirebase(tempUri)
                }
            }else{
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                Glide.with(this)
                    .load(imageBitmap)
                    .transform(CircleTransformation())
                    .into(imageView)
                val byteArrayOutputStream = ByteArrayOutputStream()
                imageBitmap?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                intent.putExtra("imagen", byteArray)
            }
        }
    }

    //Convierte el bitmap a Uri para poder subirlo a firebase - TOMADO DESDE CAMARA
    private fun bitmapToUri(context: Context, bitmap: Bitmap): Uri {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Perfil", null)
        return Uri.parse(path)
    }

    //ACTUALIZAR IMAGEN DE FIREBASE
    private fun uploadImageToFirebase(uri: Uri?) {
        if (uri != null) {
            val storage = FirebaseStorage.getInstance().reference
            val ref = storage.child("profile_images/" + UUID.randomUUID().toString())
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        updateProfileImage(downloadUri)
                    }
                }
                .addOnFailureListener {
                    // Handle unsuccessful uploads
                }
        }
    }

    //ACTUALIZAR IMAGEN
    private fun updateProfileImage(downloadUri: Uri) {
        val user = Firebase.auth.currentUser

        val profileUpdates = userProfileChangeRequest {
            photoUri = downloadUri
        }

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Load the new profile image using Glide
                    Glide.with(this)
                        .load(downloadUri)
                        .transform(CircleTransformation())
                        .into(imageView)
                }
            }
    }

    //DATOS ********************

    private fun guardarDatosUsuarioEnSharedPreferences(usuario: Usuario) {
        val preferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("id", usuario.id)
        editor.putString("nombre", usuario.nombre)
        editor.putString("apellido", usuario.apellido)
        editor.putString("correo", usuario.correo)
        editor.putString("contrasena", usuario.contrasena)
        if (usuario.imagen != null) {
            editor.putString("imagen", Base64.encodeToString(usuario.imagen, Base64.DEFAULT))
        }
        editor.apply()

        Log.d("SharedPreferences", "Datos guardados en SharedPreferences:")
        Log.d("SharedPreferences", "ID: ${usuario.id}")
        Log.d("SharedPreferences", "Nombre: ${usuario.nombre}")
        Log.d("SharedPreferences", "Apellido: ${usuario.apellido}")
        Log.d("SharedPreferences", "Correo: ${usuario.correo}")
        Log.d("SharedPreferences", "Contrasena: ${usuario.contrasena}")
    }


    private fun guardarCambios() {
        val nombres = editTextNombres.text.toString().trim()
        val apellidos = editTextApellidos.text.toString().trim()
        val correo = editTextCorreo.text.toString().trim()
        val contrasena = editTextContrasena.text.toString().trim()

        if (nombres.isNotEmpty() && apellidos.isNotEmpty() && correo.isNotEmpty() && contrasena.isNotEmpty()) {
            val usuarioExistente = dbHelper.obtenerPerfilPorId(usuarioId)
            if (usuarioExistente != null) {
                usuarioExistente.nombre = nombres
                usuarioExistente.apellido = apellidos
                usuarioExistente.correo = correo
                usuarioExistente.contrasena = contrasena
                usuarioExistente.imagen = obtenerImagenPerfil()

                val success = dbHelper.actualizarPerfil(usuarioExistente)
                if (success) {
                    guardarDatosUsuarioEnSharedPreferences(usuarioExistente)

                    Toast.makeText(this, "Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
                    cargarDatosUsuario()
                    habilitarEdicion(false)
                } else {
                    Toast.makeText(this, "Error al guardar los cambios", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerImagenPerfil(): ByteArray? {
        val drawable = imageView.drawable as? BitmapDrawable ?: return null
        val bitmap = drawable.bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun habilitarEdicion(habilitar: Boolean) {
        editTextNombres.isEnabled = habilitar
        editTextApellidos.isEnabled = habilitar
        editTextCorreo.isEnabled = habilitar
        editTextContrasena.isEnabled = habilitar

        if (habilitar) {
            btnEditar.visibility = View.GONE
            btnGuardar.visibility = View.VISIBLE
        } else {
            btnEditar.visibility = View.VISIBLE
            btnGuardar.visibility = View.GONE
        }
    }
    private fun cargarDatosUsuario() {
        val preferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val nombre = preferences.getString("nombre", "")
        val apellido = preferences.getString("apellido", "")
        val correo = preferences.getString("correo", "")
        val contrasena = preferences.getString("contrasena", "")
        val imagenBase64 = preferences.getString("imagen", "")
        val usuarioId = preferences.getInt("id", 0)

        if (usuarioId == 0) {
            Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("SharedPreferences", "Datos recuperados de SharedPreferences:")
        Log.d("SharedPreferences", "ID: $usuarioId")
        Log.d("SharedPreferences", "Nombre: $nombre")
        Log.d("SharedPreferences", "Apellido: $apellido")
        Log.d("SharedPreferences", "Correo: $correo")
        Log.d("SharedPreferences", "Contrasena: $contrasena")
        Log.d("SharedPreferences", "Imagen: $imagenBase64")


        textViewUser.text = "Hola, $nombre"
        editTextNombres.setText(nombre)
        editTextApellidos.setText(apellido)
        editTextCorreo.setText(correo)
        editTextContrasena.setText(contrasena)

        if (!imagenBase64.isNullOrEmpty()) {
            val imageBytes = Base64.decode(imagenBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            imageView.setImageBitmap(bitmap)
        } else {
            imageView.setImageResource(R.drawable.default_profile_image)
        }
    }

    private fun obtenerIdDeUsuario(): Int {
        val preferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val id = preferences.getInt("id", -1)
        if (id == -1) {
            Toast.makeText(this, "ID de usuario no encontrado en SharedPreferences", Toast.LENGTH_SHORT).show()
            Log.d("PerfilActivity", "ID de usuario no encontrado en SharedPreferences")
        } else {
            Log.d("PerfilActivity", "ID de usuario obtenido de SharedPreferences: $id")
        }
        return id
    }



    //CERRAR SESION ------------------------------------------

    //Firebase
    private fun signOutAndStartSignInActivity() {
        mAuth.signOut()

        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            // Optional: Update UI or show a message to the user
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //Con bd
    private fun cerrarSesionYRedirigirAMainActivity() {
        val preferences = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        finish()
    }

    /*
    override fun onResume() {
        super.onResume()
        cargarImagenPerfil()
    }*/


    //  PERMISOS ---------------------------------------------

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionsToRequest = mutableListOf<String>()

        for (permission in permissions) {
            //El conetextCompat verifica que el permiso haya sido agregado
            //si no, lo solicita con el permissionsToRequest
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        //El permissionsToRquest verifica que haya permisos por solicitar
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_CODE_STORAGE_PERMISSION)
        } else {
            // AQUI VIENE LA LOGICA PARA CARGAR LOS QUE DESEAMOS DE LOS PERMISOS CONCEDIDOS
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    //Verifica nuevamente que todos los permisos hayan sido otogados
                } else {
                    println("No se puede acceder a la escritura de imagenes")
                }
            }
        }
    }
}