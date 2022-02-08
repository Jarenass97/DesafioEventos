package com.example.eventoscompartidos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import assistant.BDFirestore
import model.Evento

class GestionEventoDetalle : AppCompatActivity() {
    private lateinit var evento: Evento
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_evento_detalle)

        val bun: Bundle = intent.extras!!
        val idEvento = bun.getSerializable(getString(R.string.strEvento)) as String
        evento = BDFirestore.getEvento(idEvento)
        title = evento.nombre
    }
}