package com.cibertec.boutique

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    lateinit var tvRegistro:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvRegistro = findViewById(R.id.tvRegistro)
        val text: String = "No tienes una cuenta? Registrate"

        val spannableString = SpannableString(text)
        val boldSpan = StyleSpan(Typeface.BOLD)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@MainActivity, InicioActivity::class.java)
                startActivity(intent)
            }
        }

        val start = text.indexOf("Registrate")
        val end = start + "Registrate".length

        spannableString.setSpan(boldSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvRegistro.text = spannableString
        tvRegistro.movementMethod = LinkMovementMethod.getInstance()
    }

    fun sendInicio(v: View){
        val intent = Intent(this, InicioActivity::class.java)
        startActivity(intent)
    }


}