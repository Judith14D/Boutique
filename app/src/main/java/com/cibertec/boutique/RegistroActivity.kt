package com.cibertec.boutique

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cibertec.boutique.clase.Usuario
import com.cibertec.boutique.db.DBHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RegistroActivity : AppCompatActivity() {
    private lateinit var etFechaNac: EditText
    private lateinit var textViewCuentaYa: TextView
    private lateinit var etNombres: EditText
    private lateinit var etApellidos: EditText
    private lateinit var etEdad: EditText
    private lateinit var etEmail: EditText
    private lateinit var etContrasena: EditText
    private lateinit var radioGroupSexo: RadioGroup
    private lateinit var radioButtonFemenino: RadioButton
    private lateinit var radioButtonMasculino: RadioButton
    private lateinit var checkBoxAcuerdo: CheckBox
    private lateinit var btnSendRegistro: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etFechaNac = findViewById(R.id.editTextDate)
        textViewCuentaYa = findViewById(R.id.textViewCuentaYa)
        etNombres = findViewById(R.id.etNombres)
        etApellidos = findViewById(R.id.etApellidos)
        etEdad = findViewById(R.id.etEdad)
        etEmail = findViewById(R.id.etEmail)
        etContrasena = findViewById(R.id.etContrasena)
        radioGroupSexo = findViewById(R.id.radioGroupSexo)
        checkBoxAcuerdo = findViewById(R.id.checkBoxAcuerdo)
        btnSendRegistro = findViewById(R.id.btnSendRegistro)
        textViewCuentaYa = findViewById(R.id.textViewCuentaYa)
        radioButtonFemenino = findViewById(R.id.radioButtonFemenino)
        radioButtonMasculino = findViewById(R.id.radioButtonMasculino)


        //DATEPICKER
        etFechaNac.setOnClickListener {
            showDatePickerDialog(etFechaNac)
        }

        //SPAN STRING
        val text: String = " Ya tienes una cuenta? Inicia sesión"
        val spannableString = SpannableString(text)
        val colorSpan = ForegroundColorSpan(Color.parseColor("#777AF5"))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegistroActivity, InicioActivity::class.java)
                startActivity(intent)
            }
        }

        val start = text.indexOf("Inicia sesión")
        val end = start + "Inicia sesión".length

        spannableString.setSpan(colorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textViewCuentaYa.text = spannableString
        textViewCuentaYa.movementMethod = LinkMovementMethod.getInstance()

    }

    //DATE PICKER PT.2
    private fun showDatePickerDialog(fechaNac: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                fechaNac.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()

        btnSendRegistro.setOnClickListener {
            if (checkBoxAcuerdo.isChecked) {
                if (validateInputs()) {
                    Log.d("RegistroActivity", "Inputs validados correctamente")
                    registerUser()
                } else {
                    Toast.makeText(this, "Por favor, complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Debe aceptar la política de privacidad", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun validateInputs(): Boolean {
        val nombre = etNombres.text.toString().trim()
        val apellido = etApellidos.text.toString().trim()
        val edadStr = etEdad.text.toString().trim()
        val edad = edadStr.toIntOrNull()
        val fechaNacStr = etFechaNac.text.toString().trim()
        val sexoId = radioGroupSexo.checkedRadioButtonId
        val correo = etEmail.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()

        Log.d("RegistroActivity", "Nombre: $nombre, Apellido: $apellido, Edad: $edad, Fecha Nac: $fechaNacStr, Sexo ID: $sexoId, Correo: $correo, Contraseña: $contrasena")

        val sexoSeleccionado = findViewById<RadioButton>(sexoId)?.text?.toString()
        val sexoValido = !sexoSeleccionado.isNullOrEmpty()

        var isValid = true
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        dateFormat.isLenient = false
        if (nombre.isEmpty()) {
            etNombres.error = "El nombre no puede estar vacío"
            isValid = false
        }
        if (apellido.isEmpty()) {
            etApellidos.error = "El apellido no puede estar vacío"
            isValid = false
        }
        if (edad == null || edad < 18 || edad > 100) {
            etEdad.error = "La edad debe estar entre 18 y 100 años"
            isValid = false
        }
        if (fechaNacStr.isEmpty()) {
            etFechaNac.error = "La fecha de nacimiento no puede estar vacía"
            isValid = false
        } else {
            try {
                val fechaNac = dateFormat.parse(fechaNacStr)
                if (fechaNac.after(Date())) {
                    etFechaNac.error = "La fecha de nacimiento no puede ser posterior a la fecha actual"
                    isValid = false
                }
            } catch (e: Exception) {
                etFechaNac.error = "Formato de fecha inválido. Utilice dd/MM/yyyy"
                isValid = false
            }
        }
        if (!sexoValido) {
            Toast.makeText(this, "Debe seleccionar un sexo", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        if (correo.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etEmail.error = "Ingrese un correo válido"
            isValid = false
        }
        if (contrasena.isEmpty() || contrasena.length < 5) {
            etContrasena.error = "La contraseña debe tener al menos 5 caracteres"
            isValid = false
        }

        return isValid
    }



    private fun registerUser() {
        val nombre = etNombres.text.toString().trim()
        val apellido = etApellidos.text.toString().trim()
        val edadStr = etEdad.text.toString().trim()
        val edad = edadStr.toIntOrNull()
        val fechaNacStr = etFechaNac.text.toString().trim()
        val sexoId = radioGroupSexo.checkedRadioButtonId
        val sexo = findViewById<RadioButton>(sexoId)?.text?.toString() ?: ""
        val correo = etEmail.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()

        val fechaNac = SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(fechaNacStr) ?: Date()

        Log.d("RegistroActivity", "Datos a registrar: Nombre: $nombre, Apellido: $apellido, Edad: $edad, Fecha Nac: $fechaNac, Sexo: $sexo, Correo: $correo, Contraseña: $contrasena")

        val usuario = Usuario(
            nombre = nombre,
            apellido = apellido,
            correo = correo,
            edad = edad ?: 0,
            fechaNac = fechaNac,
            sexo = sexo,
            contrasena = contrasena,
            imagen = null
        )

        try {
            val dbHelper = DBHelper(this)
            val id = dbHelper.insertarUsuario(usuario)

            if (id > 0) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, InicioActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RegistroActivity", "Error en el registro: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}



