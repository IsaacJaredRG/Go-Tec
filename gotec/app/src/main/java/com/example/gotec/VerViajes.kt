package com.example.gotec

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.Serializable


class VerViajes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_viajes)

        // Configurar RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar Adaptador con un callback
        val adapter = RouteAdapter(emptyList()) { route ->
            // Navegar a la nueva actividad con los detalles
            val intent = Intent(this, ViajeEnDetalle::class.java)
            intent.putExtra("route", route) // Pasar el objeto Route
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Cargar rutas desde Firebase
        loadRoutes { rutas ->
            adapter.updateRoutes(rutas) // Actualizar el adaptador con las rutas cargadas
        }
    }

    // Clase RouteAdapter (mantener igual)
    class RouteAdapter(
        private var routes: List<Route>,
        private val onRouteClick: (Route) -> Unit
    ) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

        class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvHora: TextView = itemView.findViewById(R.id.tvHora)
            val tvTarifa: TextView = itemView.findViewById(R.id.tvTarifa)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ruta, parent, false)
            return RouteViewHolder(view)
        }

        override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
            val route = routes[position]
            holder.tvHora.text = "Hora: ${route.hora}"
            holder.tvTarifa.text = "Tarifa: ${route.tarifa}"

            // Asigna el listener de clics
            holder.itemView.setOnClickListener {
                onRouteClick(route) // Llama al callback
            }
        }

        override fun getItemCount(): Int = routes.size

        fun updateRoutes(newRoutes: List<Route>) {
            routes = newRoutes
            notifyDataSetChanged()
        }
    }

    // Clase Route (asegúrate de que sea Serializable o Parcelable)
    data class Route(
        val userId: String? = null,
        val hora: String? = null,
        val tarifa: String? = null,
        val ruta: List<Map<String, Double>>? = null
    ) : Serializable

    // Método para cargar rutas desde Firebase
    private fun loadRoutes(onRoutesLoaded: (List<Route>) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("routes")

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val routes = mutableListOf<Route>()
                for (routeSnapshot in snapshot.children) {
                    val route = routeSnapshot.getValue(Route::class.java)
                    route?.let { routes.add(it) }
                }
                onRoutesLoaded(routes)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@VerViajes, "Error al cargar las rutas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}