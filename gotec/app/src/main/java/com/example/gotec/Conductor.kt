package com.example.gotec

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Conductor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conductor)

        val registrar=findViewById<Button>(R.id.BtnRegistrarViaje)
        val borrar=findViewById<Button>(R.id.btnborrar)
        val cerrarsesion=findViewById<Button>(R.id.BtnSalirConductor)
        registrar.setOnClickListener {
            val intent = Intent(this@Conductor, RegistrarViaje::class.java)
            startActivity(intent)
            Toast.makeText(applicationContext, "Registrar Viaje", Toast.LENGTH_SHORT).show()
        }
        borrar.setOnClickListener {
            deleteRouteFromFirebase()
        }
        cerrarsesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent=Intent(this@Conductor,Login::class.java)
            Toast.makeText(applicationContext,"has cerrados sesión",Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }
        val btnVerChats = findViewById<Button>(R.id.BtnVerMensaje)
        btnVerChats.setOnClickListener {
            val intent = Intent(this, lista_chats::class.java)
            startActivity(intent)
        }


    }
    private fun deleteRouteFromFirebase() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = firebaseUser?.uid

        if (userId == null) {
            Toast.makeText(this, "No se pudo obtener la información del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        // Consulta para buscar el viaje del usuario
        val database = FirebaseDatabase.getInstance().getReference("routes")
        val query = database.orderByChild("userId").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Recorremos los resultados y eliminamos el registro
                    for (routeSnapshot in snapshot.children) {
                        routeSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(this@Conductor, "Viaje eliminado con éxito", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { error ->
                                Toast.makeText(this@Conductor, "Error al eliminar el viaje: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // Si no hay viajes registrados para este usuario
                    Toast.makeText(this@Conductor, "No se encontró un viaje registrado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Conductor, "Error al buscar el viaje: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}