package com.cibertec.boutique.adaptador

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cibertec.boutique.DetalleProductoActivity
import com.cibertec.boutique.R
import com.cibertec.boutique.clase.Producto
import com.squareup.picasso.Picasso

class ProductoAdapter(private var productList: List<Producto>) : RecyclerView.Adapter<ProductoAdapter.ProductViewHolder>() {

    private var filteredList: List<Producto> = productList

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewProducto: ImageView = itemView.findViewById(R.id.imageViewProducto)
        private val imageViewCorazon: ImageView = itemView.findViewById(R.id.imageViewFavorite)
        private var isFavorito: Boolean = false // Puedes usar un campo en tu modelo para esto

        init {
            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetalleProductoActivity::class.java).apply {
                    val position = adapterPosition
                    putExtra("imagen", filteredList[position].image_link)
                    putExtra("nombre", filteredList[position].name)
                    putExtra("descripcion", filteredList[position].description)
                    putExtra("categoria", filteredList[position].product_type)
                    putExtra("precio", filteredList[position].price ?: 0.0) // Default a 0.0 si es null
                    // Agrega más datos según sea necesario
                }
                itemView.context.startActivity(intent)
            }

            imageViewCorazon.setOnClickListener {
                isFavorito = !isFavorito
                val animacion = AnimationUtils.loadAnimation(itemView.context, R.anim.cambio_corazon)
                imageViewCorazon.startAnimation(animacion)

                if (isFavorito) {
                    imageViewCorazon.setImageResource(R.drawable.corazon_rojo) // Cambia a la imagen roja
                } else {
                    imageViewCorazon.setImageResource(R.drawable.corazon_normal) // Cambia a la imagen normal
                }

                // Aquí puedes realizar otras acciones como guardar el estado en tu modelo de datos
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        if (filteredList.isNotEmpty()) {
            val product = filteredList[position]
            holder.itemView.findViewById<TextView>(R.id.tvNombreProducto).text = product.name
            holder.itemView.findViewById<TextView>(R.id.tvDescripcion).text = product.description
            val imageViewProducto = holder.itemView.findViewById<ImageView>(R.id.imageViewProducto)
            try {
                Picasso.get().load(product.image_link).into(imageViewProducto)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Implementa aquí el comportamiento del botón de favorito si es necesario
        } else {
            // Manejar caso donde la lista está vacía, por ejemplo:
            holder.itemView.findViewById<TextView>(R.id.tvNombreProducto).text = "No hay productos"
            holder.itemView.findViewById<TextView>(R.id.tvDescripcion).text = ""
            // Puedes ocultar o deshabilitar vistas aquí según sea necesario
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun filterProducts(query: String) {
        filteredList = if (query.isEmpty()) {
            productList
        } else {
            productList.filter { it.name.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    fun updateProducts(newProducts: List<Producto>) {
        productList = newProducts
        filteredList = newProducts // Asegúrate de actualizar también la lista filtrada si es necesario
        notifyDataSetChanged()
    }
}
