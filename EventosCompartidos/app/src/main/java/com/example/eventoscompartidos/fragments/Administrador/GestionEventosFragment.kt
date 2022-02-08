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

    override fun onResume() {
        super.onResume()
        adaptador = GestionEventosAdapter(ventana, BDFirestore.getEventos())
        rvGestionEventos.adapter = adaptador
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

}