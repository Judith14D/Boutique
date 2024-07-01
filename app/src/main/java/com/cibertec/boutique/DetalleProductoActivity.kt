package com.cibertec.boutique

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.picasso.Picasso

class DetalleProductoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_producto)

        val imageViewProducto: ImageView = findViewById(R.id.imageProducto)
        val editTextNombre: EditText = findViewById(R.id.editTextNombre)
        val editTextDescripcion: EditText = findViewById(R.id.editTextDescripcion)
        val editTextCategoria: EditText = findViewById(R.id.editTextCategoria)
        val editTextPrecio: EditText = findViewById(R.id.editTextPrecio)
        val btnVolver: Button = findViewById(R.id.btnVolver)

        editTextNombre.isEnabled = false
        editTextDescripcion.isEnabled = false
        editTextCategoria.isEnabled = false
        editTextPrecio.isEnabled = false

        val imagenUrl = intent.getStringExtra("imagen")
        Picasso.get().load(imagenUrl).into(imageViewProducto)

        val nombre = intent.getStringExtra("nombre")
        val descripcion = intent.getStringExtra("descripcion")
        val categoria = intent.getStringExtra("categoria")
        val precio = intent.getDoubleExtra("precio", 0.0)

        editTextNombre.setText(nombre)
        editTextDescripcion.setText(descripcion)
        editTextCategoria.setText(categoria)
        editTextPrecio.setText(precio.toString())

        btnVolver.setOnClickListener {
            finish()
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