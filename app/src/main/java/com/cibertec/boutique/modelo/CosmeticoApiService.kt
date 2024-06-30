package com.cibertec.boutique.modelo

import com.cibertec.boutique.clase.Producto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CosmeticoApiService {
    @GET("products.json")
    fun getProducts(): Call<List<Producto>>
    @GET("products.json")
    fun getProductsByType(@Query("product_type") type: String): Call<List<Producto>>
}

