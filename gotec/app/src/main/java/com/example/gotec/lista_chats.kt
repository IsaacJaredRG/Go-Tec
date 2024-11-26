package com.example.gotec

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class lista_chats : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private val chats = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_chats)

        recyclerView = findViewById(R.id.chatsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Configurar el adaptador
        adapter = ChatListAdapter(chats) { chatItem ->
            // Navegar al chat seleccionado
            val intent = Intent(this, Chat::class.java)
            intent.putExtra("chatId", chatItem.chatId) // ID del chat
            intent.putExtra("passengerId", chatItem.passengerId) // ID del pasajero
            intent.putExtra("driverId", chatItem.driverId) // ID del conductor
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadChats()
    }

    private fun loadChats() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("chats")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chats.clear()
                for (chatSnapshot in snapshot.children) {
                    val chatId = chatSnapshot.key ?: continue

                    // Separar el ID del chat para obtener los IDs del pasajero y conductor
                    val ids = chatId.split("_")
                    if (ids.size != 2) continue
                    val passengerId = ids[0]
                    val driverId = ids[1]

                    // Verificar si el usuario actual es parte del chat
                    if (currentUserId == passengerId || currentUserId == driverId) {
                        // Obtener el último mensaje del chat
                        val messages = chatSnapshot.child("messages").children
                        val lastMessageSnapshot = messages.lastOrNull()
                        val lastMessage = lastMessageSnapshot?.child("message")?.getValue(String::class.java)
                        val timestamp = lastMessageSnapshot?.child("timestamp")?.getValue(Long::class.java)

                        // Determinar quién es el "otro usuario" en el chat
                        val otherUserId = if (currentUserId == passengerId) driverId else passengerId

                        // Crear un objeto ChatItem y agregarlo a la lista
                        val chatItem = ChatItem(
                            chatId = chatId,
                            passengerId = passengerId,
                            driverId = driverId,
                            otherUserId = otherUserId,
                            lastMessage = lastMessage,
                            timestamp = timestamp
                        )
                        chats.add(chatItem)
                    }
                }
                chats.sortByDescending { it.timestamp } // Ordenar por el último mensaje
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@lista_chats, "Error al cargar los chats: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    class ChatListAdapter(
        private val chats: List<ChatItem>,
        private val onChatClick: (ChatItem) -> Unit
    ) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

        class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
            val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
            return ChatViewHolder(view)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            val chat = chats[position]
            holder.tvUserName.text = chat.otherUserId // Mostrar el ID del otro usuario (puedes reemplazar esto con un nombre si lo tienes)
            holder.tvLastMessage.text = chat.lastMessage
            holder.itemView.setOnClickListener { onChatClick(chat) }
        }

        override fun getItemCount(): Int = chats.size
    }

    data class ChatItem(
        val chatId: String,
        val passengerId: String,
        val driverId: String,
        val otherUserId: String,
        val lastMessage: String?,
        val timestamp: Long?
    )
}