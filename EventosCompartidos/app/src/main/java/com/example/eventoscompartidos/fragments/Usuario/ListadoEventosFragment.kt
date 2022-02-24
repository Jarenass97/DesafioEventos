package com.example.eventoscompartidos.fragments.Usuario

import adapters.EventosAdapter
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import assistant.BDFirebase
import com.example.eventoscompartidos.R
import kotlinx.android.synthetic.main.fragment_listado.*

class ListadoEventosFragment(val ventana: AppCompatActivity) : Fragment() {

    lateinit var adaptador: EventosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_listado, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvListado.setHasFixedSize(true)
        rvListado.layoutManager = LinearLayoutManager(ventana)
        adaptador = EventosAdapter(ventana, BDFirebase.getEventos())
        rvListado.adapter = adaptador
    }

}