package com.example.eventoscompartidos

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import assistant.Auxiliar
import kotlinx.android.synthetic.main.activity_comentarios.*
import model.Evento
import model.Lugar

class ComentariosActivity : AppCompatActivity() {
    lateinit var evento: Evento
    lateinit var lugar: Lugar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comentarios)

        val bun: Bundle = intent.extras!!
        evento = bun.getSerializable("evento") as Evento
        lugar = bun.getSerializable("lugar") as Lugar
        title = getString(R.string.strTituloComentarios, lugar.nombre)

        rvComentarios.setHasFixedSize(true)
        rvComentarios.layoutManager = LinearLayoutManager(this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miAdd -> addComment()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addComment() {
        val intent = Intent(this, ComentarioCreaterActivity::class.java)
        intent.putExtra("lugar", lugar)
        intent.putExtra("evento", evento)
        startActivityForResult(intent, Auxiliar.CODE_COMMENTS)
    }
}