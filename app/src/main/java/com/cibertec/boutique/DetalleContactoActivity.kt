package com.cibertec.boutique

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cibertec.boutique.clase.Usuario
import com.cibertec.boutique.databinding.ActivityDetalleContactoBinding
import com.cibertec.boutique.db.DBHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DetalleContactoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleContactoBinding
    private var usuario: Usuario? = null
    private var imagenUsuario: ByteArray? = null
    private val dbHelper = DBHelper(this)

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intentData: Intent? = result.data
            intentData?.data?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    imagenUsuario = stream.toByteArray()

                    binding.imageUsuario.setImageBitmap(bitmap)

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleContactoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        usuario = intent.getSerializableExtra("usuario") as? Usuario


        usuario?.let { user ->
            binding.editTextId.setText(user.id.toString())
            binding.editTextNombre.setText(user.nombre ?: "")
            binding.editTextApellido.setText(user.apellido ?: "")
            binding.editTextCorreo.setText(user.correo ?: "")
            binding.editTextEdad.setText(user.edad?.toString() ?: "")


            val fechaNacString = user.fechaNac?.let { dateFormat.format(it) } ?: ""
            binding.editTextFechaNac.setText(fechaNacString)

            binding.editTextSexo.setText(user.sexo ?: "")
            binding.editTextContrasenia.setText(user.contrasena ?: "")

            user.imagen?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                binding.imageUsuario.setImageBitmap(bitmap)
            }

            binding.btnSeleccionarImagen.setOnClickListener {
                seleccionarImagen()
            }

            binding.btnEliminar.setOnClickListener {
                eliminarUsuario()
            }

            binding.btnEditar.setOnClickListener {
                actualizarUsuario()
            }

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
        }
    }

    private fun actualizarUsuario() {
        val nuevoNombre = binding.editTextNombre.text.toString()
        val nuevoApellido = binding.editTextApellido.text.toString()
        val nuevoCorreo = binding.editTextCorreo.text.toString()
        val nuevaEdad = binding.editTextEdad.text.toString().toIntOrNull() ?: 0
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val nuevaFechaNac = dateFormat.parse(binding.editTextFechaNac.text.toString()) ?: Date()
        val nuevoSexo = binding.editTextSexo.text.toString()
        val nuevaContrasena = binding.editTextContrasenia.text.toString()


        if (usuario != null && (
                    usuario!!.nombre != nuevoNombre ||
                            usuario!!.apellido != nuevoApellido ||
                            usuario!!.correo != nuevoCorreo ||
                            usuario!!.edad != nuevaEdad ||
                            usuario!!.fechaNac != nuevaFechaNac ||
                            usuario!!.sexo != nuevoSexo ||
                            usuario!!.contrasena != nuevaContrasena ||
                            usuario!!.imagen != imagenUsuario
                    )) {

            usuario?.apply {
                nombre = nuevoNombre
                apellido = nuevoApellido
                correo = nuevoCorreo
                edad = nuevaEdad
                fechaNac = nuevaFechaNac
                sexo = nuevoSexo
                contrasena = nuevaContrasena
                imagen = imagenUsuario
            }


            val dbHelper = DBHelper(this)
            if (dbHelper.actualizarUsuario(usuario!!)) {

                Toast.makeText(this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, ContactoActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error al actualizar usuario", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No se han realizado cambios", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ContactoActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
    private fun eliminarUsuario() {
        usuario?.let { user ->
            if (dbHelper.eliminarUsuario(user.id)) {
                Toast.makeText(this, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ContactoActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        actualizarUsuario()
        super.onBackPressed()
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        getContent.launch(intent)
    }

}
