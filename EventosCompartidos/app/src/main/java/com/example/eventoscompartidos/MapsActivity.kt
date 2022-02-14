package com.example.eventoscompartidos

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import assistant.Auxiliar
import assistant.BDFirestore

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.eventoscompartidos.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.android.synthetic.main.activity_maps.*
import model.Evento
import model.Localizacion
import model.Lugar
import model.MapsOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var myUbication: LatLng
    lateinit var opcion: MapsOptions
    lateinit var evento: Evento

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bun: Bundle = intent.extras!!
        opcion = bun.getSerializable("opcion") as MapsOptions
        if (opcion == MapsOptions.ADD_PLACES)
            evento = bun.getSerializable("evento") as Evento

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMapActivity) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnBuscarDir.setOnClickListener {
            val dir = edDireccionMaps.text.toString().trim()
            if (dir.isNotEmpty()) {
                val coder = Geocoder(this)
                try {
                    val location = coder.getFromLocationName(dir, 1)[0]
                    val loc = LatLng(location.latitude, location.longitude)
                    irPunto(loc)
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        getString(R.string.strDirNoEncontrada),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun irPunto(loc: LatLng) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(loc, 18f), 2000, null
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        enableMyLocation()
        obtenerMiUbi()
        irMyUbication()
        map.setOnMapClickListener(this)
        if (opcion == MapsOptions.ADD_PLACES) toMarkPlaces()
    }

    private fun toMarkPlaces() {
        map.addMarker(
            MarkerOptions().position(evento.localizacionPuntoReunion()).title(evento.nombre)
                .snippet("${evento.fecha} ${evento.hora}")
        )
        for (lugar in evento.lugares) {
            map.addMarker(
                MarkerOptions().position(lugar.latLng()).title(lugar.nombre).icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
            )
        }
    }

    private fun irMyUbication() {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(myUbication, 18f), 1000, null
        )
    }

    @SuppressLint("MissingPermission")
    private fun obtenerMiUbi() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val ubi = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        myUbication = LatLng(ubi!!.latitude, ubi.longitude)
    }

    override fun onMapClick(loc: LatLng) {
        when (opcion) {
            MapsOptions.CHANGE_REUNION -> changeReunion(loc)
            MapsOptions.ADD_PLACES -> addPlace(loc)
        }
    }

    private fun addPlace(loc: LatLng) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.strConfirmar))
            .setMessage(getString(R.string.strConfirmarNuevoLugar) + "\n$loc")
            .setPositiveButton(getString(R.string.strAceptar)) { view, _ ->
                pedirNombreLugar(loc)
                view.dismiss()
            }
            .setNegativeButton(getString(R.string.strCancelar)) { view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .create().show()
    }

    private fun pedirNombreLugar(loc: LatLng) {
        val dialog = layoutInflater.inflate(R.layout.dialog_pide_string, null)
        val edNombre = dialog.findViewById<EditText>(R.id.edStringDialog)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.strNombreLugar))
            .setView(dialog)
            .setPositiveButton(getString(R.string.strAceptar)) { view, _ ->
                val nombre = edNombre.text.toString()
                map.addMarker(
                    MarkerOptions().position(loc).title(nombre).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                )
                val lugar =
                    Lugar(nombre, Localizacion(loc.latitude, loc.longitude))
                evento.addPlace(lugar)
                BDFirestore.actualizarListaLugares(evento)
                view.dismiss()
            }
            .setNegativeButton(getString(R.string.strCancelar)) { view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .create().show()
    }

    private fun changeReunion(loc: LatLng) {
        map.addMarker(MarkerOptions().position(loc))
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.strConfirmar))
            .setMessage(getString(R.string.strConfirmarPuntoReunion) + "\n$loc")
            .setPositiveButton(getString(R.string.strAceptar)) { view, _ ->
                val localizacion = Localizacion(loc.latitude, loc.longitude)
                intent.putExtra("result", localizacion)
                setResult(RESULT_OK, intent)
                finish()
                view.dismiss()
            }
            .setNegativeButton(getString(R.string.strCancelar)) { view, _ ->
                map.clear()
                view.dismiss()
            }
            .setCancelable(false)
            .create().show()
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
                Auxiliar.LOCATION_REQUEST_CODE
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