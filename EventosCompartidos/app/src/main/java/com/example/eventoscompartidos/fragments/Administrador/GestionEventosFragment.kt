package com.example.eventoscompartidos.fragments.Administrador

import adapters.EventosAdapter
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import assistant.BDFirebase
import assistant.DatePickerFragment
import assistant.TimePickerFragment
import com.example.eventoscompartidos.R
import kotlinx.android.synthetic.main.fragment_listado.*
import model.Evento

class GestionEventosFragment(val ventana: AppCompatActivity) : Fragment() {

    lateinit var adaptador: EventosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_listado, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvListado.setHasFixedSize(true)
        rvListado.layoutManager = LinearLayoutManager(ventana)
        adaptador = EventosAdapter(ventana, BDFirebase.getEventos())
        rvListado.adapter = adaptador
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_admin_events_fragment, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miAddEvent -> crearEvento()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        adaptador = EventosAdapter(ventana, BDFirebase.getEventos())
        rvListado.adapter = adaptador
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
                    BDFirebase.addEvento(ev)
                    adaptador = EventosAdapter(ventana, BDFirebase.getEventos())
                    rvListado.adapter = adaptador
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