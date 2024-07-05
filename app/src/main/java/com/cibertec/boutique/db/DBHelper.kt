package com.cibertec.boutique.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.cibertec.boutique.clase.Usuario
import java.util.*

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "UsuariosDB"
        const val TABLE_NAME = "usuarios"
        const val KEY_ID = "id"
        const val KEY_NOMBRE = "nombre"
        const val KEY_APELLIDO = "apellido"
        const val KEY_CORREO = "correo"
        const val KEY_EDAD = "edad"
        const val KEY_FECHA_NAC = "fecha_nac"
        const val KEY_SEXO = "sexo"
        const val KEY_CONTRASENA = "contrasena"
        const val KEY_IMAGEN = "imagen"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = ("CREATE TABLE $TABLE_NAME ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$KEY_NOMBRE TEXT, $KEY_APELLIDO TEXT, $KEY_CORREO TEXT, "
                + "$KEY_EDAD INTEGER, $KEY_FECHA_NAC TEXT, $KEY_SEXO TEXT, "
                + "$KEY_CONTRASENA TEXT, $KEY_IMAGEN BLOB)")
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertarUsuario(usuario: Usuario): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(KEY_NOMBRE, usuario.nombre)
            put(KEY_APELLIDO, usuario.apellido)
            put(KEY_CORREO, usuario.correo)
            put(KEY_EDAD, usuario.edad)
            put(KEY_FECHA_NAC, usuario.fechaNac.date.toString())
            put(KEY_SEXO, usuario.sexo)
            put(KEY_CONTRASENA, usuario.contrasena)
        }
        val result = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return result
    }

    fun actualizarUsuario(usuario: Usuario): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(KEY_NOMBRE, usuario.nombre)
            put(KEY_APELLIDO, usuario.apellido)
            put(KEY_CORREO, usuario.correo)
            put(KEY_EDAD, usuario.edad)
            put(KEY_FECHA_NAC, usuario.fechaNac.time.toString())
            put(KEY_SEXO, usuario.sexo)
            put(KEY_CONTRASENA, usuario.contrasena)
            usuario.imagen?.let { put(KEY_IMAGEN, it) }
        }

        val whereClause = "$KEY_ID = ?"
        val whereArgs = arrayOf(usuario.id.toString())

        val updatedRows = db.update(TABLE_NAME, contentValues, whereClause, whereArgs)
        db.close()

        return updatedRows > 0
    }
    fun actualizarPerfil(usuario: Usuario): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(KEY_NOMBRE, usuario.nombre)
            put(KEY_APELLIDO, usuario.apellido)
            put(KEY_CORREO, usuario.correo)
            put(KEY_CONTRASENA, usuario.contrasena) // Asegúrate de que contrasena sea String
            usuario.imagen?.let { put(KEY_IMAGEN, it) }
        }

        val whereClause = "$KEY_ID = ?"
        val whereArgs = arrayOf(usuario.id.toString())

        val updatedRows = db.update(TABLE_NAME, contentValues, whereClause, whereArgs)
        db.close()

        return updatedRows > 0
    }


    fun obtenerPerfilPorId(id: Int): Usuario? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(KEY_ID, KEY_NOMBRE, KEY_APELLIDO, KEY_CORREO, KEY_EDAD, KEY_FECHA_NAC, KEY_SEXO, KEY_CONTRASENA, KEY_IMAGEN),
            "$KEY_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val usuario = Usuario(
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOMBRE)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_APELLIDO)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_CORREO)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_EDAD)),
                Date(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FECHA_NAC)).toLong()),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_SEXO)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_CONTRASENA)),
                cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_IMAGEN))
            )
            cursor.close()
            db.close()
            usuario
        } else {
            cursor?.close()
            db.close()
            null
        }
    }


    fun obtenerUsuarios(): ArrayList<Usuario> {
        val usuariosList = ArrayList<Usuario>()
        val db = readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME"
        val cursor: Cursor? = db.rawQuery(selectQuery, null)

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getInt(it.getColumnIndexOrThrow(KEY_ID))
                    val nombre = it.getString(it.getColumnIndexOrThrow(KEY_NOMBRE))
                    val apellido = it.getString(it.getColumnIndexOrThrow(KEY_APELLIDO))
                    val correo = it.getString(it.getColumnIndexOrThrow(KEY_CORREO))
                    val edad = it.getInt(it.getColumnIndexOrThrow(KEY_EDAD))
                    val fechaNacMillis = it.getLong(it.getColumnIndexOrThrow(KEY_FECHA_NAC))
                    val fechaNac = Date(fechaNacMillis)
                    val sexo = it.getString(it.getColumnIndexOrThrow(KEY_SEXO))
                    val contrasena = it.getString(it.getColumnIndexOrThrow(KEY_CONTRASENA))
                    val imagen = it.getBlob(it.getColumnIndexOrThrow(KEY_IMAGEN))

                    val usuario = Usuario(id, nombre, apellido, correo, edad, fechaNac, sexo, contrasena, imagen)
                    usuariosList.add(usuario)
                } while (it.moveToNext())
            }
        }

        cursor?.close()
        return usuariosList
    }
    fun eliminarUsuario(idUsuario: Int): Boolean {
        val db = writableDatabase
        val whereClause = "$KEY_ID = ?"
        val whereArgs = arrayOf(idUsuario.toString())

        val deletedRows = db.delete(TABLE_NAME, whereClause, whereArgs)
        db.close()

        return deletedRows > 0
    }
    fun verificarUsuario(correo: String, contrasena: String): Usuario? {
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $KEY_CORREO = ? AND $KEY_CONTRASENA = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(correo, contrasena))
        var usuario: Usuario? = null

        try {
            if (cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOMBRE))
                val apellido = cursor.getString(cursor.getColumnIndexOrThrow(KEY_APELLIDO))
                val edad = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_EDAD))
                val fechaNacTimestamp = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_FECHA_NAC))
                val sexo = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SEXO))
                val imagen = cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_IMAGEN))
                val fechaNac = Date(fechaNacTimestamp)

                usuario = Usuario(id, nombre, apellido, correo, edad, fechaNac, sexo, contrasena, imagen)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor.close()
            db.close()
        }

        return usuario
    }
}
