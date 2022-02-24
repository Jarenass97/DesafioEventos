package com.example.eventoscompartidos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import assistant.Auxiliar
import com.example.eventoscompartidos.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import model.Evento
import model.Localizacion

class QuedadasActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationChangeListener {

    private val LOCATION_REQUEST_CODE: Int = 0
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    lateinit var evento: Evento
    lateinit var myUbication: LatLng
    var line: Polyline? = null
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quedadas)

        val bun: Bundle = intent.extras!!
        evento = bun.getSerializable("evento") as Evento

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_maptype, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mNormal -> map.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.mHibrido -> map.mapType = GoogleMap.MAP_TYPE_HYBRID
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        enableMyLocation()
        obtenerMiUbi()
        title = evento.nombre
        colocarMarcador()
        generarRuta()
        map.setOnMyLocationClickListener(this)
        map.setOnMyLocationChangeListener(this)
        irAMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun obtenerMiUbi() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val ubi = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        myUbication = LatLng(ubi!!.latitude, ubi.longitude)
    }

    private fun generarRuta() {
        line?.remove()
        line = map.addPolyline(PolylineOptions().run {
            add(myUbication, evento.localizacionPuntoReunion())
            color(Color.BLUE)
            width(9f)
        })
    }


    private fun irAMyLocation() {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(myUbication, 18f),
            2000, null
        )
    }

    private fun colocarMarcador() {
        map.addMarker(
            MarkerOptions().position(evento.localizacionPuntoReunion()).title(evento.nombre)
                .snippet("${evento.fecha} ${evento.hora}")
        )
        pintarCirculo()
    }


    private fun pintarCirculo() {
        map.addCircle(CircleOptions().run {
            center(evento.localizacionPuntoReunion())
            radius(Auxiliar.RADIO_CIRCULO)
            strokeColor(Color.BLUE)
            fillColor(R.color.circle_map)
        })
    }

    override fun onMyLocationChange(ubication: Location) {
        myUbication = LatLng(ubication.latitude, ubication.longitude)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(myUbication, 18f), 1000, null
        )
        generarRuta()
    }


    override fun onMyLocationClick(loc: Location) {
        val distance = getDistancia()
        if (distance < Auxiliar.RADIO_CIRCULO) {
            if (!evento.estoyPresente()) {
                evento.indicarPresencialidad()
                Toast.makeText(
                    this,
                    getString(R.string.strAgradecerPuntualidad),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this, getString(R.string.strYaPresente),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this, getString(R.string.strFueraDelCirculo), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDistancia(): Float {
        val marker = evento.localizacionPuntoReunion()
        val myLocation = Location("")
        myLocation.latitude = myUbication.latitude
        myLocation.longitude = myUbication.longitude
        val eventLocation = Location("")
        eventLocation.latitude = marker.latitude
        eventLocation.longitude = marker.longitude
        return myLocation.distanceTo(eventLocation)
    }

    @SuppressLint("MissingPermission")
    fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (isPermissionsGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    fun isPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingSuperCall", "MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    this,
                    "Para activar la localización ve a ajustes y acepta los permisos",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {}
        }
    }

}