package com.cibertec.boutique
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.boutique.adaptador.ProductoAdapter
import com.cibertec.boutique.clase.Producto
import com.cibertec.boutique.modelo.RetrofitInstancia
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsuarioActivity : AppCompatActivity() {

    private lateinit var recyclerViewProducts: RecyclerView
    private lateinit var productoAdapter: ProductoAdapter
    private var productList: List<Producto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usuario)

        // Configurar el RecyclerView
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts)
        recyclerViewProducts.layoutManager = LinearLayoutManager(this)
        productoAdapter = ProductoAdapter(productList)
        recyclerViewProducts.adapter = productoAdapter

        // Configurar el SearchView
        val searchViewProducts: SearchView = findViewById(R.id.searchView)
        searchViewProducts.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterProducts(it) }
                return true
            }
        })

        // Configurar el Spinner de tipos de producto
        val spinnerProductType: Spinner = findViewById(R.id.spFiltros)
        val productTypes = arrayOf("Todo", "Bronzer", "Lip Liner", "Mascara", "Eyeliner", "blush", "foundation", "lipstick", "eyeshadow", "eyebrow") // Aquí deberías tener los tipos disponibles
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, productTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProductType.adapter = adapter

        spinnerProductType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedType = productTypes[position]
                if (selectedType == "Todo") {
                    fetchAllProducts()
                } else {
                    fetchProductsByType(selectedType)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Implementar según sea necesario
            }
        }

        // Configurar BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_inicio -> {
                    // Ya estamos en esta actividad
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

        // Cargar todos los productos al iniciar la actividad
        fetchAllProducts()
    }

    private fun fetchAllProducts() {
        RetrofitInstancia.api.getProducts().enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                if (response.isSuccessful) {
                    val productListResponse = response.body()
                    productListResponse?.let {
                        productList = it.drop(50) // Filtrar los primeros 50 elementos
                        updateAdapter(productList)
                    }
                } else {
                    showToast("Error al obtener los productos")
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                showToast("Fallo la solicitud de red")
            }
        })
    }


    private fun fetchProductsByType(type: String) {
        RetrofitInstancia.api.getProductsByType(type).enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                if (response.isSuccessful) {
                    val productListResponse = response.body()
                    productListResponse?.let {
                        productList = it // Actualizar la lista de productos
                        updateAdapter(productList)
                    }
                } else {
                    showToast("Error al obtener los productos")
                }
            }

            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                showToast("Fallo la solicitud de red")
            }
        })
    }

    private fun filterProducts(query: String) {
        // Filtrar productos por nombre que contengan el texto de la búsqueda
        val filteredList = productList.filter {
            it.name.contains(query, ignoreCase = true)
        }
        // Actualizar el adaptador con la lista filtrada
        updateAdapter(filteredList)
    }

    private fun updateAdapter(products: List<Producto>) {
        productoAdapter.updateProducts(products)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}