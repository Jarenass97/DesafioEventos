package com.example.eventoscompartidos.fragments.Administrador

import adapters.GestionUsuariosAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import assistant.Auxiliar.usuario
import assistant.BDFirestore
import assistant.BDFirestore.ACTIVADO__USUARIOS
import assistant.BDFirestore.COL_USUARIOS
import assistant.BDFirestore.EMAIL__USUARIOS
import com.example.eventoscompartidos.R
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_gestion_usuarios.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import model.UsuarioItem

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
        adaptador = GestionUsuariosAdapter(ventana, BDFirestore.getUsers())
        rvGestionUsuarios.adapter = adaptador
    }

}