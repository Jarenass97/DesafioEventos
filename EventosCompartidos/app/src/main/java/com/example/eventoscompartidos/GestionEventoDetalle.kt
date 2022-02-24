package com.example.eventoscompartidos

import adapters.AsistentesAdapter
import adapters.UsuariosAinvitarAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import assistant.Auxiliar
import assistant.Auxiliar.usuario
import assistant.BDFirebase
import assistant.DatePickerFragment
import assistant.TimePickerFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_gestion_evento_detalle.*
import model.Asistente
import model.Evento
import model.Localizacion
import model.MapsOptions

class GestionEventoDetalle : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationChangeListener {
    private lateinit var evento: Evento
    lateinit var map: GoogleMap
    private val LOCATION_REQUEST_CODE: Int = 0
    var myUbication: LatLng = LatLng(40.4165, -3.70256)
    lateinit var adaptadorAsistentes: AsistentesAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_evento_detalle)

        val bun: Bundle = intent.extras!!
        val idEvento = bun.getSerializable(getString(R.string.strEvento)) as String
        evento = BDFirebase.getEvento(idEvento)
        title = evento.nombre

        cargarMapa()
        cargarDatos()
        if (usuario.isAdmin()) {
            edFechaEventoDetalle.setOnClickListener {
                showDatePickerDialog(edFechaEventoDetalle)
            }
            edHoraEventoDetalle.setOnClickListener {
                showTimePickerDialog(edHoraEventoDetalle)
            }

        }
        btnInvitarUsuario.setOnClickListener {
            if (usuario.isAdmin()) mostrarUsuarios()
            else apuntarse()
        }
        if (!usuario.isAdmin()) {
            btnChangePuntoReunion.text = getString(R.string.strVerLugares)
            btnSalirDeEvento.setOnClickListener {
                desapuntarse()
            }
        }
        btnChangePuntoReunion.setOnClickListener {
            if (usuario.isAdmin()) cambiarUbicacion()
            else irLugares()
        }
        btnSalirDeEvento.isVisible = !usuario.isAdmin()
    }

    private fun desapuntarse() {
        if (evento.estoyApuntado()) {
            evento.delAsistente(usuario.email)
            Toast.makeText(this, getString(R.string.strDesapuntado), Toast.LENGTH_SHORT)
                .show()
            adaptadorAsistentes.notifyDataSetChanged()
        } else {
            Toast.makeText(
                this,
                getString(R.string.strNoApuntado),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun apuntarse() {
        if (evento.estoyApuntado())
            Toast.makeText(
                this,
                getString(R.string.strYaApuntado),
                Toast.LENGTH_SHORT
            ).show()
        else {
            evento.addAsistente(Asistente(usuario.email))
            Toast.makeText(this, getString(R.string.strApuntado, evento.nombre), Toast.LENGTH_SHORT)
                .show()
            adaptadorAsistentes.notifyDataSetChanged()
        }
    }

    private fun mostrarUsuarios() {
        val dialog = layoutInflater.inflate(R.layout.layout_recycler, null)
        val recycler = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = cargarRecycler(recycler)
        if (adapter.itemCount > 0) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.strUsuarios))
                .setView(dialog)
                .setPositiveButton(getString(R.string.strAceptar)) { view, _ ->
                    val emailUsuario = adapter.getSelected()
                    if (emailUsuario.isNotEmpty()) {
                        evento.addAsistente(
                            Asistente(emailUsuario)
                        )
                        Toast.makeText(
                            this,
                            getString(R.string.strInvitacionCorrecta, emailUsuario),
                            Toast.LENGTH_SHORT
                        ).show()
                        adaptadorAsistentes.notifyDataSetChanged()
                    } else Toast.makeText(
                        this,
                        getString(R.string.strAlertaUsuarioNoSeleccionado),
                        Toast.LENGTH_SHORT
                    ).show()
                    view.dismiss()
                }
                .setCancelable(true).create().show()
        } else
            Toast.makeText(this, getString(R.string.strSinUsuariosDisponibles), Toast.LENGTH_SHORT)
                .show()
    }

    private fun cargarRecycler(recycler: RecyclerView): UsuariosAinvitarAdapter {
        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(this)
        val adaptador = UsuariosAinvitarAdapter(this, BDFirebase.getUsuariosDisponibles(evento))
        recycler.adapter = adaptador
        return adaptador
    }

    private fun cargarDatos() {
        edFechaEventoDetalle.setText(evento.fecha)
        edHoraEventoDetalle.setText(evento.hora)
        rvAsistentes.setHasFixedSize(true)
        rvAsistentes.layoutManager = LinearLayoutManager(this)
        adaptadorAsistentes = AsistentesAdapter(this, evento.asistentes, evento)
        rvAsistentes.adapter = adaptadorAsistentes
    }

    private fun showTimePickerDialog(hora: EditText) {
        TimePickerFragment(hora, true, evento).show(supportFragmentManager, "timePicker")
    }

    private fun showDatePickerDialog(edFecha: EditText) {
        val newFragment = DatePickerFragment(edFecha, true, evento)
        newFragment.show(supportFragmentManager, "datePicker")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (usuario.isAdmin()) menuInflater.inflate(R.menu.menu_evento_detalle, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miEditName -> editarNombre()
            R.id.miAddPlaces -> addPlaces()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun irLugares() {
        val intent = Intent(this, LugaresActivity::class.java)
        intent.putExtra("evento", evento)
        startActivityForResult(intent, Auxiliar.CODE_PLACES)
    }

    fun addPlaces() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("opcion", MapsOptions.ADD_PLACES)
        intent.putExtra("evento", evento)
        startActivityForResult(intent, Auxiliar.CODE_PLACES)
    }

    private fun editarNombre() {
        val dialog = layoutInflater.inflate(R.layout.dialog_pide_string, null)
        val edNombre = dialog.findViewById<EditText>(R.id.edStringDialog)
        edNombre.setText(evento.nombre)
        AlertDialog.Builder(this)
            .setTitle("Cambiar nombre")
            .setView(dialog)
            .setPositiveButton(getString(R.string.strAceptar)) { view, _ ->
                val nuevoNombre = edNombre.text.toString()
                BDFirebase.changeNameEvent(evento, nuevoNombre)
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
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        enableMyLocation()
        if (!evento.sinPuntoReunion()) {
            val pos = evento.localizacionPuntoReunion()
            map.addMarker(
                MarkerOptions().position(pos).title(evento.nombre)
                    .snippet("${evento.fecha} ${evento.hora}")
            )
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f))
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
                obtenerMiUbi()
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

    fun cambiarUbicacion() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("opcion", MapsOptions.CHANGE_REUNION)
        startActivityForResult(intent, Auxiliar.CODE_CHANGE_UBICATION)
    }

    override fun onMyLocationChange(ubication: Location) {
        myUbication = LatLng(ubication.latitude, ubication.longitude)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(myUbication, 14f),
            2000, null
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Auxiliar.CODE_CHANGE_UBICATION -> {
                if (resultCode == Activity.RESULT_OK) {
                    val punto = data?.extras?.get("result") as Localizacion
                    evento.puntoReunion = punto
                    BDFirebase.establecerPuntoReunion(punto, evento)
                    map.clear()
                    cargarMapa()
                }
            }
            Auxiliar.CODE_PLACES -> evento = BDFirebase.getEvento(Auxiliar.idEvento(evento))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}