package com.cibertec.boutique

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
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

        imageView = findViewById(R.id.imageView)
        val textViewUser = findViewById<TextView>(R.id.tvBienvenidaUsuario)
        val btnChangeImage = findViewById<Button>(R.id.btnChangeProfilePicture)

        val auth = Firebase.auth
        val user = auth.currentUser

        if (user != null) {
            val userName = user.displayName
            val userPhoto = user.photoUrl
            textViewUser.text = "Hola, " + userName
            Glide.with(this)
                .load(userPhoto)
                .into(imageView)
        } else {
            // Handle the case where the user is not signed in
        }

        btnChangeImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), IMAGE_REQUEST)
        }

        /*val sign_out_button = findViewById<Button>(R.id.logout_button)
        sign_out_button.setOnClickListener {
            signOutAndStartSignInActivity()
        }*/

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            // uri = utilizado para archivos de imagem, manejo de rutas de imagen
            val uri = data.data
            uploadImageToFirebase(uri)
        }
    }

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