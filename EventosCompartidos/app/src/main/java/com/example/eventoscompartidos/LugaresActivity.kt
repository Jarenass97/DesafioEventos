package com.example.eventoscompartidos

import adapters.LugaresAdapter
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import assistant.Auxiliar
import assistant.BDFirebase
import kotlinx.android.synthetic.main.fragment_listado.*
import model.Evento
import model.MapsOptions

class LugaresActivity : AppCompatActivity() {

    lateinit var adaptador: LugaresAdapter
    private lateinit var evento: Evento
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_listado)

        val bun: Bundle = intent.extras!!
        evento = bun.getSerializable("evento") as Evento
        title = evento.nombre
        rvListado.setHasFixedSize(true)
        rvListado.layoutManager = LinearLayoutManager(this)
        adaptador = LugaresAdapter(this, evento.lugares, evento)
        rvListado.adapter = adaptador
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miAdd -> addPlaces()
        }
        return super.onOptionsItemSelected(item)
    }

    fun addPlaces() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("opcion", MapsOptions.ADD_PLACES)
        intent.putExtra("evento", evento)
        startActivityForResult(intent, Auxiliar.CODE_PLACES)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Auxiliar.CODE_PLACES -> {
                evento = BDFirebase.getEvento(Auxiliar.idEvento(evento))
                adaptador = LugaresAdapter(this, evento.lugares, evento)
                rvListado.adapter = adaptador
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}