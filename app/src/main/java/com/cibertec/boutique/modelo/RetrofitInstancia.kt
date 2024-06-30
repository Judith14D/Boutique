package com.cibertec.boutique.modelo
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstancia {
    private const val BASE_URL = "https://makeup-api.herokuapp.com/api/v1/"

    val api: CosmeticoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CosmeticoApiService::class.java)
    }
}