package com.example.eventoscompartidos.fragments.Administrador

import adapters.GestionUsuariosAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import assistant.BDFirebase
import com.example.eventoscompartidos.R
import kotlinx.android.synthetic.main.fragment_gestion_usuarios.*

class GestionUsuariosFragment(val ventana: AppCompatActivity) : Fragment() {

    lateinit var adaptador: GestionUsuariosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gestion_usuarios, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvGestionUsuarios.setHasFixedSize(true)
        rvGestionUsuarios.layoutManager = LinearLayoutManager(ventana)
        adaptador = GestionUsuariosAdapter(ventana, BDFirebase.getUsers())
        rvGestionUsuarios.adapter = adaptador
    }

}