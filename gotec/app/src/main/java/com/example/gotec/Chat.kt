package com.example.gotec

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
class Chat : AppCompatActivity() {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendMessageButton: Button

    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    private var passengerId: String? = null
    private var driverId: String? = null
    private var currentUserId: String? = null // ID del usuario actual (obtenido desde Firebase Auth)
    private var otherUserId: String? = null  // ID del otro usuario en el chat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Obtener IDs del intent
        passengerId = intent.getStringExtra("passengerId")
        driverId = intent.getStringExtra("driverId")

        // Obtener el UID del usuario actual desde Firebase Authentication
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Configurar el ID del otro usuario en el chat
        otherUserId = if (currentUserId == passengerId) driverId else passengerId

        // Inicializar vistas
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendMessageButton = findViewById(R.id.sendMessageButton)

        // Configurar RecyclerView
        adapter = ChatAdapter(messages, currentUserId ?: "")
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = adapter

        // Escuchar mensajes en Firebase
        listenToMessages()

        // Enviar mensaje
        sendMessageButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun listenToMessages() {
        val chatId = "${passengerId}_${driverId}"
        val database = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    message?.let { messages.add(it) }
                }
                adapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(messages.size - 1) // Desplazar al último mensaje
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Chat, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendMessage() {
        val text = messageEditText.text.toString().trim()
        if (text.isNotEmpty()) {
            val chatId = "${passengerId}_${driverId}"
            val database = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages")

            // Crear el mensaje con senderId y receiverId
            val message = Message(
                senderId = currentUserId, // ID del remitente
                receiverId = otherUserId, // ID del receptor
                message = text,
                timestamp = System.currentTimeMillis()
            )

            database.push().setValue(message).addOnCompleteListener {
                if (it.isSuccessful) {
                    messageEditText.text.clear() // Limpiar campo de texto
                } else {
                    Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    class ChatAdapter(
        private val messages: List<Message>,
        private val currentUserId: String
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val VIEW_TYPE_SENT = 1
            private const val VIEW_TYPE_RECEIVED = 2
        }

        override fun getItemViewType(position: Int): Int {
            val message = messages[position]
            return if (message.senderId == currentUserId) {
                VIEW_TYPE_SENT // Mensaje enviado por el usuario actual
            } else {
                VIEW_TYPE_RECEIVED // Mensaje recibido del otro usuario
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == VIEW_TYPE_SENT) {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.message_sent, parent, false)
                SentMessageViewHolder(view)
            } else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.message_recived, parent, false)
                ReceivedMessageViewHolder(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val message = messages[position]
            if (holder is SentMessageViewHolder) {
                holder.bind(message)
            } else if (holder is ReceivedMessageViewHolder) {
                holder.bind(message)
            }
        }

        override fun getItemCount(): Int = messages.size

        class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)

            fun bind(message: Message) {
                tvMessage.text = message.message
            }
        }

        class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)

            fun bind(message: Message) {
                tvMessage.text = message.message
            }
        }
    }

    data class Message(
        val senderId: String? = null, // ID del remitente
        val receiverId: String? = null, // ID del receptor
        val message: String? = null,
        val timestamp: Long? = null
    )
}
