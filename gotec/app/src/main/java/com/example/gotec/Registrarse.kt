package com.example.gotec

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Registrarse : AppCompatActivity() {

    private lateinit var Et_nombre_usuario : EditText
    private lateinit var Et_correo_usuario : EditText
    private lateinit var Et_password_usuario : EditText
    private lateinit var Et_password_confirmar_usuario : EditText
    private lateinit var Btn_registrar : Button
    private lateinit var radioGroup : RadioGroup


    private lateinit var auth : FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = "Registrarse"
        setContentView(R.layout.activity_registrarse)
        InicializarVariables()
        Btn_registrar.setOnClickListener() {
        ValidarDatos()
        }
    }



    private fun InicializarVariables(){
         Et_nombre_usuario = findViewById(R.id.nombre)
         Et_correo_usuario = findViewById(R.id.correo)
         Et_password_usuario = findViewById(R.id.password)
         Et_password_confirmar_usuario= findViewById(R.id.confirmPassword)
         Btn_registrar = findViewById(R.id.btnRegistro)
         auth = FirebaseAuth.getInstance()
         radioGroup = findViewById(R.id.pajaseroConductorGrupo)
    }

    private fun ValidarDatos() {
        val nombre_usuario : String = Et_nombre_usuario.text.toString()
        val email : String = Et_correo_usuario.text.toString()
        val password : String = Et_password_usuario.text.toString()
        val confirmar_password: String = Et_password_confirmar_usuario.text.toString()
        val selectedRadioButtonId = radioGroup.checkedRadioButtonId
        val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
        val rol = selectedRadioButton.text.toString()



        if (nombre_usuario.isEmpty()){
            Toast.makeText(applicationContext, "Ingrese nombre de usuatrio", Toast.LENGTH_SHORT).show()
        }
        else if(email.isEmpty()){
            Toast.makeText(applicationContext, "Ingrese su correo", Toast.LENGTH_SHORT).show()
                    //aqui va otro if para comprobar si tiene termiancion @pabellon.tecnm.com
        }
        else if(password.isEmpty()) {
            Toast.makeText(applicationContext, "Ingrese su contraseña", Toast.LENGTH_SHORT).show()
        }
        else if(confirmar_password.isEmpty()) {
            Toast.makeText(applicationContext, "Porfavor repita su contraseña", Toast.LENGTH_SHORT).show()
        }
        else if(!password.equals(confirmar_password)){
            Toast.makeText(applicationContext, "las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
        }
            else{
                RegistrarUsuario(email, password)
        }
    }

    private fun RegistrarUsuario(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task->
            if(task.isSuccessful){
              var uid : String = ""
              uid = auth.currentUser!!.uid
              reference = FirebaseDatabase.getInstance().reference.child("Usuarios").child(uid)
              val hashmap = HashMap<String, Any>()
              val h_nombre_usuario : String = Et_nombre_usuario.text.toString()
              val h_email : String = Et_correo_usuario.text.toString()
              val selectedRadioButtonId = radioGroup.checkedRadioButtonId
              val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
              val h_rol = selectedRadioButton.text.toString()

              hashmap ["uid"]=uid
              hashmap ["n_usuario"]=h_nombre_usuario
              hashmap ["email"]=h_email
              hashmap ["rol"]=h_rol

              reference.updateChildren(hashmap).addOnCompleteListener{task2->
                  if (task2.isSuccessful){
                      val intent = Intent (this@Registrarse,Login::class.java)
                      Toast.makeText(applicationContext,"se ha registrado con exito",Toast.LENGTH_SHORT).show()
                      startActivity(intent)
                  }
              }.addOnFailureListener{e->
                  Toast.makeText(applicationContext,"${e.message}",Toast.LENGTH_SHORT).show()
              }



            }else{
                Toast.makeText(applicationContext,"ha ocurrido un error",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{e->
            Toast.makeText(applicationContext,"${e.message}",Toast.LENGTH_SHORT).show()
        }
    }
}