package com.cibertec.boutique.modelo
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstancia {
    private const val BASE_URL = "https://makeup-api.herokuapp.com/api/v1/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Tiempo máximo para establecer conexión
        .readTimeout(30, TimeUnit.SECONDS)    // Tiempo máximo para leer datos
        .writeTimeout(30, TimeUnit.SECONDS)   // Tiempo máximo para escribir datos
        .build()

    // Creación de la instancia de Retrofit
    val api: CosmeticoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Asignación del cliente OkHttpClient configurado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CosmeticoApiService::class.java)
    }
}