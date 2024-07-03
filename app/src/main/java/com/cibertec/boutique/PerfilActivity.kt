package com.cibertec.boutique

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class PerfilActivity : AppCompatActivity() {
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    private lateinit var imageView: ImageView
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextNombres: EditText
    private lateinit var editTextApellidos:EditText
    private lateinit var editTextContrasena: EditText


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
        val textViewUser = findViewById<TextView>(R.id.tvBienvenidaUsuario)
        val btnChangeImage = findViewById<ImageView>(R.id.btnChangeProfilePicture)

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
                .into(imageView)
        } else {
            // Handle the case where the user is not signed in
        }

        //Lanza el popup de las opciones
        btnChangeImage.setOnClickListener {
            showPopupMenu(it)
        }

        val sign_out_button = findViewById<Button>(R.id.btnCerrarSesion)
        sign_out_button.setOnClickListener {
            signOutAndStartSignInActivity()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // uri = utilizado para archivos de imagem, manejo de rutas de imagen de galería
            val uri = data?.data

            //valida si la imagen fue tomada desde la galería o por camara
            if (uri != null) {
                uploadImageToFirebase(uri)
            } else {
                val bitmap = data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    val tempUri = bitmapToUri(this,bitmap)
                    uploadImageToFirebase(tempUri)
                }
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
                        .into(imageView)
                }
            }
    }


    //CERRAR SESION
    private fun signOutAndStartSignInActivity() {
        mAuth.signOut()

        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            // Optional: Update UI or show a message to the user
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    //  PERMISOS

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