package com.example.gotec

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class PasswordOlvidada : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_olvidada)
        var emailview=findViewById<EditText>(R.id.email)
        var cambiar=findViewById<Button>(R.id.cambiarContraseña)

        cambiar.setOnClickListener {
            val email = emailview.text.toString()
            sendPasswordResetEmail(email)

        }

    }
    private fun sendPasswordResetEmail(email: String) {
        val auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("es") // Configura el idioma para el correo

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Correo de restablecimiento enviado", Toast.LENGTH_SHORT).show()
                } else {
                    // Manejo de errores
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    Toast.makeText(this, "Error al enviar correo: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
