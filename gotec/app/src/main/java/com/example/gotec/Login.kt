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
import com.google.firebase.FirebaseApp

class Login : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    private lateinit var reference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.contra)
        val BotonLogin = findViewById<Button>(R.id.login)
        val registrarse = findViewById<TextView>(R.id.registrarse)
        val passwordOlvidada = findViewById<TextView>(R.id.olvidada)

        registrarse.setOnClickListener {
            val intent = Intent(this@Login, Registrarse::class.java)
            Toast.makeText(applicationContext, "Registrarse", Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }

        passwordOlvidada.setOnClickListener {
            val intent= Intent(this@Login,PasswordOlvidada::class.java)
            Toast.makeText(applicationContext,"contraseña olvidada", Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }


        BotonLogin.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()

            // Autenticacion
            if (email.isNotEmpty() && password.isNotEmpty()) {
                LoginUsuario(email,password)
                Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, completa los campos.", Toast.LENGTH_SHORT).show()
            }


        }
    }

    private fun LoginUsuario(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    val uid = firebaseUser.uid

                    val database = FirebaseDatabase.getInstance().reference
                    val userRef = database.child("Usuarios").child(uid)

                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                // Obtener el campo "rol"
                                val rol = snapshot.child("rol").getValue(String::class.java)
                                if (rol != null) {
                                    // Redirigir según el rol del usuario
                                    when (rol) {
                                        "Pasajero" -> {
                                            val intent = Intent(this@Login, PasajeroActivity::class.java)
                                            startActivity(intent)
                                            Toast.makeText(
                                                applicationContext,
                                                "Bienvenido, Pasajero",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finish()
                                        }
                                        "Conductor" -> {
                                            val intent = Intent(this@Login, Conductor::class.java)
                                            startActivity(intent)
                                            Toast.makeText(
                                                applicationContext,
                                                "Bienvenido, Conductor",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            finish()
                                        }
                                    }
                                } else {
                                    // Si el campo "rol" no existe
                                    Toast.makeText(
                                        applicationContext,
                                        "El usuario no tiene rol asignado.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                // Si no existe el nodo del usuario
                                Toast.makeText(
                                    applicationContext,
                                    "Usuario no encontrado en la base de datos.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Manejo de errores de la consulta
                            Toast.makeText(
                                applicationContext,
                                "Error al consultar la base de datos: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
            } else {
                // Mostrar error de autenticación
                val errorMessage = task.exception?.message ?: "Error al iniciar sesión"
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun ComprobarSesion() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            // Consultar rol
            val uid = firebaseUser.uid
            val database = FirebaseDatabase.getInstance().reference
            val userRef = database.child("Usuarios").child(uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rol = snapshot.child("rol").getValue(String::class.java)
                    Log.d("ComprobarSesion", "Datos del usuario: ${snapshot.value}")
                    Log.d("ComprobarSesion", "Rol obtenido: $rol")

                    when (rol) {
                        "Pasajero" -> {
                            val intent = Intent(this@Login, PasajeroActivity::class.java)
                            Toast.makeText(applicationContext, "La sesión está activa como Pasajero", Toast.LENGTH_SHORT).show()
                            startActivity(intent)
                            finish()
                        }
                        "Conductor" -> {
                            val intent = Intent(this@Login, Conductor::class.java)
                            Toast.makeText(applicationContext, "La sesión está activa como Conductor", Toast.LENGTH_SHORT).show()
                            startActivity(intent)
                            finish()
                        }
                        else -> {
                            Log.e("ComprobarSesion", "Rol desconocido: $rol")
                            Toast.makeText(applicationContext, "Error: Rol desconocido", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Error al consultar los datos: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Log.d("ComprobarSesion", "Usuario no autenticado")
        }
    }

    override fun onStart(){
        ComprobarSesion()
        super.onStart()

    }

}