package com.example.eventoscompartidos

import adapters.UsuariosAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import assistant.Auxiliar
import assistant.BDFirestore
import assistant.DatePickerFragment
import assistant.TimePickerFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_gestion_evento_detalle.*
import model.Asistente
import model.Evento

class GestionEventoDetalle : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationChangeListener {
    private lateinit var evento: Evento
    lateinit var map: GoogleMap
    private val LOCATION_REQUEST_CODE: Int = 0
    lateinit var myUbication: LatLng
    lateinit var adaptadorAsistentes: UsuariosAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_evento_detalle)

        val bun: Bundle = intent.extras!!
        val idEvento = bun.getSerializable(getString(R.string.strEvento)) as String
        evento = BDFirestore.getEvento(idEvento)
        title = evento.nombre

        cargarMapa()
        cargarDatos()
    }

    private fun cargarDatos() {
        edFechaEventoDetalle.setText(evento.fecha)
        edHoraEventoDetalle.setText(evento.hora)
        rvAsistentes.setHasFixedSize(true)
        rvAsistentes.layoutManager = LinearLayoutManager(this)
        adaptadorAsistentes = UsuariosAdapter(this, evento.asistentes, evento)
        rvAsistentes.adapter = adaptadorAsistentes
        edFechaEventoDetalle.setOnClickListener {
            showDatePickerDialog(edFechaEventoDetalle)
        }
        edHoraEventoDetalle.setOnClickListener {
            showTimePickerDialog(edHoraEventoDetalle)
        }
    }

    private fun showTimePickerDialog(hora: EditText) {
        TimePickerFragment(hora, true, evento).show(supportFragmentManager, "timePicker")
    }

    private fun showDatePickerDialog(edFecha: EditText) {
        val newFragment = DatePickerFragment(edFecha, true, evento)
        newFragment.show(supportFragmentManager, "datePicker")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_evento_detalle, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miEditName -> editarNombre()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun editarNombre() {
        val dialog = layoutInflater.inflate(R.layout.dialog_edit_nombre_evento, null)
        val edNombre = dialog.findViewById<EditText>(R.id.edNombreEventoEditor)
        edNombre.setText(evento.nombre)
        AlertDialog.Builder(this)
            .setTitle("Cambiar nombre")
            .setView(dialog)
            .setPositiveButton(getString(R.string.strAceptar)) { view, _ ->
                val nuevoNombre = edNombre.text.toString()
                BDFirestore.changeNameEvent(evento, nuevoNombre)
                evento.nombre = nuevoNombre
                title = evento.nombre
                Toast.makeText(this, getString(R.string.strSuccess), Toast.LENGTH_SHORT).show()
                view.dismiss()
            }
            .setNegativeButton(getString(R.string.strCancelar)) { view, _ ->
                view.dismiss()
            }
            .setCancelable(true).create().show()
    }

    private fun cargarMapa() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentMapEvento) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        enableMyLocation()
        obtenerMiUbi()
        if (evento.puntoReunion != null) {
            val pos = evento.localizacionPuntoReunion()
            map.addMarker(
                MarkerOptions().position(pos).title(evento.nombre)
                    .snippet("${evento.fecha} ${evento.hora}")
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f))
        } else {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    myUbication, 14f
                )
            )
            map.setOnMyLocationChangeListener(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtenerMiUbi() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val ubi = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        myUbication = LatLng(ubi!!.latitude, ubi.longitude)
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

    fun cambiarUbicacion(view: View) {
        Toast.makeText(this, "Abriendo mapa a lo grande", Toast.LENGTH_SHORT).show()
    }

    override fun onMyLocationChange(ubication: Location) {
        myUbication = LatLng(ubication.latitude, ubication.longitude)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(myUbication, 14f),
            2000, null
        )
    }
}