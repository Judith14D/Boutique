package com.cibertec.boutique.clase

import java.io.Serializable
import java.util.Date

data class Usuario(
    val id: Int  = 0,
    var nombre: String,
    var apellido: String,
    var correo: String,
    var edad: Int,
    var fechaNac: Date,
    var sexo: String,
    var contrasena: String,
    var imagen: ByteArray?
): Serializable