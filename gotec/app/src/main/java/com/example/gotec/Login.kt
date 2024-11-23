package com.example.gotec

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {

    var firebaseUser: FirebaseUser? = null
    private lateinit var reference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.contra)
        val BotonLogin = findViewById<Button>(R.id.login)
        val registrarse = findViewById<TextView>(R.id.registrarse)

        registrarse.setOnClickListener {
            val intent = Intent(this@Login, Registrarse::class.java)
            Toast.makeText(applicationContext, "Registrarse", Toast.LENGTH_SHORT).show()
            startActivity(intent)
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

    private fun ComprobarSesion() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            //consultar rol
            val uid = firebaseUser.uid
            val database = FirebaseDatabase.getInstance().reference
            val userRef = database.child("Usuarios").child(uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Si la consulta es exitosa, obtenemos el rol
                    val rol = snapshot.child("rol").getValue(String::class.java)
                    Log.d("ComprobarSesion", "Datos del usuario: ${snapshot.value}")

                    if (rol != null) {
                        // Dependiendo del rol, realizamos las acciones necesarias
                        when (rol) {
                            "Pasajero" -> {
                                val intent= Intent(this@Login, PasajeroActivity::class.java)
                                Toast.makeText(applicationContext,"la sesión está activa", Toast.LENGTH_SHORT).show()
                                startActivity(intent)
                                finish()
                            }
                            "Conductor" -> {
                                val intent= Intent(this@Login, PasajeroActivity::class.java)
                                Toast.makeText(applicationContext,"la sesión está activa", Toast.LENGTH_SHORT).show()
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else {
                        // Si no se encuentra el rol
                        Toast.makeText(applicationContext,"no existe rol??????", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejo de errores de la consulta
                    Toast.makeText(applicationContext,"${error(Any())}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Si el usuario no está autenticado
            Log.d("ComprobarSesion", "Usuario no autenticado")
        }
    }
    override fun onStart(){
        ComprobarSesion()
        super.onStart()

    }

}