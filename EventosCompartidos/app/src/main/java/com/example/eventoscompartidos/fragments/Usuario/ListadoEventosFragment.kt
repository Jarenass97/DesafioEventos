package com.example.eventoscompartidos.fragments.Usuario

import adapters.EventosAdapter
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import assistant.BDFirebase
import com.example.eventoscompartidos.R
import com.example.eventoscompartidos.fragments.PerfilUsuarioFragment
import kotlinx.android.synthetic.main.fragment_listado_eventos.*

class ListadoEventosFragment(val ventana: AppCompatActivity) : Fragment() {

    lateinit var adaptador: EventosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_listado_eventos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvGestionEventos.setHasFixedSize(true)
        rvGestionEventos.layoutManager = LinearLayoutManager(ventana)
        adaptador = EventosAdapter(ventana, BDFirebase.getEventos())
        rvGestionEventos.adapter = adaptador
    }

}