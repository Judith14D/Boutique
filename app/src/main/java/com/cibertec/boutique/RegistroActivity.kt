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
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegistroActivity : AppCompatActivity() {
    private lateinit var fechaNac: EditText
    private lateinit var textViewCuentaYa: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fechaNac = findViewById(R.id.editTextDate)
        textViewCuentaYa = findViewById(R.id.textViewCuentaYa)

        //DATEPICKER
        fechaNac.setOnClickListener {
            showDatePickerDialog(fechaNac)
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
    }


}