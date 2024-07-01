package com.cibertec.boutique

import android.app.Activity
import android.content.Intent
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

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

        btnChangeImage.setOnClickListener {
            showPopupMenu(it)
        }

        /*val sign_out_button = findViewById<Button>(R.id.logout_button)
        sign_out_button.setOnClickListener {
            signOutAndStartSignInActivity()
        }*/

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
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // uri = utilizado para archivos de imagem, manejo de rutas de imagen
            val uri = data.data
            uploadImageToFirebase(uri)
        }
    }

    //ACTUALIZAR IMAGEN DE FIREBASE
    private fun uploadImageToFirebase(uri: Uri?) {
        if (uri != null) {
            val storageReference = FirebaseStorage.getInstance().reference
            val ref = storageReference.child("profile_images/" + UUID.randomUUID().toString())
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
}