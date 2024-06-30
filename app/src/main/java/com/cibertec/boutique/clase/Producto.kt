package com.cibertec.boutique.clase

data class Producto(
    val id: Int,
    val brand: String?,
    val name: String,
    val price: Double?,
    val image_link: String?,
    val description: String?,
    val product_type: String?
)