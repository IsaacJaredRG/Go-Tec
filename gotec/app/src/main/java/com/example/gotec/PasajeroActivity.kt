package com.example.gotec

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import java.util.zip.Inflater


class PasajeroActivity : AppCompatActivity() {
    lateinit var btn_CerrarSesion:Button
    lateinit var btn_VerViajes:Button
    lateinit var btn_VerMensajes:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pasajero)
        btn_CerrarSesion=findViewById(R.id.BtnSalirPasajero)
        btn_VerViajes=findViewById(R.id.BtnVerViajes)

        btn_CerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent=Intent(this@PasajeroActivity,Login::class.java)
            Toast.makeText(applicationContext,"has cerrados sesión",Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }
        btn_VerViajes.setOnClickListener {
            val intent=Intent(this@PasajeroActivity,VerViajes::class.java)
            Toast.makeText(applicationContext,"viendo viajes",Toast.LENGTH_SHORT).show()
            startActivity(intent)
        }
        val btnVerChats = findViewById<Button>(R.id.BtnVerMensajePajasero)
        btnVerChats.setOnClickListener {
            val intent = Intent(this, lista_chats::class.java)
            startActivity(intent)
        }
    }


}

