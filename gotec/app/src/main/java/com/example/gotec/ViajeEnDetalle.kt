package com.example.gotec

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

class ViajeEnDetalle : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var btnSendMessage: Button
    private var route: VerViajes.Route? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viaje_en_detalle)


        route = intent.getSerializableExtra("route") as? VerViajes.Route


        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        btnSendMessage = findViewById(R.id.btnSendMessage)

        btnSendMessage.setOnClickListener {
                val intent = Intent(this, Chat::class.java)

                intent.putExtra("passengerId", FirebaseAuth.getInstance().currentUser?.uid)
                intent.putExtra("driverId", route?.userId)
                startActivity(intent)
            }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (route?.ruta.isNullOrEmpty()) {
            Toast.makeText(this, "No se encontraron coordenadas para la ruta", Toast.LENGTH_SHORT).show()
            return
        }

        val routeCoordinates = route!!.ruta!!.map { LatLng(it["lat"]!!, it["lng"]!!) }
        val origin = routeCoordinates.first()
        val destination = routeCoordinates.last()


        fetchRoute(origin, destination, googleMap)
    }


    private fun fetchRoute(origin: LatLng, destination: LatLng, googleMap: GoogleMap) {
        val apiKey = "AIzaSyBVxyQPkFQ0l-3wZr-aoeeXFABK2EiKseM" // Reemplaza con tu clave de API de Google

        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&key=$apiKey"


        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                val jsonResponse = JSONObject(response)
                val routes = jsonResponse.getJSONArray("routes")
                if (routes.length() > 0) {
                    val points = routes.getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")


                    val path = decodePolyline(points)
                    googleMap.addPolyline(
                        PolylineOptions().addAll(path).color(Color.BLUE).width(10f)
                    )


                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 12f))
                } else {
                    Toast.makeText(this, "No se encontraron rutas disponibles", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error al obtener la ruta: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
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

            val point = LatLng(lat / 1E5, lng / 1E5)
            poly.add(point)
        }
        return poly
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}