package com.example.eventoscompartidos

import adapters.LugaresAdapter
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import assistant.Auxiliar
import kotlinx.android.synthetic.main.fragment_listado.*
import model.Evento
import model.MapsOptions

class LugaresActivity : AppCompatActivity() {

    private lateinit var evento: Evento
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_listado)

        val bun: Bundle = intent.extras!!
        evento = bun.getSerializable("evento") as Evento
        rvListado.setHasFixedSize(true)
        rvListado.layoutManager = LinearLayoutManager(this)
        rvListado.adapter = LugaresAdapter(this, evento.lugares, evento)
    }

    fun addPlaces() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("opcion", MapsOptions.ADD_PLACES)
        intent.putExtra("evento", evento)
        startActivityForResult(intent, Auxiliar.CODE_PLACES)
    }
}