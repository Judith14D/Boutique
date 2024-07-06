package com.cibertec.boutique.adaptador

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cibertec.boutique.R
import com.cibertec.boutique.clase.CircleTransformation
import com.cibertec.boutique.clase.Usuario

class UsuarioAdapter(
    private var usuarios: List<Usuario>,
    private val onClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contacto, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.bind(usuario, onClick)
    }

    fun actualizarLista(nuevaLista: List<Usuario>) {
        usuarios = nuevaLista
        notifyDataSetChanged()
    }

    override fun getItemCount() = usuarios.size

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nombreTextView: TextView = view.findViewById(R.id.tvNombreUsuario)
        private val edadTextView: TextView = view.findViewById(R.id.tvSoles)
        private val imagenImageView: ImageView = view.findViewById(R.id.imageViewUsuario)

        fun bind(usuario: Usuario, onClick: (Usuario) -> Unit) {
            nombreTextView.text = usuario.nombre
            edadTextView.text = "Edad: ${usuario.edad}"
            usuario.imagen?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                Glide.with(itemView.context)
                    .load(bitmap)
                    .transform(CircleTransformation())
                    .into(imagenImageView)
            }
            itemView.setOnClickListener { onClick(usuario) }
        }
    }
}