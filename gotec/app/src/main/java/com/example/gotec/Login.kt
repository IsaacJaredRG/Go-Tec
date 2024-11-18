package com.example.gotec

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.contra)
        val BotonLogin = findViewById<Button>(R.id.login)
        val registrarse= findViewById<TextView>(R.id.registrarse)

        registrarse.setOnClickListener {
            Toast.makeText(this, "Redirigiendo a registro...", Toast.LENGTH_SHORT).show()
        }
        BotonLogin.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()

            // Autenticacion
            if (email.isNotEmpty() && password.isNotEmpty()) {
                Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, completa los campos.", Toast.LENGTH_SHORT).show()
            }


        }
    }
}