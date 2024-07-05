package com.cibertec.boutique

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.cibertec.boutique.adaptador.UsuarioAdapter
import com.cibertec.boutique.clase.Usuario
import com.cibertec.boutique.db.DBHelper
import com.cibertec.boutique.databinding.ActivityContactoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class ContactoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactoBinding
    private lateinit var usuarioAdapter: UsuarioAdapter
    //private lateinit var usuarioDao: UsuarioDAO
    private lateinit var usuarioDB: DBHelper
    private lateinit var usuariosList: ArrayList<Usuario>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        usuarioDB = DBHelper(this)

        val bottomNavigationView: BottomNavigationView = findViewById(com.cibertec.boutique.R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                com.cibertec.boutique.R.id.navigation_inicio -> {
                    val intent = Intent(this, UsuarioActivity::class.java)
                    startActivity(intent)
                    true
                }
                com.cibertec.boutique.R.id.navigation_contacto -> {
                    val intent = Intent(this, ContactoActivity::class.java)
                    startActivity(intent)
                    true
                }
                com.cibertec.boutique.R.id.navigation_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarUsuarios(newText)
                return true
            }
        })

        val opcionesFiltro = arrayOf("Todos", "Hombre", "Mujer")
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_dropdown_item, opcionesFiltro)
        binding.spFiltros.adapter = adapter
        binding.spFiltros.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filtrarUsuariosPorSexo(opcionesFiltro[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        usuariosList = ArrayList(usuarioDB.obtenerUsuarios())

        usuarioAdapter = UsuarioAdapter(usuariosList) { usuario ->
            val intent = Intent(this, DetalleContactoActivity::class.java)
            intent.putExtra("usuario", usuario)
            startActivity(intent)
        }
        binding.recyclerViewUsuarios.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewUsuarios.adapter = usuarioAdapter
    }

    private fun filtrarUsuarios(query: String?) {
        // Obtener el filtro de sexo seleccionado
        val filtroSexo = binding.spFiltros.selectedItem.toString()

        val usuariosFiltrados = usuariosList.filter { usuario ->
            val cumpleNombre = usuario.nombre?.contains(query.orEmpty(), ignoreCase = true) ?: false
            val cumpleSexo = when (filtroSexo) {
                "Hombre" -> usuario.sexo == "Hombre"
                "Mujer" -> usuario.sexo == "Mujer"
                else -> true
            }
            cumpleNombre && cumpleSexo
        }

        usuarioAdapter.actualizarLista(usuariosFiltrados)
    }

    private fun filtrarUsuariosPorSexo(sexo: String) {
        val usuariosFiltrados = when (sexo) {
            "Hombre" -> usuariosList.filter { it.sexo == "Hombre" }
            "Mujer" -> usuariosList.filter { it.sexo == "Mujer" }
            else -> usuariosList
        }
        usuarioAdapter.actualizarLista(usuariosFiltrados)

        val textoBusqueda = binding.searchView.query.toString()
        filtrarUsuarios(textoBusqueda)
    }
}
