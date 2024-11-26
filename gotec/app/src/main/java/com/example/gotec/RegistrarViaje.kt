package com.example.gotec

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.FirebaseDatabase
import com.android.volley.Request
import com.android.volley.Response
import com.google.android.gms.maps.model.Marker
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject


class RegistrarViaje : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val routePoints = mutableListOf<LatLng>()
    private val markers = mutableListOf<Marker>() // Lista para almacenar los marcadores
    private var polyline: Polyline? = null
    private val repository = RouteRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_viaje)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this) ?: run {
            Toast.makeText(this, "Error al cargar el mapa", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.registrarViaje).setOnClickListener {
            confirmSaveRoute()
        }
    }

    class RouteRepository {
        private val database = FirebaseDatabase.getInstance().getReference("routes")

        fun saveRoute(
            routeData: Map<String, Any>,
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
        ) {
            val routeId = database.push().key
            if (routeId != null) {
                database.child(routeId).setValue(routeData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e.message ?: "Error desconocido") }
            } else {
                onFailure("Error al generar el ID de la ruta")
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        val inicio = LatLng(22.0162, -102.3489)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(inicio, 15f))

        googleMap.setOnMapClickListener { latLng ->
            if (routePoints.size < 2) {
                routePoints.add(latLng)
                val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(if (routePoints.size == 1) "Origen" else "Destino")
                        .icon(BitmapDescriptorFactory.defaultMarker(if (routePoints.size == 1) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED))
                )
                marker?.let { markers.add(it) } // Agregar el marcador a la lista

                if (routePoints.size == 2) {
                    // Llama a la API Directions solo cuando haya dos puntos
                    fetchRoute(routePoints.first(), routePoints.last())
                }
            } else {
                Toast.makeText(this, "Solo puedes seleccionar dos puntos", Toast.LENGTH_SHORT).show()
            }

            drawRoute(routePoints)
        }

        googleMap.setOnMarkerClickListener { marker ->
            // Eliminar el marcador seleccionado
            val index = markers.indexOf(marker)
            if (index != -1) {
                markers.removeAt(index) // Elimina el marcador de la lista
                routePoints.removeAt(index) // Elimina el punto correspondiente
                marker.remove() // Elimina el marcador del mapa
                drawRoute(routePoints) // Redibuja la ruta
            }
            true // Retorna true para evitar que el marcador muestre su ventana de información
        }
    }

    private fun fetchRoute(origin: LatLng, destination: LatLng) {
        val apiKey = "AIzaSyBVxyQPkFQ0l-3wZr-aoeeXFABK2EiKseM"
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&mode=driving&key=$apiKey"

        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val routes = jsonResponse.optJSONArray("routes")
                    if (routes != null && routes.length() > 0) {
                        val overviewPolyline = routes.getJSONObject(0)
                            .getJSONObject("overview_polyline")
                            .getString("points")
                        val decodedPath = decodePolyline(overviewPolyline)
                        drawRoute(decodedPath)
                    } else {
                        Toast.makeText(this, "No se encontraron rutas disponibles", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al procesar la respuesta: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error al obtener la ruta: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(stringRequest)
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return poly
    }

    private fun drawRoute(routePoints: List<LatLng>) {
        polyline?.remove() // Elimina la polilínea anterior si existe

        polyline = googleMap.addPolyline(
            PolylineOptions()
                .addAll(routePoints)
                .color(getColor(R.color.guindo)) // Asegúrate de que el color sea visible
                .width(10f) // Ajusta el grosor de la línea si es necesario
        )
    }

    private fun confirmSaveRoute() {
        AlertDialog.Builder(this)
            .setTitle("Guardar Ruta")
            .setMessage("¿Estás seguro de guardar esta ruta?")
            .setPositiveButton("Guardar") { _, _ -> saveRouteToFirebase() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveRouteToFirebase() {
        val edtHora = findViewById<EditText>(R.id.hora)
        val edtTarifa = findViewById<EditText>(R.id.tarifa)
        val hora = edtHora.text.toString().trim()
        val tarifa = edtTarifa.text.toString().trim()

        if (hora.isEmpty() || tarifa.isEmpty() || routePoints.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos y selecciona al menos un punto", Toast.LENGTH_SHORT).show()
            return
        }

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId = firebaseUser?.uid

        if (userId == null) {
            Toast.makeText(this, "No se pudo obtener la información del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si el usuario ya tiene un viaje
        val database = FirebaseDatabase.getInstance().getReference("routes")
        val query = database.orderByChild("userId").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // El usuario ya tiene un viaje registrado
                    Toast.makeText(this@RegistrarViaje, "Ya tienes un viaje registrado", Toast.LENGTH_SHORT).show()
                } else {
                    // Si no tiene viajes, permite guardar la nueva ruta
                    val rutaData = mapOf(
                        "userId" to userId,
                        "hora" to hora,
                        "tarifa" to tarifa,
                        "ruta" to routePoints.map { point -> mapOf("lat" to point.latitude, "lng" to point.longitude) }
                    )

                    repository.saveRoute(rutaData,
                        onSuccess = {
                            Toast.makeText(this@RegistrarViaje, "Ruta guardada con éxito", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(this@RegistrarViaje, "Error al guardar la ruta: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegistrarViaje, "Error al verificar el registro: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}