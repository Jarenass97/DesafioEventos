package com.example.eventoscompartidos.fragments.Administrador

import adapters.GestionEventosAdapter
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import assistant.BDFirestore
import assistant.DatePickerFragment
import assistant.TimePickerFragment
import com.example.eventoscompartidos.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_gestion_eventos.*
import model.Evento

class GestionEventosFragment(val ventana: AppCompatActivity) : Fragment() {

    lateinit var adaptador: GestionEventosAdapter
    val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gestion_eventos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvGestionEventos.setHasFixedSize(true)
        rvGestionEventos.layoutManager = LinearLayoutManager(ventana)
        adaptador = GestionEventosAdapter(ventana, BDFirestore.getEventos())
        rvGestionEventos.adapter = adaptador
        btnAddEvent.setOnClickListener {
            crearEvento()
        }
    }


    private fun crearEvento() {
        val creador = layoutInflater.inflate(R.layout.eventos_creater, null)
        val txtNombre = creador.findViewById<EditText>(R.id.edNombreEventoCreater)
        val fecha = creador.findViewById<EditText>(R.id.edFecha)
        val hora = creador.findViewById<EditText>(R.id.edHora)
        fecha.setOnClickListener { showDatePickerDialog(fecha) }
        hora.setOnClickListener { showTimePickerDialog(hora) }
        AlertDialog.Builder(ventana)
            .setTitle(getString(R.string.strCrearEvento))
            .setView(creador)
            .setPositiveButton("OK") { view, _ ->
                if (!camposVacios(txtNombre, fecha, hora)) {
                    val ev =
                        Evento(
                            txtNombre.text.toString(),
                            fecha.text.toString(),
                            hora.text.toString()
                        )
                    BDFirestore.addEvento(ev)
                    adaptador = GestionEventosAdapter(ventana, BDFirestore.getEventos())
                    rvGestionEventos.adapter = adaptador
                    // irMaps(ev)
                } else {
                    Toast.makeText(ventana, getString(R.string.strCamposVacios), Toast.LENGTH_SHORT)
                        .show()
                }
                view.dismiss()
            }
            .setCancelable(true)
            .create().show()
    }


    private fun showDatePickerDialog(edFecha: EditText) {
        val newFragment = DatePickerFragment(edFecha)
        newFragment.show(ventana.supportFragmentManager, "datePicker")
    }

    private fun showTimePickerDialog(hora: EditText) {
        TimePickerFragment(hora).show(ventana.supportFragmentManager, "timePicker")
    }

    private fun camposVacios(txtDesc: EditText, fecha: EditText, hora: EditText): Boolean {
        return txtDesc.text.isEmpty() ||
                fecha.text.isEmpty() ||
                hora.text.isEmpty()
    }

    /*private fun irMaps(evento: Evento) {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("evento", evento)
        intent.putExtra("opcion", OpcionMaps.CREAR)
        startActivityForResult(intent, CODE_MAPS)
    }*/

    /*private fun getEvents(): ArrayList<EventoGestion> {
        var eventos = ArrayList<EventoGestion>(0)
        runBlocking {
            val job: Job = launch {
                val data: QuerySnapshot = queryEventos() as QuerySnapshot
                for (dc: DocumentChange in data.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val event = EventoGestion(
                            dc.document.get(CamposBD.EMAIL__USUARIOS).toString()
                        )
                        eventos.add(event)
                    }
                }
            }
            job.join()
        }
        return eventos
    }
    private suspend fun queryEventos(): Any {
        return db.collection(CamposBD.COL_USUARIOS)
            .whereNotEqualTo(CamposBD.EMAIL__USUARIOS, Auxiliar.usuario.email)
            .get()
            .await()
    }*/
}